package io.github.ahadporkar.ordertocash.governance;
import java.util.*;
public record Proposal(Action action,long amountCents,String supplierId,String explanation,List<String> citations) {
    public enum Action { REFUND,CANCEL,SWITCH_SUPPLIER,ESCALATE }
    public Proposal {
        Objects.requireNonNull(action);Objects.requireNonNull(supplierId);Objects.requireNonNull(explanation);
        Problem.require(amountCents>=0&&amountCents<=100_000_000,400,"INVALID_AMOUNT");
        Problem.require(supplierId.isEmpty()||supplierId.matches("[A-Za-z0-9._-]{1,64}"),400,"INVALID_SUPPLIER");
        Problem.require(!explanation.isBlank()&&explanation.length()<=1500,400,"INVALID_EXPLANATION");
        Problem.require(citations!=null&&citations.size()<=8&&citations.stream().allMatch(c->c!=null&&c.length()<=80),400,"INVALID_CITATIONS");
        citations=List.copyOf(citations);
    }
    public static Proposal parse(Map<String,Object> m) {
        Data.keys(m,"action","amountCents","supplierId","explanation","citations");
        Action action;try {action=Action.valueOf(Data.str(m,"action",30));}catch(IllegalArgumentException e){throw new Problem(400,"UNKNOWN_ACTION");}
        Problem.require(m.get("supplierId") instanceof String,400,"INVALID_SUPPLIER");
        var citations=new ArrayList<String>();
        for(Object v:Json.array(m.get("citations"))) { Problem.require(v instanceof String,400,"INVALID_CITATIONS");citations.add((String)v); }
        return new Proposal(action,Data.integer(m,"amountCents",0,100_000_000),(String)m.get("supplierId"),Data.str(m,"explanation",1500),citations);
    }
    public Map<String,Object> toMap() { return Data.map("action",action.name(),"amountCents",amountCents,"supplierId",supplierId,"explanation",explanation,"citations",citations); }
}
