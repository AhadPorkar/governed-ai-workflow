package io.github.ahadporkar.ordertocash.governance.spring;
import io.github.ahadporkar.ordertocash.governance.*;
import java.nio.file.*;
import java.time.*;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;
/** Opt-in reference integration. Does not secure or modify the host application's existing APIs. */
@Configuration
@ConditionalOnProperty(name="app.governance.enabled",havingValue="true")
public class GovernanceConfiguration {
    @Bean public GovernanceEngine governanceEngine(DataSource source,
            @Value("${app.governance.seed-demo:false}") boolean seed) {
        var engine=new GovernanceEngine(new JdbcStateStore(source));
        if(seed)DemoFixtures.seed(engine,"demo");
        return engine;
    }
    @Bean public Planner governancePlanner(
            @Value("${app.governance.planner:baseline}") String provider,
            @Value("${app.governance.ollama-endpoint:http://127.0.0.1:11434/api/chat}") String endpoint,
            @Value("${app.governance.ollama-model:}") String model) {
        if("baseline".equals(provider))return new DeterministicPlanner();
        if("ollama".equals(provider))return new OllamaPlanner(java.net.URI.create(endpoint),model);
        throw new IllegalArgumentException("Planner must be baseline or ollama");
    }
    @Bean public TokenAuth governanceTokenAuth(@Value("${app.governance.auth-file}") String file) throws Exception {
        Path path=Path.of(file);if(Files.size(path)>65536)throw new IllegalArgumentException("Auth file too large");
        return new TokenAuth(Json.object(Json.parse(Files.readString(path))),Clock.systemUTC());
    }
    @Bean public ApiRouter governanceApiRouter(GovernanceEngine engine,Planner planner,TokenAuth auth) {
        return new ApiRouter(engine,planner,auth,120);
    }
}
