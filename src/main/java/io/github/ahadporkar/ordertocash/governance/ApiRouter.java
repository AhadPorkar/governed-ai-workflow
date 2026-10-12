package io.github.ahadporkar.ordertocash.governance;
import java.time.*;
import java.util.*;
/** Shared routing for the JDK demo server and Spring MVC adapter. */
public final class ApiRouter {
    public record Response(int status,Map<String,Object> body) {}
    private final GovernanceEngine engine;private final Planner planner;private final TokenAuth auth;
    private final Map<String,long[]> limits=new HashMap<>();private final int requestsPerMinute;
    public ApiRouter(GovernanceEngine engine,Planner planner,TokenAuth auth,int requestsPerMinute) {
        Problem.require(requestsPerMinute>=1&&requestsPerMinute<=10000,500,"INVALID_RATE_LIMIT");
        this.engine=engine;this.planner=planner;this.auth=auth;this.requestsPerMinute=requestsPerMinute;
    }
    public Response route(String method,String path,String authorization,String idempotencyKey,Map<String,Object> body) {
        if("/api/health".equals(path)&&"GET".equals(method))return new Response(200,Data.map("status","UP","version","1.1.0-rc.1","sandbox",true,"planner",planner.name()));
        Actor actor=auth.authenticate(authorization);rateLimit(actor);
        if("GET".equals(method)) {
            if("/api/session".equals(path))return ok(Data.map("id",actor.id(),"tenant",actor.tenant(),"roles",actor.roles().stream().sorted().toList(),"planner",planner.name(),"sandbox",true));
            if("/api/dashboard".equals(path))return ok(engine.dashboard(actor));
            if("/api/jobs".equals(path))return ok(Data.map("jobs",engine.readyJobs(actor)));
            if(path.matches("/api/cases/[a-f0-9-]{36}"))return ok(engine.getCase(actor,path.substring("/api/cases/".length())));
            throw new Problem(404,"NOT_FOUND");
        }
        Problem.require("POST".equals(method),405,"METHOD_NOT_ALLOWED");
        if("/api/cases".equals(path)) {
            Data.keys(body,"orderId","request");return new Response(201,engine.openCase(actor,Data.str(body,"orderId",100),Data.str(body,"request",4000),idempotencyKey));
        }
        String[] parts=path.split("/");
        if(parts.length==5&&"api".equals(parts[1])&&parts[3].matches("[a-f0-9-]{36}")) {
            String id=parts[3];
            if("cases".equals(parts[2])) {
                if("plan".equals(parts[4])) {Data.keys(body);return ok(engine.plan(actor,id,planner));}
                if("decisions".equals(parts[4])) {
                    Data.keys(body,"proposalDigest","approve","reason");Problem.require(body.get("approve") instanceof Boolean,400,"INVALID_APPROVE");
                    return ok(engine.decide(actor,id,Data.str(body,"proposalDigest",64),(Boolean)body.get("approve"),Data.str(body,"reason",600)));
                }
            }
            if("jobs".equals(parts[2])) {
                if("run".equals(parts[4])) {Data.keys(body);return ok(engine.execute(actor,id));}
                if("ack".equals(parts[4])) {Data.keys(body,"receiptId");return ok(engine.acknowledge(actor,id,Data.str(body,"receiptId",36)));}
            }
        }
        throw new Problem(404,"NOT_FOUND");
    }
    private static Response ok(Map<String,Object> body) { return new Response(200,body); }
    public static Response problem(Problem p) { return new Response(p.status,Data.map("type","urn:governance:error:"+p.code.toLowerCase(Locale.ROOT),"title",p.code,"status",p.status)); }
    private synchronized void rateLimit(Actor actor) {
        long minute=Instant.now().getEpochSecond()/60;String key=actor.tenant()+"/"+actor.id();
        long[] count=limits.computeIfAbsent(key,k->new long[]{minute,0});if(count[0]!=minute){count[0]=minute;count[1]=0;}
        Problem.require(++count[1]<=requestsPerMinute,429,"RATE_LIMITED");
    }
}
