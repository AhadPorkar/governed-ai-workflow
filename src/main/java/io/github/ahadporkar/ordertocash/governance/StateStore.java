package io.github.ahadporkar.ordertocash.governance;
import java.util.*;
import java.util.function.Function;
/** A callback is one serializable local transaction. Callbacks must not perform network I/O. */
public interface StateStore extends AutoCloseable {
    Map<String,Object> transaction(Function<Map<String,Object>,Map<String,Object>> work);
    Map<String,Object> snapshot();
    @Override default void close() {}
    static Map<String,Object> empty() {
        return Data.map("schemaVersion",1,"orders",new LinkedHashMap<String,Object>(),"cases",new LinkedHashMap<String,Object>(),
                "requests",new LinkedHashMap<String,Object>(),"effects",new LinkedHashMap<String,Object>(),
                "jobs",new LinkedHashMap<String,Object>(),"outbox",new LinkedHashMap<String,Object>(),"audit",new ArrayList<>());
    }
    static void validate(Map<String,Object> state) {
        Problem.require(Json.integer(state,"schemaVersion",0)==1 && Audit.verify(state),500,"STORAGE_INTEGRITY");
        for(String k:List.of("orders","cases","requests","effects","jobs","outbox"))Json.object(state.get(k));
    }
}
