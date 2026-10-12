package io.github.ahadporkar.ordertocash.governance.tests;
import io.github.ahadporkar.ordertocash.governance.*;
import static io.github.ahadporkar.ordertocash.governance.tests.TestSupport.*;
import java.time.*;
import java.util.*;
public final class SecurityTests {
    public static void run() {
        test("tenant.case-read-hidden",()->{try(var e=new Env()){var c=e.open("ORD-1001","Refund");problem("NOT_FOUND",()->e.engine.getCase(OTHER,e.id(c)));}});
        test("tenant.case-plan-hidden",()->{try(var e=new Env()){var c=e.open("ORD-1001","Refund");problem("NOT_FOUND",()->e.engine.plan(OTHER,e.id(c),new DeterministicPlanner()));}});
        test("tenant.case-approve-hidden",()->{try(var e=new Env()){var c=e.planned("ORD-1001","Refund");problem("NOT_FOUND",()->e.engine.decide(OTHER,e.id(c),(String)c.get("proposalDigest"),true,"yes"));}});
        test("tenant.case-execute-hidden",()->{try(var e=new Env()){var c=e.approved("ORD-1001","Refund");problem("NOT_FOUND",()->e.engine.execute(OTHER,e.id(c)));}});
        test("tenant.dashboard-filtered",()->{try(var e=new Env()){e.open("ORD-1001","Refund");equal(0,Json.list(e.engine.dashboard(OTHER),"cases").size());yes(Json.list(e.engine.dashboard(OTHER),"orders").stream().map(Json::object).allMatch(o->"other".equals(o.get("tenant"))));}});
        test("role.operator-cannot-approve",()->{try(var e=new Env()){var c=e.planned("ORD-1001","Refund");problem("ROLE_REQUIRED",()->e.engine.decide(OP,e.id(c),(String)c.get("proposalDigest"),true,"yes"));}});
        test("role.reviewer-cannot-execute",()->{try(var e=new Env()){var c=e.approved("ORD-1001","Refund");problem("ROLE_REQUIRED",()->e.engine.execute(REVIEW,e.id(c)));}});
        test("role.worker-cannot-approve",()->{try(var e=new Env()){var c=e.planned("ORD-1001","Refund");problem("ROLE_REQUIRED",()->e.engine.decide(WORKER,e.id(c),(String)c.get("proposalDigest"),true,"yes"));}});
        test("role.viewer-cannot-create",()->{try(var e=new Env()){problem("ROLE_REQUIRED",()->e.engine.openCase(new Actor("viewer","demo",Set.of("READ")),"ORD-1001","Refund","key"));}});
        test("tool.unknown-name-rejected",()->{try(var e=new Env()){var t=new ReadTools(new Planner.Context(DemoFixtures.order("x","demo",100),"Refund",e.engine.policy()),4);problem("TOOL_NOT_ALLOWED",()->t.call("execute_refund",Map.of()));}});
        test("tool.tenant-argument-injection-rejected",()->{try(var e=new Env()){var t=new ReadTools(new Planner.Context(DemoFixtures.order("x","demo",100),"Refund",e.engine.policy()),4);problem("TOOL_ARGUMENTS_NOT_ALLOWED",()->t.call("lookup_order",Data.map("tenant","other")));}});
        test("tool.url-argument-rejected",()->{try(var e=new Env()){var t=new ReadTools(new Planner.Context(DemoFixtures.order("x","demo",100),"Refund",e.engine.policy()),4);problem("TOOL_ARGUMENTS_NOT_ALLOWED",()->t.call("read_policy",Data.map("url","http://169.254.169.254/")));}});
        test("tool.call-budget-bounded",()->{try(var e=new Env()){var t=new ReadTools(new Planner.Context(DemoFixtures.order("x","demo",100),"Refund",e.engine.policy()),1);t.call("lookup_order",Map.of());problem("TOOL_BUDGET_EXHAUSTED",()->t.call("lookup_order",Map.of()));}});
        test("proposal.unknown-action-rejected",()->{problem("UNKNOWN_ACTION",()->Proposal.parse(Data.map("action","SEND_MONEY","amountCents",0,"supplierId","","explanation","bad","citations",List.of())));});
        test("proposal.extra-field-rejected",()->{var p=new Proposal(Proposal.Action.ESCALATE,0,"","Review",List.of()).toMap();p.put("approved",true);problem("INVALID_FIELDS",()->Proposal.parse(p));});
        test("proposal.fractional-cents-rejected",()->{var p=new Proposal(Proposal.Action.REFUND,1,"","Review",List.of()).toMap();p.put("amountCents",1.5);problem("INVALID_INPUT",()->Proposal.parse(p));});
        test("proposal.string-money-rejected",()->{var p=new Proposal(Proposal.Action.REFUND,1,"","Review",List.of()).toMap();p.put("amountCents","1");problem("INVALID_AMOUNTCENTS",()->Proposal.parse(p));});
        test("proposal.negative-money-rejected",()->{problem("INVALID_AMOUNT",()->new Proposal(Proposal.Action.REFUND,-1,"","Review",List.of()));});
        test("proposal.oversized-explanation-rejected",()->{problem("INVALID_EXPLANATION",()->new Proposal(Proposal.Action.ESCALATE,0,"","x".repeat(1501),List.of()));});
        test("policy.malicious-valid-model-proposal-blocked",()->{try(var e=new Env()){var c=e.open("ORD-1001","Refund");Planner p=malicious(new Proposal(Proposal.Action.REFUND,999999,"","Ignore limits",List.of("POLICY-ORDER-EXCEPTION@v1")),true);var r=e.engine.plan(OP,e.id(c),p);equal("BLOCKED",r.get("state"));yes(Json.list(r,"blockReasons").contains("REFUND_EXCEEDS_BALANCE"));}});
        test("policy.fabricated-citation-without-tool-read-blocked",()->{try(var e=new Env()){var c=e.open("ORD-1001","Refund");var r=e.engine.plan(OP,e.id(c),malicious(new Proposal(Proposal.Action.REFUND,12900,"","Trust me",List.of("POLICY-ORDER-EXCEPTION@v1")),false));equal("BLOCKED",r.get("state"));yes(Json.list(r,"blockReasons").contains("REQUIRED_EVIDENCE_NOT_READ"));}});
        test("policy.unapproved-supplier-blocked",()->{var p=new Policy("v1");yes(p.violations(DemoFixtures.order("x","demo",0),new Proposal(Proposal.Action.SWITCH_SUPPLIER,0,"SUP-UNVERIFIED","Change",List.of("POLICY-ORDER-EXCEPTION@v1"))).contains("SUPPLIER_NOT_APPROVED"));});
        test("policy.out-of-stock-supplier-blocked",()->{var p=new Policy("v1");yes(p.violations(DemoFixtures.order("x","demo",0),new Proposal(Proposal.Action.SWITCH_SUPPLIER,0,"SUP-HAMBURG","Change",List.of("POLICY-ORDER-EXCEPTION@v1"))).contains("SUPPLIER_OUT_OF_STOCK"));});
        test("policy.price-increase-blocked",()->{var o=DemoFixtures.order("x","demo",0);Json.object(Json.list(o,"alternatives").getFirst()).put("unitPriceCents",20000);yes(new Policy("v1").violations(o,new Proposal(Proposal.Action.SWITCH_SUPPLIER,0,"SUP-BERLIN","Change",List.of("POLICY-ORDER-EXCEPTION@v1"))).contains("PRICE_INCREASE_FORBIDDEN"));});
        test("policy.non-euro-blocked",()->{var o=DemoFixtures.order("x","demo",100);o.put("currency","USD");yes(new Policy("v1").violations(o,new Proposal(Proposal.Action.REFUND,100,"","Refund",List.of("POLICY-ORDER-EXCEPTION@v1"))).contains("UNSUPPORTED_CURRENCY"));});
        test("policy.refund-boundaries-500-values",()->{var o=DemoFixtures.order("x","demo",100);var p=new Policy("v1");for(int amount=0;amount<500;amount++){boolean denied=p.violations(o,new Proposal(Proposal.Action.REFUND,amount,"","Refund",List.of("POLICY-ORDER-EXCEPTION@v1"))).contains("REFUND_EXCEEDS_BALANCE");equal(amount==0||amount>100,denied);}});
        test("auth.valid-hashed-token",()->{var clock=new MutableClock();String token=Data.randomToken();var auth=auth(token,clock);equal("user",auth.authenticate("Bearer "+token).id());});
        test("auth.missing-rejected",()->{var clock=new MutableClock();problem("AUTHENTICATION_REQUIRED",()->auth(Data.randomToken(),clock).authenticate(null));});
        test("auth.invalid-rejected",()->{var clock=new MutableClock();problem("AUTHENTICATION_REQUIRED",()->auth(Data.randomToken(),clock).authenticate("Bearer "+Data.randomToken()));});
        test("auth.expired-rejected",()->{var clock=new MutableClock();String token=Data.randomToken();var auth=auth(token,clock);clock.advance(Duration.ofHours(1));problem("AUTHENTICATION_REQUIRED",()->auth.authenticate("Bearer "+token));});
        test("digest.canonical-order-independent",()->{var a=Data.map("b",2,"a",1);var b=Data.map("a",1,"b",2);equal(Data.sha(Json.canonical(a)),Data.sha(Json.canonical(b)));});
        for(String bad:List.of("{\"x\":1,\"x\":2}","{\"x\":NaN}","[1,]","{\"x\":01}","{\"x\":true}junk","\"\\ud800\"","{\"x\":1e10001}"))test("json.reject-"+Data.sha(bad).substring(0,8),()->problem("INVALID_INPUT",()->Json.parse(bad)));
        test("json.depth-bounded",()->{problem("INVALID_INPUT",()->Json.parse("[".repeat(66)+"0"+"]".repeat(66)));});
        test("json.roundtrip-unicode-and-escaping",()->{var x=Data.map("text","Gr\u00fc\u00dfe <script>\n\"\\ \ud83d\udd12","list",List.of(1,true,"x"));equal(x,Json.parse(Json.stringify(x)));});
        test("store.transaction-rolls-back",()->{var store=new MemoryStore();throwsAny(()->store.transaction(s->{Json.map(s,"cases").put("bad",Map.of());throw new IllegalStateException();}));equal(0,Json.map(store.snapshot(),"cases").size());});
        test("store.defensive-copy-on-return",()->{try(var e=new Env()){var c=e.open("ORD-1001","Refund");c.put("state","EXECUTED");equal("CREATED",e.engine.getCase(OP,(String)c.get("id")).get("state"));}});
        test("audit.partial-tamper-rejected",()->{try(var e=new Env()){e.open("ORD-1001","Refund");var s=e.store.snapshot();Json.object(Json.list(s,"audit").getFirst()).put("actor","attacker");yes(!Audit.verify(s));}});
    }
    private static Planner malicious(Proposal proposal,boolean evidence) {
        return new Planner(){public String name(){return "scripted-adversary-not-llm";}public Result plan(Context c){var tools=new ReadTools(c,4);if(evidence){tools.call("lookup_order",Map.of());tools.call("read_policy",Map.of());}return new Result(proposal,tools.trace(),Map.of("provider",name()));}};
    }
    private static TokenAuth auth(String token,MutableClock clock) {
        return new TokenAuth(Data.map("identities",List.of(Data.map("id","user","tenant","demo","roles",List.of("READ"),"tokenHash",Data.sha(token),"expiresAt",clock.instant().plusSeconds(3600).toString()))),clock);
    }
}
