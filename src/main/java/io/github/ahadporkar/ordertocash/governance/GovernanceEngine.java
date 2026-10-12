package io.github.ahadporkar.ordertocash.governance;
import java.time.*;
import java.util.*;
/**
 * Application core. Effects update a sandbox order ledger, receipt, audit and outbox in one transaction.
 * A real payment/supplier connector must add its own idempotency and reconciliation protocol.
 */
public final class GovernanceEngine {
    private final StateStore store;
    private final Policy policy;
    private final Clock clock;
    private final Duration approvalTtl;
    public GovernanceEngine(StateStore store,Policy policy,Clock clock,Duration approvalTtl) {
        Problem.require(!approvalTtl.isNegative()&&!approvalTtl.isZero()&&approvalTtl.compareTo(Duration.ofHours(24))<=0,500,"INVALID_APPROVAL_TTL");
        this.store=store;this.policy=policy;this.clock=clock;this.approvalTtl=approvalTtl;
    }
    public GovernanceEngine(StateStore store) { this(store,new Policy("2026-10-12.1"),Clock.systemUTC(),Duration.ofMinutes(15)); }
    public StateStore store() { return store; }
    public Policy policy() { return policy; }
    public Map<String,Object> registerOrder(Actor actor,Map<String,Object> order,boolean replace) {
        actor.require("ADMIN");var o=Data.copy(order);String id=Data.id(Data.str(o,"id",100));
        actor.tenant(Data.str(o,"tenant",100));
        for(String field:List.of("totalCents","capturedCents","refundedCents","unitPriceCents","originalStock"))Data.integer(o,field,0,100_000_000);
        Data.integer(o,"quantity",1,10_000);Data.str(o,"currency",3);Data.id(Data.str(o,"sku",100));
        Problem.require(Set.of("UNFULFILLED","READY","SHIPPED","CANCELLED","REFUNDED").contains(o.get("status")),400,"INVALID_STATUS");
        Problem.require(Json.number(o,"refundedCents",0)<=Json.number(o,"capturedCents",0)&&Json.number(o,"capturedCents",0)<=Json.number(o,"totalCents",0),400,"INVALID_BALANCE");
        for(Object a:Json.list(o,"alternatives")) {var v=Json.object(a);Data.id(Data.str(v,"id",64));Data.integer(v,"stock",0,100_000_000);Data.integer(v,"unitPriceCents",0,100_000_000);Json.bool(v,"approved",false);}
        return store.transaction(s->{
            var orders=Json.map(s,"orders");String key=Data.orderKey(actor.tenant(),id);
            var old=Json.map(orders,key);
            if(!old.isEmpty()&&!replace)return old;
            o.put("version",old.isEmpty()?1:Json.number(old,"version",0)+1);orders.put(key,o);
            audit(s,actor.tenant(),"",actor.id(),"ORDER_REGISTERED",Data.map("orderId",id,"version",o.get("version")));return o;
        });
    }
    public Map<String,Object> openCase(Actor actor,String orderId,String request,String idempotencyKey) {
        actor.require("OPERATOR");Data.id(orderId);Data.id(idempotencyKey);
        Problem.require(request!=null&&!request.isBlank()&&request.length()<=4000,400,"INVALID_REQUEST");
        String fingerprint=Data.sha(Json.canonical(Data.map("orderId",orderId,"request",request)));
        return store.transaction(s->{
            String key=actor.tenant()+"/"+actor.id()+"/"+idempotencyKey;
            var requests=Json.map(s,"requests");var prior=Json.map(requests,key);
            if(!prior.isEmpty()) {
                Problem.require(Data.equal(fingerprint,Json.text(prior,"fingerprint","")),409,"IDEMPOTENCY_CONFLICT");
                return findCase(s,actor,Json.text(prior,"caseId",""));
            }
            findOrder(s,actor.tenant(),orderId);
            var cases=Json.map(s,"cases");Problem.require(cases.size()<10000,503,"DEMO_CAPACITY_REACHED");
            String id=UUID.randomUUID().toString();
            var c=Data.map("id",id,"tenant",actor.tenant(),"orderId",orderId,"request",request,"requestHash",Data.sha(request),
                    "createdBy",actor.id(),"createdAt",clock.instant().toString(),"state","CREATED","revision",0);
            cases.put(id,c);requests.put(key,Data.map("fingerprint",fingerprint,"caseId",id));
            audit(s,actor.tenant(),id,actor.id(),"CASE_OPENED",Data.map("orderId",orderId,"requestHash",c.get("requestHash")));
            event(s,c,"GovernanceCaseOpened",Data.map("caseId",id,"tenant",actor.tenant()));return c;
        });
    }
    public Map<String,Object> plan(Actor actor,String caseId,Planner planner) {
        Problem.require(actor.roles().contains("OPERATOR")||actor.roles().contains("WORKER"),403,"ROLE_REQUIRED");
        Map<String,Object> start=store.transaction(s->{
            var c=findCase(s,actor,caseId);String state=Json.text(c,"state","");
            if(!Set.of("CREATED","INVALIDATED","PLANNING").contains(state))return Data.map("existing",c);
            if("PLANNING".equals(state))Problem.require(!clock.instant().isBefore(Instant.parse(Json.text(c,"leaseUntil",""))),409,"PLANNING_IN_PROGRESS");
            var order=findOrder(s,actor.tenant(),Json.text(c,"orderId",""));
            String token=UUID.randomUUID().toString();c.put("state","PLANNING");c.put("planToken",token);
            c.put("leaseUntil",clock.instant().plusSeconds(120).toString());c.put("revision",Json.number(c,"revision",0)+1);
            c.remove("approvedBy");c.remove("approvedDigest");c.remove("effectId");
            audit(s,actor.tenant(),caseId,actor.id(),"PLANNING_STARTED",Data.map("provider",planner.name(),"revision",c.get("revision")));
            return Data.map("case",c,"order",order,"token",token);
        });
        if(start.containsKey("existing"))return Json.map(start,"existing");
        Planner.Result result;
        String failure=null;
        try { result=planner.plan(new Planner.Context(Json.map(start,"order"),Json.text(Json.map(start,"case"),"request",""),policy)); }
        catch(RuntimeException e) {
            failure=e instanceof Problem p?p.code:"PLANNER_UNAVAILABLE";
            result=new Planner.Result(new Proposal(Proposal.Action.ESCALATE,0,"","Automated investigation was unavailable. A human must investigate before any action.",List.of()),List.of(),Data.map("provider",planner.name(),"failure",failure));
        }
        final Planner.Result planned=result;final String error=failure;
        return store.transaction(s->{
            var c=findCase(s,actor,caseId);
            Problem.require(Objects.equals(c.get("planToken"),start.get("token"))&&"PLANNING".equals(c.get("state")),409,"STALE_PLAN_RESULT");
            var order=findOrder(s,actor.tenant(),Json.text(c,"orderId",""));
            if(Json.number(order,"version",0)!=Json.number(Json.map(start,"order"),"version",-1)) {
                c.put("state","INVALIDATED");c.put("blockReasons",List.of("ORDER_CHANGED_DURING_PLANNING"));
                audit(s,actor.tenant(),caseId,actor.id(),"PLAN_INVALIDATED",Data.map("reason","ORDER_CHANGED_DURING_PLANNING"));return c;
            }
            Proposal p=planned.proposal();var reasons=new ArrayList<>(policy.violations(order,p));
            Set<String> evidence=new HashSet<>();for(var t:planned.trace())evidence.add(Json.text(t,"tool",""));
            if(p.action()!=Proposal.Action.ESCALATE&&(!evidence.contains("lookup_order")||!evidence.contains("read_policy")))reasons.add("REQUIRED_EVIDENCE_NOT_READ");
            if(p.action()==Proposal.Action.SWITCH_SUPPLIER&&!evidence.contains("list_alternatives"))reasons.add("ALTERNATIVES_NOT_READ");
            c.put("proposal",p.toMap());c.put("trace",planned.trace());c.put("usage",planned.usage());
            c.put("orderVersion",order.get("version"));c.put("policyVersion",policy.version());
            c.put("expiresAt",clock.instant().plus(approvalTtl).toString());c.put("proposalDigest",digest(c));c.put("blockReasons",reasons);
            c.put("state",!reasons.isEmpty()?"BLOCKED":p.action()==Proposal.Action.ESCALATE?"ESCALATED":"PENDING_APPROVAL");
            if(error!=null)c.put("plannerFailure",error);else c.remove("plannerFailure");
            audit(s,actor.tenant(),caseId,actor.id(),"PROPOSAL_RECORDED",Data.map("state",c.get("state"),"digest",c.get("proposalDigest"),"reasons",reasons));return c;
        });
    }
    public Map<String,Object> decide(Actor actor,String caseId,String submittedDigest,boolean approve,String reason) {
        actor.require("REVIEWER");Problem.require(reason!=null&&!reason.isBlank()&&reason.length()<=600,400,"REASON_REQUIRED");
        return checked(store.transaction(s->{
            var c=findCase(s,actor,caseId);
            if(!"PENDING_APPROVAL".equals(c.get("state")))return deny(s,c,actor,"DECISION_STATE_CONFLICT",409);
            if(actor.id().equals(c.get("createdBy")))return deny(s,c,actor,"SELF_APPROVAL_FORBIDDEN",403);
            if(!Data.equal(submittedDigest,Json.text(c,"proposalDigest","")))return deny(s,c,actor,"PROPOSAL_DIGEST_MISMATCH",409);
            String invalid=invalid(c,findOrder(s,actor.tenant(),Json.text(c,"orderId","")));
            if(invalid!=null) { invalidate(s,c,invalid);return failure(invalid,409); }
            c.put("state",approve?"APPROVED":"DECLINED");c.put("reviewedBy",actor.id());c.put("decisionReason",reason);c.put("decidedAt",clock.instant().toString());
            if(approve) {
                c.put("approvedBy",actor.id());c.put("approvedDigest",c.get("proposalDigest"));
                Json.map(s,"jobs").put(caseId,Data.map("id",caseId,"caseId",caseId,"tenant",actor.tenant(),"state","READY","attempts",0));
            }
            audit(s,actor.tenant(),caseId,actor.id(),approve?"APPROVAL_GRANTED":"PROPOSAL_DECLINED",Data.map("digest",c.get("proposalDigest"),"reason",reason));
            event(s,c,"GovernanceDecisionRecorded",Data.map("caseId",caseId,"tenant",actor.tenant(),"approved",approve));return c;
        }));
    }
    public Map<String,Object> execute(Actor actor,String caseId) {
        actor.require("WORKER");
        return checked(store.transaction(s->{
            var c=findCase(s,actor,caseId);var effects=Json.map(s,"effects");
            if("EXECUTED".equals(c.get("state"))) {
                var receipt=Json.map(effects,Json.text(c,"effectId",""));Problem.require(!receipt.isEmpty(),500,"MISSING_EFFECT_RECEIPT");
                var job=Json.map(Json.map(s,"jobs"),caseId);job.put("attempts",Json.number(job,"attempts",0)+1);
                audit(s,actor.tenant(),caseId,actor.id(),"EXECUTION_REPLAYED",Data.map("receiptId",receipt.get("id")));return receipt;
            }
            if(!"APPROVED".equals(c.get("state")))return deny(s,c,actor,"APPROVAL_REQUIRED",409);
            var order=findOrder(s,actor.tenant(),Json.text(c,"orderId",""));String invalid=invalid(c,order);
            if(invalid!=null){invalidate(s,c,invalid);return failure(invalid,409);}
            if(!Data.equal(Json.text(c,"approvedDigest",""),digest(c))||Json.text(c,"approvedBy","").isBlank()||Objects.equals(c.get("approvedBy"),c.get("createdBy"))) {
                invalidate(s,c,"APPROVAL_BINDING_INVALID");return failure("APPROVAL_BINDING_INVALID",409);
            }
            Proposal p=Proposal.parse(Json.map(c,"proposal"));var violations=policy.violations(order,p);
            if(!violations.isEmpty()||p.action()==Proposal.Action.ESCALATE) {invalidate(s,c,"EXECUTION_POLICY_DENIED");return failure("EXECUTION_POLICY_DENIED",409);}
            String key=caseId+"/"+Json.text(c,"approvedDigest","");
            Problem.require(!effects.containsKey(key),500,"EFFECT_STATE_INCONSISTENT");
            switch(p.action()) {
                case REFUND -> {
                    long refunded=Json.number(order,"refundedCents",0)+p.amountCents();order.put("refundedCents",refunded);
                    if(refunded==Json.number(order,"capturedCents",0))order.put("status","REFUNDED");
                }
                case CANCEL -> order.put("status","CANCELLED");
                case SWITCH_SUPPLIER -> {
                    for(Object x:Json.list(order,"alternatives")) {var a=Json.object(x);if(p.supplierId().equals(a.get("id")))a.put("stock",Json.number(a,"stock",0)-Json.number(order,"quantity",1));}
                    order.put("supplierId",p.supplierId());order.put("status","READY");
                }
                default -> throw new Problem(409,"ACTION_NOT_EXECUTABLE");
            }
            order.put("version",Json.number(order,"version",0)+1);
            var receipt=Data.map("id",UUID.randomUUID().toString(),"idempotencyKey",key,"caseId",caseId,"tenant",actor.tenant(),"orderId",order.get("id"),
                    "action",p.action().name(),"amountCents",p.amountCents(),"currency",order.get("currency"),"supplierId",p.supplierId(),
                    "committedAt",clock.instant().toString(),"sandbox",true,"proposalDigest",c.get("approvedDigest"));
            effects.put(key,receipt);c.put("state","EXECUTED");c.put("effectId",key);
            var job=Json.map(Json.map(s,"jobs"),caseId);job.put("attempts",Json.number(job,"attempts",0)+1);
            audit(s,actor.tenant(),caseId,actor.id(),"EFFECT_COMMITTED",Data.map("receiptId",receipt.get("id"),"action",p.action().name(),"sandbox",true));
            event(s,c,"GovernanceEffectRecorded",receipt);return receipt;
        }));
    }
    public Map<String,Object> acknowledge(Actor actor,String caseId,String receiptId) {
        actor.require("WORKER");return store.transaction(s->{
            var c=findCase(s,actor,caseId);var effect=Json.map(Json.map(s,"effects"),Json.text(c,"effectId",""));
            Problem.require("EXECUTED".equals(c.get("state"))&&Objects.equals(effect.get("id"),receiptId),409,"INVALID_RECEIPT");
            var job=Json.map(Json.map(s,"jobs"),caseId);
            if(!"COMPLETED".equals(job.get("state"))) {job.put("state","COMPLETED");audit(s,actor.tenant(),caseId,actor.id(),"WORKER_ACKNOWLEDGED",Data.map("receiptId",receiptId));}
            return job;
        });
    }
    public Map<String,Object> expire(Actor actor,String caseId) {
        actor.require("WORKER");return store.transaction(s->{
            var c=findCase(s,actor,caseId);
            if(Set.of("PENDING_APPROVAL","APPROVED").contains(c.get("state"))&&!clock.instant().isBefore(Instant.parse(Json.text(c,"expiresAt",""))))invalidate(s,c,"APPROVAL_EXPIRED");
            return c;
        });
    }
    public List<Map<String,Object>> readyJobs(Actor actor) {
        actor.require("WORKER");return Json.map(store.snapshot(),"jobs").values().stream().map(Json::object)
                .filter(j->actor.tenant().equals(j.get("tenant"))&&"READY".equals(j.get("state"))).map(Data::copy).toList();
    }
    public Map<String,Object> getCase(Actor actor,String caseId) {
        actor.require("READ");var s=store.snapshot();var c=Data.copy(findCase(s,actor,caseId));
        c.put("audit",Json.list(s,"audit").stream().map(Json::object).filter(a->actor.tenant().equals(a.get("tenant"))&&caseId.equals(a.get("caseId"))).toList());
        if(c.containsKey("effectId"))c.put("receipt",Json.map(Json.map(s,"effects"),Json.text(c,"effectId","")));
        c.remove("planToken");return c;
    }
    public Map<String,Object> dashboard(Actor actor) {
        actor.require("READ");var s=store.snapshot();
        var cases=Json.map(s,"cases").values().stream().map(Json::object).filter(c->actor.tenant().equals(c.get("tenant"))).map(c->{var v=Data.copy(c);v.remove("planToken");return v;}).sorted(Comparator.comparing(c->Json.text(c,"createdAt",""),Comparator.reverseOrder())).toList();
        var orders=Json.map(s,"orders").values().stream().map(Json::object).filter(o->actor.tenant().equals(o.get("tenant"))).toList();
        return Data.map("cases",cases,"orders",orders,"policy",policy.document(),"auditValid",Audit.verify(s),"sandbox",true);
    }
    public List<Map<String,Object>> pendingEvents(int limit) {
        return Json.map(store.snapshot(),"outbox").values().stream().map(Json::object).filter(e->!Json.bool(e,"published",false)).limit(limit).map(Data::copy).toList();
    }
    public void acknowledgeEvent(String id) {
        store.transaction(s->{var event=Json.map(Json.map(s,"outbox"),id);Problem.require(!event.isEmpty(),404,"NOT_FOUND");event.put("published",true);return Map.of();});
    }
    public static String digest(Map<String,Object> c) {
        return Data.sha(Json.canonical(Data.map("tenant",c.get("tenant"),"caseId",c.get("id"),"orderId",c.get("orderId"),
                "orderVersion",c.get("orderVersion"),"policyVersion",c.get("policyVersion"),"revision",c.get("revision"),
                "requestHash",c.get("requestHash"),"proposal",c.get("proposal"),"expiresAt",c.get("expiresAt"))));
    }
    private String invalid(Map<String,Object> c,Map<String,Object> order) {
        if(!clock.instant().isBefore(Instant.parse(Json.text(c,"expiresAt",""))))return "APPROVAL_EXPIRED";
        if(!policy.version().equals(c.get("policyVersion")))return "POLICY_CHANGED";
        if(Json.number(c,"orderVersion",-1)!=Json.number(order,"version",-2))return "ORDER_CHANGED";
        if(!Data.equal(Json.text(c,"proposalDigest",""),digest(c)))return "PROPOSAL_TAMPERED";
        return null;
    }
    private void invalidate(Map<String,Object> s,Map<String,Object> c,String reason) {
        c.put("state","APPROVAL_EXPIRED".equals(reason)?"EXPIRED":"INVALIDATED");c.put("blockReasons",List.of(reason));
        var jobs=Json.map(s,"jobs");if(jobs.containsKey(c.get("id")))Json.object(jobs.get(c.get("id"))).put("state","FAILED");
        audit(s,Json.text(c,"tenant",""),Json.text(c,"id",""),"policy-engine","AUTHORIZATION_INVALIDATED",Data.map("reason",reason));
    }
    private Map<String,Object> deny(Map<String,Object> s,Map<String,Object> c,Actor actor,String code,int status) {
        audit(s,actor.tenant(),Json.text(c,"id",""),actor.id(),"ACTION_DENIED",Data.map("code",code));return failure(code,status);
    }
    private static Map<String,Object> failure(String code,int status) { return Data.map("_error",code,"_status",status); }
    private static Map<String,Object> checked(Map<String,Object> result) {
        if(result.containsKey("_error"))throw new Problem(Json.integer(result,"_status",409),Json.text(result,"_error","CONFLICT"));return result;
    }
    private static Map<String,Object> findCase(Map<String,Object> s,Actor actor,String id) {
        var c=Json.map(Json.map(s,"cases"),id);Problem.require(!c.isEmpty(),404,"NOT_FOUND");actor.tenant(Json.text(c,"tenant",""));return c;
    }
    private static Map<String,Object> findOrder(Map<String,Object> s,String tenant,String id) {
        var o=Json.map(Json.map(s,"orders"),Data.orderKey(tenant,id));Problem.require(!o.isEmpty(),404,"NOT_FOUND");return o;
    }
    private void audit(Map<String,Object> s,String tenant,String id,String actor,String event,Map<String,Object> details) { Audit.append(s,clock.instant(),tenant,id,actor,event,details); }
    private void event(Map<String,Object> s,Map<String,Object> c,String type,Map<String,Object> payload) {
        String id=UUID.randomUUID().toString();Json.map(s,"outbox").put(id,Data.map("id",id,"caseId",c.get("id"),"tenant",c.get("tenant"),"type",type,"payload",payload,"occurredAt",clock.instant().toString(),"published",false));
    }
}
