package io.github.ahadporkar.ordertocash.governance;
import java.util.Set;
/** Constructed only by a trusted transport adapter, never from request JSON or model output. */
public record Actor(String id,String tenant,Set<String> roles) {
    public Actor { Data.id(id);Data.id(tenant);roles=Set.copyOf(roles); }
    public void require(String role) { Problem.require(roles.contains(role),403,"ROLE_REQUIRED"); }
    public void tenant(String value) { Problem.require(tenant.equals(value),404,"NOT_FOUND"); }
    public static Actor worker(String tenant) { return new Actor("workflow-worker",tenant,Set.of("READ","WORKER")); }
}
