package io.github.ahadporkar.ordertocash.governance;
import java.util.Map;
import java.util.function.Function;
public final class MemoryStore implements StateStore {
    private Map<String,Object> state=StateStore.empty();
    @Override public synchronized Map<String,Object> transaction(Function<Map<String,Object>,Map<String,Object>> work) {
        var draft=Data.copy(state);var result=Data.copy(work.apply(draft));StateStore.validate(draft);state=Data.copy(draft);return result;
    }
    @Override public synchronized Map<String,Object> snapshot() { return Data.copy(state); }
}
