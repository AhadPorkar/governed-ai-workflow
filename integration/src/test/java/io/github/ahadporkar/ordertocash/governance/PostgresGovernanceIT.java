package io.github.ahadporkar.ordertocash.governance;
import java.sql.DriverManager;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.postgresql.PostgreSQLContainer;
import static org.junit.jupiter.api.Assertions.*;
/** Real Postgres acceptance gate. Authored here; NOT EXECUTED in the source-delivery environment. */
@Testcontainers
class PostgresGovernanceIT {
    @Container static PostgreSQLContainer postgres=new PostgreSQLContainer("postgres:16-alpine");
    GovernanceEngine engine;JdbcStateStore store;
    final Actor operator=new Actor("operator","demo",Set.of("READ","OPERATOR"));
    final Actor reviewer=new Actor("reviewer","demo",Set.of("READ","REVIEWER"));
    final Actor worker=new Actor("worker","demo",Set.of("READ","WORKER"));
    @BeforeEach void setup() throws Exception {
        try(var c=DriverManager.getConnection(postgres.getJdbcUrl(),postgres.getUsername(),postgres.getPassword());var s=c.createStatement()) {
            s.execute("DROP TABLE IF EXISTS governance_state");
            s.execute("CREATE TABLE governance_state(id smallint primary key check(id=1),payload jsonb not null,updated_at timestamptz default current_timestamp)");
        }
        var ds=new PGSimpleDataSource();ds.setURL(postgres.getJdbcUrl());ds.setUser(postgres.getUsername());ds.setPassword(postgres.getPassword());
        store=new JdbcStateStore(ds);engine=new GovernanceEngine(store);DemoFixtures.seed(engine,"demo");
    }
    String approved() {
        var c=engine.openCase(operator,"ORD-1001","Please refund my unavailable order.","postgres-case");String id=Json.text(c,"id","");
        c=engine.plan(operator,id,new DeterministicPlanner());engine.decide(reviewer,id,Json.text(c,"proposalDigest",""),true,"Evidence reviewed");return id;
    }
    @Test void parallelRetriesCommitOnlyOneReceipt()throws Exception {
        String id=approved();var receipts=new HashSet<String>();
        try(var pool=Executors.newFixedThreadPool(8)) {
            var calls=new ArrayList<Future<Map<String,Object>>>();for(int i=0;i<16;i++)calls.add(pool.submit(()->engine.execute(worker,id)));
            for(var result:calls)receipts.add(Json.text(result.get(30,TimeUnit.SECONDS),"id",""));
        }
        assertEquals(1,receipts.size());assertEquals(1,Json.map(store.snapshot(),"effects").size());
    }
    @Test void thrownTransactionRollsBack() {
        var before=Json.canonical(store.snapshot());
        assertThrows(IllegalStateException.class,()->store.transaction(s->{Json.map(s,"orders").clear();throw new IllegalStateException("controlled failure");}));
        assertEquals(before,Json.canonical(store.snapshot()));
    }
    @Test void reconstructionReplaysCommittedReceipt() {
        String id=approved();var receipt=engine.execute(worker,id);var reconstructed=new GovernanceEngine(store);
        assertEquals(receipt,reconstructed.execute(worker,id));assertEquals(1,Json.map(store.snapshot(),"effects").size());
    }
}
