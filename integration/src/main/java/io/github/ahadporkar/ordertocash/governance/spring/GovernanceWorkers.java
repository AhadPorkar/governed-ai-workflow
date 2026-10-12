package io.github.ahadporkar.ordertocash.governance.spring;
import io.camunda.client.annotation.JobWorker;
import io.camunda.client.annotation.Variable;
import io.camunda.client.exception.CamundaError;
import io.github.ahadporkar.ordertocash.governance.*;
import java.util.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
/** Camunda is a trusted control plane; engine still verifies every persisted authorization. */
@Component
@ConditionalOnProperty(name="app.governance.enabled",havingValue="true")
public class GovernanceWorkers {
    private final GovernanceEngine engine;private final Planner planner;
    public GovernanceWorkers(GovernanceEngine engine,Planner planner){this.engine=engine;this.planner=planner;}
    private static Actor actor(String tenant){return new Actor("camunda-worker",tenant,Set.of("WORKER","READ"));}
    @JobWorker(type="governance-plan")
    public Map<String,Object> plan(@Variable(name="caseId") String caseId,@Variable(name="tenant") String tenant) {
        var c=engine.plan(actor(tenant),caseId,planner);
        return Map.of("outcome",c.get("state"));
    }
    @JobWorker(type="governance-execute")
    public Map<String,Object> execute(@Variable(name="caseId") String caseId,@Variable(name="tenant") String tenant) {
        try {
            var receipt=engine.execute(actor(tenant),caseId);
            engine.acknowledge(actor(tenant),caseId,Json.text(receipt,"id",""));
            return Map.of("receiptId",receipt.get("id"),"sandbox",true);
        }catch(Problem p) {
            if(p.status==409)throw CamundaError.bpmnError("GOV_AUTHORIZATION_INVALID",p.code);
            throw p;
        }
    }
    @JobWorker(type="governance-expire")
    public Map<String,Object> expire(@Variable(name="caseId") String caseId,@Variable(name="tenant") String tenant) {
        return Map.of("outcome",engine.expire(actor(tenant),caseId).get("state"));
    }
}
