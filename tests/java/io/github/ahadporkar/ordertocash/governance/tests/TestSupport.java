package io.github.ahadporkar.ordertocash.governance.tests;
import io.github.ahadporkar.ordertocash.governance.*;
import java.time.*;
import java.nio.file.*;
import java.util.*;
public final class TestSupport {
    @FunctionalInterface public interface Check { void run() throws Exception; }
    private static final List<Map<String,Object>> RESULTS=new ArrayList<>();
    public static void test(String name,Check check) {
        long start=System.nanoTime();String error=null;
        try {check.run();}catch(Throwable e){error=e.toString();e.printStackTrace(System.err);}
        RESULTS.add(Data.map("name",name,"passed",error==null,"seconds",(System.nanoTime()-start)/1e9,"error",error));
        System.out.println((error==null?"PASS ":"FAIL ")+name);
    }
    public static void equal(Object expected,Object actual) {
        if(!Json.canonical(expected).equals(Json.canonical(actual)))throw new AssertionError("Expected "+expected+", actual "+actual);
    }
    public static void yes(boolean condition) {if(!condition)throw new AssertionError("Condition failed");}
    public static Problem problem(String code,Check action) throws Exception {
        try {action.run();}catch(Problem p){equal(code,p.code);return p;}throw new AssertionError("Expected "+code);
    }
    public static void throwsAny(Check action) throws Exception {
        try {action.run();}catch(Exception e){return;}throw new AssertionError("Expected exception");
    }
    public static long finish(Path directory) throws Exception {
        Files.createDirectories(directory);Files.writeString(directory.resolve("java-tests.json"),Json.stringify(Data.map("tests",RESULTS,"failed",RESULTS.stream().filter(r->!Json.bool(r,"passed",false)).count())));
        StringBuilder xml=new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><testsuite name=\"governance-java\" tests=\"").append(RESULTS.size()).append("\" failures=\"").append(RESULTS.stream().filter(r->!Json.bool(r,"passed",false)).count()).append("\">");
        for(var r:RESULTS){xml.append("<testcase classname=\"GovernanceContract\" name=\"").append(escape((String)r.get("name"))).append("\" time=\"").append(r.get("seconds")).append("\">");if(!Json.bool(r,"passed",false))xml.append("<failure message=\"").append(escape((String)r.get("error"))).append("\"/>");xml.append("</testcase>");}
        xml.append("</testsuite>");Files.writeString(directory.resolve("java-tests.xml"),xml);
        return RESULTS.stream().filter(r->!Json.bool(r,"passed",false)).count();
    }
    private static String escape(String s) {return s.replace("&","&amp;").replace("<","&lt;").replace("\"","&quot;");}
    public static final class MutableClock extends Clock {
        private Instant now=Instant.parse("2026-10-12T10:00:00Z");
        public void advance(Duration d){now=now.plus(d);}
        @Override public java.time.ZoneId getZone(){return ZoneOffset.UTC;}
        @Override public Clock withZone(java.time.ZoneId zone){return this;}
        @Override public Instant instant(){return now;}
    }
    public static final Actor OP=new Actor("operator","demo",Set.of("READ","OPERATOR"));
    public static final Actor REVIEW=new Actor("reviewer","demo",Set.of("READ","REVIEWER"));
    public static final Actor WORKER=Actor.worker("demo");
    public static final Actor ADMIN=new Actor("admin","demo",Set.of("ADMIN"));
    public static final Actor OTHER=new Actor("other","other",Set.of("READ","OPERATOR","REVIEWER","WORKER"));
    public static final class Env implements AutoCloseable {
        public final StateStore store;public final MutableClock clock=new MutableClock();public final GovernanceEngine engine;
        public Env(){this(new MemoryStore());}
        public Env(StateStore store){this.store=store;engine=new GovernanceEngine(store,new Policy("v1"),clock,Duration.ofMinutes(15));DemoFixtures.seed(engine,"demo");DemoFixtures.seed(engine,"other");}
        public Map<String,Object> open(String order,String text){return engine.openCase(OP,order,text,UUID.randomUUID().toString());}
        public Map<String,Object> planned(String order,String text){var c=open(order,text);return engine.plan(OP,(String)c.get("id"),new DeterministicPlanner());}
        public Map<String,Object> approved(String order,String text){var c=planned(order,text);return engine.decide(REVIEW,(String)c.get("id"),(String)c.get("proposalDigest"),true,"Reviewed the evidence and exact action.");}
        public String id(Map<String,Object> c){return (String)c.get("id");}
        public Map<String,Object> current(Map<String,Object> c){return engine.getCase(OP,id(c));}
        @Override public void close(){store.close();}
    }
}
