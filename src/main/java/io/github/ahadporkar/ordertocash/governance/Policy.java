package io.github.ahadporkar.ordertocash.governance;
import java.util.*;
/** Deterministic authority. No prompt, model-provided confidence or browser field can bypass these checks. */
public final class Policy {
    private final String version;
    public Policy(String version) { this.version=Data.id(version); }
    public String version() { return version; }
    public Map<String,Object> document() {
        return Data.map("id","POLICY-ORDER-EXCEPTION","version",version,"rules",List.of(
                "All REFUND, CANCEL and SWITCH_SUPPLIER actions require an independent human reviewer.",
                "Money is integer EUR cents. Refund cannot exceed captured amount minus previous refunds.",
                "Cancellation requires an unshipped, unpaid order. Paid orders must be escalated or refunded separately.",
                "Supplier changes require an approved supplier with available stock and no higher price.",
                "An approval binds the tenant, case, order version, policy version, exact proposal, revision and expiry.",
                "Unclear intent, unavailable evidence and exhausted model budgets must escalate."));
    }
    public List<String> violations(Map<String,Object> order,Proposal p) {
        var reasons=new ArrayList<String>();
        if(p.action()==Proposal.Action.ESCALATE) {
            if(p.amountCents()!=0||!p.supplierId().isEmpty())reasons.add("ESCALATION_HAS_ARGUMENTS");return reasons;
        }
        if(!p.citations().contains("POLICY-ORDER-EXCEPTION@"+version))reasons.add("POLICY_CITATION_REQUIRED");
        if(!"EUR".equals(order.get("currency")))reasons.add("UNSUPPORTED_CURRENCY");
        String status=Json.text(order,"status","");
        if(Set.of("CANCELLED","REFUNDED").contains(status))reasons.add("ORDER_CLOSED");
        long captured=Json.number(order,"capturedCents",0),refunded=Json.number(order,"refundedCents",0);
        switch(p.action()) {
            case REFUND -> {
                if(p.amountCents()<=0||p.amountCents()>captured-refunded)reasons.add("REFUND_EXCEEDS_BALANCE");
                if(!p.supplierId().isEmpty())reasons.add("UNEXPECTED_SUPPLIER");
                if("SHIPPED".equals(status))reasons.add("SHIPPED_REQUIRES_RETURNS_PROCESS");
            }
            case CANCEL -> {
                if(!Set.of("UNFULFILLED","READY").contains(status))reasons.add("CANCELLATION_NOT_ALLOWED");
                if(captured-refunded>0)reasons.add("PAID_ORDER_REQUIRES_REFUND");
                if(p.amountCents()!=0||!p.supplierId().isEmpty())reasons.add("UNEXPECTED_ARGUMENTS");
            }
            case SWITCH_SUPPLIER -> {
                if(!"UNFULFILLED".equals(status))reasons.add("SUPPLIER_CHANGE_NOT_ALLOWED");
                if(p.amountCents()!=0)reasons.add("UNEXPECTED_AMOUNT");
                Map<String,Object> selected=null;
                for(Object x:Json.list(order,"alternatives")) {var a=Json.object(x);if(p.supplierId().equals(a.get("id")))selected=a;}
                if(selected==null)reasons.add("SUPPLIER_NOT_APPROVED");
                else {
                    if(!Json.bool(selected,"approved",false))reasons.add("SUPPLIER_NOT_APPROVED");
                    if(Json.number(selected,"stock",0)<Json.number(order,"quantity",1))reasons.add("SUPPLIER_OUT_OF_STOCK");
                    if(Json.number(selected,"unitPriceCents",Long.MAX_VALUE)>Json.number(order,"unitPriceCents",0))reasons.add("PRICE_INCREASE_FORBIDDEN");
                }
            }
            default -> {}
        }
        return List.copyOf(reasons);
    }
}
