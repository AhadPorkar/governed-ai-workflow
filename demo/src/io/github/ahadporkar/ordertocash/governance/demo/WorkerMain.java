package io.github.ahadporkar.ordertocash.governance.demo;
import io.github.ahadporkar.ordertocash.governance.*;
import java.net.*;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
/** Separate worker process. Deliberate exit 73 occurs after commit and before acknowledgement. */
public final class WorkerMain {
    public static void main(String[] args) throws Exception {
        String base=System.getenv().getOrDefault("GOVERNANCE_URL","http://127.0.0.1:8095");
        String token=System.getenv("GOVERNANCE_WORKER_TOKEN");
        if(token==null)throw new IllegalArgumentException("GOVERNANCE_WORKER_TOKEN is required");
        URI uri=URI.create(base);
        if(!Set.of("127.0.0.1","localhost").contains(uri.getHost())||!"http".equals(uri.getScheme())||uri.getUserInfo()!=null||uri.getQuery()!=null)throw new IllegalArgumentException("Demo worker accepts a loopback HTTP server only");
        boolean once=Arrays.asList(args).contains("--once"),crash=Arrays.asList(args).contains("--crash-after-commit");
        var http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).followRedirects(HttpClient.Redirect.NEVER).build();
        do {
            var result=request(http,base,"GET","/api/jobs",token,null);
            for(Object value:Json.list(result,"jobs")) {
                String id=Data.str(Json.object(value),"id",36);
                try {
                    var receipt=request(http,base,"POST","/api/jobs/"+id+"/run",token,Map.of());
                    System.out.println("COMMITTED case="+id+" receipt="+Json.text(receipt,"id",""));System.out.flush();
                    if(crash)Runtime.getRuntime().halt(73);
                    request(http,base,"POST","/api/jobs/"+id+"/ack",token,Data.map("receiptId",receipt.get("id")));
                    System.out.println("ACKNOWLEDGED case="+id);
                } catch(Problem p) { if(p.status==409)System.err.println("Job no longer authorized: "+id);else throw p; }
            }
            if(!once)Thread.sleep(1500);
        }while(!once);
    }
    private static Map<String,Object> request(HttpClient http,String base,String method,String path,String token,Map<String,Object> body) throws Exception {
        var builder=HttpRequest.newBuilder(URI.create(base+path)).timeout(Duration.ofSeconds(10)).header("Authorization","Bearer "+token);
        if(body==null)builder.GET();else builder.header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(Json.stringify(body)));
        var response=http.send(builder.build(),HttpResponse.BodyHandlers.ofString());
        if(response.statusCode()!=200)throw new Problem(response.statusCode(),"WORKER_HTTP_ERROR");return Json.obj(response.body());
    }
}
