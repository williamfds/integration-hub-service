CREATE TABLE orders (
    id              UUID            PRIMARY KEY,
    platform        VARCHAR(32)     NOT NULL,
    external_id     VARCHAR(128)    NOT NULL,
    status          VARCHAR(32)     NOT NULL,
    total_amount    NUMERIC(19, 4)  NOT NULL,
    currency        VARCHAR(3)      NOT NULL,
    customer_name   VARCHAR(255),
    customer_email  VARCHAR(255),
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT uq_orders_platform_external UNIQUE (platform, external_id)
);

CREATE INDEX idx_orders_platform_status ON orders (platform, status);
CREATE INDEX idx_orders_created_at      ON orders (created_at DESC);
