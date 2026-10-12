package io.github.ahadporkar.ordertocash.governance.demo;
import com.sun.net.httpserver.*;
import io.github.ahadporkar.ordertocash.governance.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
/** Local-only, dependency-free demo adapter. Intentionally not an Internet-facing application server. */
public final class DemoServer {
    public static void main(String[] args) throws Exception {
        Map<String,String> options=options(args);
        int port=Integer.parseInt(options.getOrDefault("port","8095"));
        Path state=Path.of(options.getOrDefault("state",".local/state"));
        Path credentials=Path.of(options.getOrDefault("auth",".local/auth.json"));
        Path web=Path.of(options.getOrDefault("web","web"));
        var store=new FileStateStore(state);var engine=new GovernanceEngine(store);
        if(options.containsKey("seed")){DemoFixtures.seed(engine,"demo");DemoFixtures.seed(engine,"other");}
        Planner planner;
        String mode=options.getOrDefault("provider","baseline");
        if("baseline".equals(mode))planner=new DeterministicPlanner();
        else if("ollama".equals(mode))planner=new OllamaPlanner(URI.create(options.getOrDefault("model-endpoint","http://127.0.0.1:11434/api/chat")),options.getOrDefault("model", ""));
        else throw new IllegalArgumentException("Unknown provider; expected baseline or ollama");
        var auth=new TokenAuth(Json.obj(Files.readString(credentials)),Clock.systemUTC());
        var router=new ApiRouter(engine,planner,auth,Integer.parseInt(options.getOrDefault("rate-limit","120")));
        HttpServer server=HttpServer.create(new InetSocketAddress("127.0.0.1",port),64);
        var executor=Executors.newVirtualThreadPerTaskExecutor();server.setExecutor(executor);
        String origin="http://127.0.0.1:"+server.getAddress().getPort();var permits=new Semaphore(32);
        server.createContext("/",exchange->{
            if(!permits.tryAcquire()) {respond(exchange,503,ApiRouter.problem(new Problem(503,"SERVER_BUSY")).body());return;}
            try { handle(exchange,router,web,origin); }
            catch(Problem p) {respond(exchange,p.status,ApiRouter.problem(p).body());}
            catch(Exception e) {System.err.println("Request failed: "+e.getClass().getSimpleName());respond(exchange,500,ApiRouter.problem(new Problem(500,"INTERNAL_ERROR")).body());}
            finally {permits.release();exchange.close();}
        });
        Runtime.getRuntime().addShutdownHook(new Thread(()->{server.stop(1);executor.close();store.close();}));
        server.start();System.out.println("Governed AI Workflow / release candidate / sandbox");
        System.out.println("LISTENING "+origin+" | "+planner.name());
    }
    private static void handle(HttpExchange x,ApiRouter router,Path web,String origin) throws IOException {
        headers(x);String path=x.getRequestURI().getRawPath();String method=x.getRequestMethod();
        String supplied=x.getRequestHeaders().getFirst("Origin");
        String localhost=origin.replace("127.0.0.1","localhost");
        Problem.require(supplied==null||supplied.equals(origin)||supplied.equals(localhost),403,"ORIGIN_NOT_ALLOWED");
        Problem.require(x.getRequestURI().getRawQuery()==null,400,"QUERY_NOT_ALLOWED");
        if(path.startsWith("/api/")) {
            Map<String,Object> body=Map.of();
            if("POST".equals(method)) {
                String type=x.getRequestHeaders().getFirst("Content-Type");
                Problem.require(type!=null&&type.split(";",2)[0].strip().equalsIgnoreCase("application/json"),415,"JSON_REQUIRED");
                byte[] bytes=x.getRequestBody().readNBytes(16385);Problem.require(bytes.length<=16384,413,"REQUEST_TOO_LARGE");
                try {body=Json.obj(StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString());}
                catch(CharacterCodingException e){throw new Problem(400,"INVALID_UTF8");}
            }
            var response=router.route(method,path,x.getRequestHeaders().getFirst("Authorization"),x.getRequestHeaders().getFirst("Idempotency-Key"),body);
            respond(x,response.status(),response.body());return;
        }
        Problem.require("GET".equals(method)||"HEAD".equals(method),405,"METHOD_NOT_ALLOWED");
        Map<String,String> resources=Map.of("/","index.html","/index.html","index.html","/app.js","app.js","/styles.css","styles.css","/icon.svg","icon.svg");
        String filename=resources.get(path);Problem.require(filename!=null,404,"NOT_FOUND");
        Path file=web.resolve(filename);Problem.require(Files.isRegularFile(file),404,"NOT_FOUND");byte[] bytes=Files.readAllBytes(file);
        String contentType=filename.endsWith(".js")?"text/javascript":filename.endsWith(".css")?"text/css":filename.endsWith(".svg")?"image/svg+xml":"text/html";
        x.getResponseHeaders().set("Content-Type",contentType+"; charset=utf-8");
        x.sendResponseHeaders(200,"HEAD".equals(method)?-1:bytes.length);if(!"HEAD".equals(method))x.getResponseBody().write(bytes);
    }
    private static void headers(HttpExchange x) {
        var h=x.getResponseHeaders();h.set("Cache-Control","no-store");h.set("X-Content-Type-Options","nosniff");h.set("X-Frame-Options","DENY");
        h.set("Referrer-Policy","no-referrer");h.set("Content-Security-Policy","default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self'; connect-src 'self'; object-src 'none'; base-uri 'none'; frame-ancestors 'none'; form-action 'self'");
        h.set("Permissions-Policy","camera=(), microphone=(), geolocation=()");
    }
    private static void respond(HttpExchange x,int status,Map<String,Object> value) throws IOException {
        headers(x);x.getResponseHeaders().set("Content-Type",status>=400?"application/problem+json":"application/json; charset=utf-8");
        if(status==401)x.getResponseHeaders().set("WWW-Authenticate","Bearer");
        if(status==429)x.getResponseHeaders().set("Retry-After","60");
        byte[] bytes=Json.stringify(value).getBytes(StandardCharsets.UTF_8);x.sendResponseHeaders(status,bytes.length);x.getResponseBody().write(bytes);
    }
    private static Map<String,String> options(String[] args) {
        var m=new HashMap<String,String>();
        for(int i=0;i<args.length;i++) {
            if(!args[i].startsWith("--"))throw new IllegalArgumentException("Expected --option");
            String key=args[i].substring(2);String value=(i+1<args.length&&!args[i+1].startsWith("--"))?args[++i]:"true";m.put(key,value);
        }
        return m;
    }
}
