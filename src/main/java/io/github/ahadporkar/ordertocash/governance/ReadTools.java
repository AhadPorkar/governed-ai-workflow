package io.github.ahadporkar.ordertocash.governance;
import java.util.*;
/** Scoped to one authoritative case snapshot. The model cannot select a tenant, order ID or URL. */
public final class ReadTools {
    public static final List<String> NAMES=List.of("lookup_order","check_inventory","read_policy","list_alternatives");
    private final Planner.Context context;
    private final int limit;
    private final List<Map<String,Object>> trace=new ArrayList<>();
    public ReadTools(Planner.Context context,int limit) { this.context=context;this.limit=limit; }
    public Map<String,Object> call(String name,Map<String,Object> arguments) {
        Problem.require(NAMES.contains(name),422,"TOOL_NOT_ALLOWED");
        Problem.require(arguments.isEmpty(),422,"TOOL_ARGUMENTS_NOT_ALLOWED");
        Problem.require(trace.size()<limit,422,"TOOL_BUDGET_EXHAUSTED");
        Map<String,Object> order=context.order();
        Map<String,Object> result=switch(name) {
            case "lookup_order" -> Data.map("id",order.get("id"),"version",order.get("version"),"status",order.get("status"),
                    "currency",order.get("currency"),"totalCents",order.get("totalCents"),"capturedCents",order.get("capturedCents"),"refundedCents",order.get("refundedCents"));
            case "check_inventory" -> Data.map("sku",order.get("sku"),"quantity",order.get("quantity"),"originalStock",order.get("originalStock"));
            case "read_policy" -> context.policy().document();
            case "list_alternatives" -> Data.map("alternatives",order.get("alternatives"),"maximumUnitPriceCents",order.get("unitPriceCents"));
            default -> throw new Problem(422,"TOOL_NOT_ALLOWED");
        };
        trace.add(Data.map("sequence",trace.size()+1,"tool",name,"arguments",arguments,"result",Data.copy(result),"resultHash",Data.sha(Json.canonical(result))));
        return Data.copy(result);
    }
    public List<Map<String,Object>> trace() { return trace.stream().map(Data::copy).toList(); }
    public static List<Map<String,Object>> definitions() {
        return NAMES.stream().map(name->Data.map("type","function","function",Data.map("name",name,
                "description","Read authoritative "+name+" for the current case only. No write effects.",
                "parameters",Data.map("type","object","properties",Map.of(),"required",List.of(),"additionalProperties",false)))).toList();
    }
}
