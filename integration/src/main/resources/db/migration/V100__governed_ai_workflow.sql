-- Additive portfolio reference: independent sandbox aggregate, not the existing orders/payment tables.
CREATE TABLE governance_state (
    id SMALLINT PRIMARY KEY CHECK (id = 1),
    payload JSONB NOT NULL CHECK (jsonb_typeof(payload) = 'object'),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE governance_state IS 'Sandbox governance aggregate. SELECT FOR UPDATE serializes writes. Not a scalable production event store.';
