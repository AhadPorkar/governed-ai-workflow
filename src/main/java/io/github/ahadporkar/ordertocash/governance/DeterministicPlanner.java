package io.github.ahadporkar.ordertocash.governance;
import java.util.*;
/** Transparent keyword baseline, not an LLM and not a semantic intent detector. Ambiguity abstains. */
public final class DeterministicPlanner implements Planner {
    @Override public String name() { return "deterministic-baseline"; }
    @Override public Result plan(Context c) {
        long started=System.nanoTime();var tools=new ReadTools(c,4);
        var order=tools.call("lookup_order",Map.of());tools.call("check_inventory",Map.of());
        tools.call("read_policy",Map.of());var alternatives=tools.call("list_alternatives",Map.of());
        String s=c.request().toLowerCase(Locale.ROOT);
        boolean refund=contains(s,"refund","money back","erstattung","erstatten","geld zur");
        boolean cancel=contains(s,"cancel","stornier","storno");
        boolean supplier=contains(s,"alternative","another supplier","different supplier","anderen lieferanten","anderer lieferant");
        boolean unsafe=contains(s,"ignore","bypass","system prompt","skip approval","without approval","ohne freigabe","nicht ","do not ","don't ");
        int intents=(refund?1:0)+(cancel?1:0)+(supplier?1:0);
        Proposal p;
        String citation="POLICY-ORDER-EXCEPTION@"+c.policy().version();
        if(unsafe||intents!=1) p=new Proposal(Proposal.Action.ESCALATE,0,"","The baseline cannot establish one unambiguous request. Human investigation is required.",List.of(citation));
        else if(refund) p=new Proposal(Proposal.Action.REFUND,Math.max(0,Json.number(order,"capturedCents",0)-Json.number(order,"refundedCents",0)),"","Propose returning the remaining captured balance, subject to policy and independent approval.",List.of(citation));
        else if(cancel) p=new Proposal(Proposal.Action.CANCEL,0,"","Propose cancelling this unfulfilled order, subject to payment-state checks and independent approval.",List.of(citation));
        else {
            String id="";
            for(Object v:Json.list(alternatives,"alternatives")) {
                var a=Json.object(v);
                if(Json.bool(a,"approved",false)&&Json.number(a,"stock",0)>=Json.number(c.order(),"quantity",1)&&Json.number(a,"unitPriceCents",Long.MAX_VALUE)<=Json.number(c.order(),"unitPriceCents",0)) {id=Json.text(a,"id","");break;}
            }
            p=id.isEmpty()?new Proposal(Proposal.Action.ESCALATE,0,"","No approved, in-stock alternative satisfies the price policy.",List.of(citation)):
                    new Proposal(Proposal.Action.SWITCH_SUPPLIER,0,id,"Propose an approved, in-stock supplier without increasing the unit price. Independent approval is required.",List.of(citation));
        }
        return new Result(p,tools.trace(),Data.map("provider",name(),"modelCalls",0,"inputTokens",0,"outputTokens",0,
                "latencyMs",(System.nanoTime()-started)/1_000_000.0,"estimatedCostUsd",null,"costStatus","not-applicable-no-model"));
    }
    private static boolean contains(String s,String... words) { for(String w:words)if(s.contains(w))return true;return false; }
}
