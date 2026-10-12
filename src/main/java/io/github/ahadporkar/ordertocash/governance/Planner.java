package io.github.ahadporkar.ordertocash.governance;
import java.util.*;
public interface Planner {
    record Context(Map<String,Object> order,String request,Policy policy) {
        public Context { order=Data.copy(order); }
    }
    record Result(Proposal proposal,List<Map<String,Object>> trace,Map<String,Object> usage) {
        public Result { trace=trace.stream().map(Data::copy).toList();usage=Data.copy(usage); }
    }
    Result plan(Context context);
    String name();
}
