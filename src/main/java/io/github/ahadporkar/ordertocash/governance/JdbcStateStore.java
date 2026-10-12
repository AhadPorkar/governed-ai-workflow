package io.github.ahadporkar.ordertocash.governance;
import javax.sql.DataSource;
import java.sql.*;
import java.util.Map;
import java.util.function.Function;
/** PostgreSQL reference adapter. One locked aggregate intentionally favors correctness over throughput. */
public final class JdbcStateStore implements StateStore {
    private final DataSource source;
    public JdbcStateStore(DataSource source) { this.source=source; }
    @Override public Map<String,Object> transaction(Function<Map<String,Object>,Map<String,Object>> work) {
        try(var connection=source.getConnection()) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            try {
                try(var init=connection.prepareStatement("INSERT INTO governance_state (id,payload) VALUES (1,CAST(? AS jsonb)) ON CONFLICT (id) DO NOTHING")) {
                    init.setString(1,Json.stringify(StateStore.empty()));init.executeUpdate();
                }
                Map<String,Object> state;
                try(var query=connection.prepareStatement("SELECT payload::text FROM governance_state WHERE id=1 FOR UPDATE")) {
                    try(var rows=query.executeQuery()) { if(!rows.next())throw new SQLException("Missing governance aggregate");state=Json.object(Json.parseStored(rows.getString(1))); }
                }
                StateStore.validate(state);var result=Data.copy(work.apply(state));StateStore.validate(state);
                try(var update=connection.prepareStatement("UPDATE governance_state SET payload=CAST(? AS jsonb),updated_at=CURRENT_TIMESTAMP WHERE id=1")) {
                    update.setString(1,Json.stringify(state));update.executeUpdate();
                }
                connection.commit();return result;
            } catch(RuntimeException|SQLException e) { connection.rollback();throw e; }
        } catch(SQLException e) { throw new IllegalStateException("Governance transaction unavailable",e); }
    }
    @Override public Map<String,Object> snapshot() {
        // A read takes the same lock to give an internally consistent snapshot.
        return transaction(Data::copy);
    }
}
