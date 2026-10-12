package io.github.ahadporkar.ordertocash.governance.spring;
import io.camunda.client.CamundaClient;
import io.github.ahadporkar.ordertocash.governance.*;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
/** At-least-once delivery. Consumers deduplicate stable event IDs. Not a distributed transaction. */
@Component
@ConditionalOnProperty(name="app.governance.enabled",havingValue="true")
public class GovernanceOutboxRelay {
    private static final Logger LOG=LoggerFactory.getLogger(GovernanceOutboxRelay.class);
    private final GovernanceEngine engine;private final CamundaClient client;private final KafkaTemplate<String,String> kafka;private final String topic;
    public GovernanceOutboxRelay(GovernanceEngine engine,CamundaClient client,KafkaTemplate<String,String> kafka,
            @Value("${app.governance.events-topic:governance.events}") String topic) {
        this.engine=engine;this.client=client;this.kafka=kafka;this.topic=topic;
    }
    @Scheduled(fixedDelayString="${app.governance.outbox-interval-ms:1000}")
    public void publish() {
        for(var event:engine.pendingEvents(20)) {
            String id=Json.text(event,"id","");
            try {
                kafka.send(topic,Json.text(event,"caseId",""),Json.stringify(event)).get(10,TimeUnit.SECONDS);
                String type=Json.text(event,"type","");
                if("GovernanceCaseOpened".equals(type)||"GovernanceDecisionRecorded".equals(type))
                    client.newPublishMessageCommand().messageName(type).correlationKey(Json.text(event,"caseId",""))
                            .messageId(id).timeToLive(Duration.ofDays(1)).variables(Json.map(event,"payload")).send().get(10,TimeUnit.SECONDS);
                engine.acknowledgeEvent(id);
            }catch(InterruptedException e){Thread.currentThread().interrupt();return;}
            catch(Exception e){LOG.warn("Governance outbox retry pending for event {}; no payload logged",id);return;}
        }
    }
}
