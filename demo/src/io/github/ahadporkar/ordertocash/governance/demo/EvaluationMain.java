package io.github.ahadporkar.ordertocash.governance.demo;
import io.github.ahadporkar.ordertocash.governance.*;
import java.nio.file.*;
import java.net.URI;
import java.time.Instant;
import java.util.*;
/** Local, labelled proposal evaluation. No actions are approved or executed by this runner. */
public final class EvaluationMain {
    public static void main(String[] args)throws Exception {
        Path input=Path.of(args.length>0?args[0]:"evaluation/cases.json");
        Path output=Path.of(args.length>1?args[1]:"build/evaluation/baseline-results.json");
        String provider=args.length>2?args[2]:"baseline";
        Planner planner=switch(provider){case "baseline"->new DeterministicPlanner();case "ollama"->new OllamaPlanner(URI.create(args[3]),args[4]);default->throw new IllegalArgumentException("Unknown provider");};
        byte[] source=Files.readAllBytes(input);var dataset=Json.obj(new String(source,java.nio.charset.StandardCharsets.UTF_8));
        var results=new ArrayList<Map<String,Object>>();int exact=0,disposition=0;long calls=0,tokens=0;var times=new ArrayList<Double>();
        for(Object item:Json.list(dataset,"cases")) {
            var row=Json.object(item);long start=System.nanoTime();var engine=new GovernanceEngine(new MemoryStore());
            var order=DemoFixtures.order("ORD-EVAL","demo",12900);order.putAll(Json.map(row,"orderOverrides"));
            engine.registerOrder(new Actor("admin","demo",Set.of("ADMIN")),order,false);
            var actor=new Actor("operator","demo",Set.of("OPERATOR","READ"));
            var c=engine.openCase(actor,"ORD-EVAL",Json.text(row,"request",""),Json.text(row,"id",""));c=engine.plan(actor,Json.text(c,"id",""),planner);
            var expected=Json.map(row,"expected");var proposal=Json.map(c,"proposal");
            boolean match=Objects.equals(expected.get("action"),proposal.get("action"))&&Json.number(expected,"amountCents",-1)==Json.number(proposal,"amountCents",-2)&&Objects.equals(expected.get("supplierId"),proposal.get("supplierId"));
            boolean state=Json.list(expected,"acceptableStates").contains(c.get("state"));
            if(match)exact++;if(state)disposition++;
            var usage=Json.map(c,"usage");calls+=Json.number(usage,"modelCalls",0);tokens+=Json.number(usage,"inputTokens",0)+Json.number(usage,"outputTokens",0);
            double elapsed=(System.nanoTime()-start)/1e6;times.add(elapsed);
            results.add(Data.map("id",row.get("id"),"language",row.get("language"),"category",row.get("category"),"request",row.get("request"),"expected",expected,"actualProposal",proposal,"actualState",c.get("state"),"exactProposalMatch",match,"acceptableDisposition",state,"latencyMs",elapsed,"usage",usage,"failure",c.get("plannerFailure")));
        }
        Collections.sort(times);int n=results.size();
        var summary=Data.map("runAt",Instant.now().toString(),"datasetVersion",dataset.get("datasetVersion"),"datasetSha256",Data.sha(new String(source,java.nio.charset.StandardCharsets.UTF_8)),"provider",planner.name(),"caseCount",n,"exactProposalMatches",exact,"exactProposalMatchRate",exact/(double)n,"acceptableDispositions",disposition,"acceptableDispositionRate",disposition/(double)n,"modelCalls",calls,"observedTokens",tokens,"estimatedCostUsd",null,"costStatus",calls==0?"not-applicable-no-model":"not-configured","p50EndToEndMs",times.get((int)Math.ceil(n*.50)-1),"p95EndToEndMs",times.get((int)Math.ceil(n*.95)-1),"javaVersion",System.getProperty("java.version"),"os",System.getProperty("os.name"),"availableProcessors",Runtime.getRuntime().availableProcessors(),"effectsExecuted",0,"caveat","Single-process synthetic proposal benchmark, no warmup or load isolation. Not real-model quality or business-task success.","results",results);
        Files.createDirectories(output.toAbsolutePath().getParent());Files.writeString(output,Json.stringify(summary));
        System.out.println("Evaluated "+n+" labelled cases: exact proposals "+exact+"/"+n+", accepted dispositions "+disposition+"/"+n+", model calls "+calls+", effects executed 0. Results: "+output);
    }
}
