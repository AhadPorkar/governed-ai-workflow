package io.github.ahadporkar.ordertocash.governance.tests;
import io.github.ahadporkar.ordertocash.governance.*;
import static io.github.ahadporkar.ordertocash.governance.tests.TestSupport.*;
import com.sun.net.httpserver.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
public final class ModelTests {
    private static Map<String,Object> call(String tool,Map<String,Object> args){return Data.map("function",Data.map("name",tool,"arguments",args));}
    private static Map<String,Object> message(Object calls,String content){return Data.map("message",Data.map("role","assistant","content",content,"tool_calls",calls),"prompt_eval_count",40,"eval_count",20);}
    private static String proposal(){return Json.stringify(new Proposal(Proposal.Action.REFUND,12900,"","Eligible captured balance; independent approval required.",List.of("POLICY-ORDER-EXCEPTION@v1")).toMap());}
    public static void run() {
        test("ollama.contract-real-http-tool-loop",()->{try(var fixture=new ModelServer(n->n==1?message(List.of(call("lookup_order",Map.of()),call("read_policy",Map.of())),""):message(List.of(),proposal()))){var result=fixture.planner().plan(context());equal("REFUND",result.proposal().action().name());equal(2,result.trace().size());equal(2,result.usage().get("modelCalls"));equal(80,result.usage().get("inputTokens"));equal(null,result.usage().get("estimatedCostUsd"));}});
        test("ollama.write-tool-denied",()->{try(var fixture=new ModelServer(n->message(List.of(call("execute_refund",Map.of())),""))){problem("TOOL_NOT_ALLOWED",()->fixture.planner().plan(context()));}});
        test("ollama.cross-order-argument-denied",()->{try(var fixture=new ModelServer(n->message(List.of(call("lookup_order",Data.map("orderId","other"))),""))){problem("TOOL_ARGUMENTS_NOT_ALLOWED",()->fixture.planner().plan(context()));}});
        test("ollama.infinite-tool-loop-bounded",()->{try(var fixture=new ModelServer(n->message(List.of(call("read_policy",Map.of())),""))){problem("MODEL_ROUND_BUDGET_EXHAUSTED",()->fixture.planner().plan(context()));equal(6,fixture.count.get());}});
        test("ollama.tool-count-bounded",()->{try(var fixture=new ModelServer(n->message(Collections.nCopies(9,call("read_policy",Map.of())),""))){problem("TOOL_BUDGET_EXHAUSTED",()->fixture.planner().plan(context()));}});
        test("ollama.invalid-final-json-rejected",()->{try(var fixture=new ModelServer(n->message(List.of(),"```json\n{}\n```"))){problem("INVALID_INPUT",()->fixture.planner().plan(context()));}});
        test("ollama.token-budget-bounded",()->{try(var fixture=new ModelServer(n->{var m=message(List.of(),proposal());m.put("prompt_eval_count",6001);return m;})){problem("MODEL_TOKEN_BUDGET_EXHAUSTED",()->fixture.planner().plan(context()));}});
        test("ollama.response-size-bounded",()->{try(var fixture=new ModelServer(n->message(List.of(),"x".repeat(70000)))){problem("MODEL_UNAVAILABLE",()->fixture.planner().plan(context()));}});
        test("ollama.model-endpoint-not-user-selectable",()->{problem("INVALID_MODEL_ENDPOINT",()->new OllamaPlanner(URI.create("http://example.com/api/chat"),"model"));});
        test("ollama.credentials-in-url-rejected",()->{problem("INVALID_MODEL_ENDPOINT",()->new OllamaPlanner(URI.create("https://user:password@example.com/api/chat"),"model"));});
        test("ollama.invalid-endpoint-path-rejected",()->{problem("INVALID_MODEL_ENDPOINT",()->new OllamaPlanner(URI.create("http://127.0.0.1:11434/admin"),"model"));});
        test("ollama.no-silent-fallback",()->{try(var e=new Env();var fixture=new ModelServer(n->message(List.of(),"not json"))){var c=e.open("ORD-1001","Refund");var result=e.engine.plan(OP,(String)c.get("id"),fixture.planner());equal("ESCALATED",result.get("state"));yes(result.containsKey("plannerFailure"));equal(0,Json.map(e.store.snapshot(),"effects").size());}});
    }
    private static Planner.Context context(){return new Planner.Context(DemoFixtures.order("x","demo",12900),"Please refund the order.",new Policy("v1"));}
    @FunctionalInterface private interface Reply {Map<String,Object> get(int request);}
    private static final class ModelServer implements AutoCloseable {
        private final HttpServer server;final AtomicInteger count=new AtomicInteger();
        ModelServer(Reply reply)throws Exception {
            server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
            server.createContext("/api/chat",x->{try{var request=Json.obj(new String(x.getRequestBody().readAllBytes(),StandardCharsets.UTF_8));yes(request.containsKey("tools"));yes(!Json.stringify(request).contains("Bearer "));byte[] b=Json.stringify(reply.get(count.incrementAndGet())).getBytes(StandardCharsets.UTF_8);x.getResponseHeaders().set("Content-Type","application/json");x.sendResponseHeaders(200,b.length);x.getResponseBody().write(b);}finally{x.close();}});server.start();
        }
        OllamaPlanner planner(){return new OllamaPlanner(URI.create("http://127.0.0.1:"+server.getAddress().getPort()+"/api/chat"),"fixture-model",Duration.ofSeconds(5),null,null);}
        @Override public void close(){server.stop(0);}
    }
}
