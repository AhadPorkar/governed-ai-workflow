package io.github.ahadporkar.ordertocash.governance;
import java.time.Instant;
import java.util.*;
/** Hash chaining detects accidental or partial modification, not an attacker rewriting the entire chain. */
public final class Audit {
    private Audit() {}
    public static void append(Map<String,Object> state,Instant time,String tenant,String caseId,String actor,String event,Map<String,Object> details) {
        List<Object> rows=Json.list(state,"audit");
        String prev=rows.isEmpty()?"GENESIS":Json.text(Json.object(rows.getLast()),"hash","");
        var row=Data.map("sequence",rows.size()+1,"time",time.toString(),"tenant",tenant,"caseId",caseId,
                "actor",actor,"event",event,"details",details,"previousHash",prev);
        row.put("hash",Data.sha(Json.canonical(row)));rows.add(row);state.put("audit",rows);
    }
    public static boolean verify(Map<String,Object> state) {
        String prev="GENESIS";int i=0;
        for(Object o:Json.list(state,"audit")) {
            var row=Data.copy(Json.object(o));String hash=Json.text(row,"hash","");row.remove("hash");
            if(Json.integer(row,"sequence",0)!=++i || !prev.equals(row.get("previousHash")) || !Data.equal(hash,Data.sha(Json.canonical(row))))return false;
            prev=hash;
        }
        return true;
    }
}
