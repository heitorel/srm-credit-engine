CREATE TABLE currencies (
    code CHAR(3) NOT NULL,
    name VARCHAR(64) NOT NULL,
    decimal_places TINYINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (code),
    CONSTRAINT chk_currencies_code_length CHECK (CHAR_LENGTH(code) = 3),
    CONSTRAINT chk_currencies_decimal_places_non_negative CHECK (decimal_places >= 0)
);

CREATE TABLE receivable_types (
    code VARCHAR(64) NOT NULL,
    description VARCHAR(128) NOT NULL,
    monthly_spread DECIMAL(19, 8) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (code),
    CONSTRAINT chk_receivable_types_monthly_spread_non_negative CHECK (monthly_spread >= 0)
);

CREATE TABLE assignors (
    id CHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    document VARCHAR(32) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_assignors_name_not_blank CHECK (name <> '')
);

CREATE INDEX idx_assignors_document ON assignors (document);
CREATE INDEX idx_assignors_name ON assignors (name);

CREATE TABLE exchange_rates (
    id CHAR(36) NOT NULL,
    source_currency_code CHAR(3) NOT NULL,
    target_currency_code CHAR(3) NOT NULL,
    rate DECIMAL(19, 8) NOT NULL,
    valid_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_exchange_rates_source_currency
        FOREIGN KEY (source_currency_code) REFERENCES currencies(code),
    CONSTRAINT fk_exchange_rates_target_currency
        FOREIGN KEY (target_currency_code) REFERENCES currencies(code),
    CONSTRAINT chk_exchange_rates_positive_rate CHECK (rate > 0),
    CONSTRAINT chk_exchange_rates_distinct_currencies CHECK (source_currency_code <> target_currency_code)
);

CREATE INDEX idx_exchange_rates_pair_valid_at
    ON exchange_rates (source_currency_code, target_currency_code, valid_at DESC, created_at DESC);

CREATE TABLE receivables (
    id CHAR(36) NOT NULL,
    assignor_id CHAR(36) NOT NULL,
    external_reference VARCHAR(128) NOT NULL,
    receivable_type_code VARCHAR(64) NOT NULL,
    face_value DECIMAL(19, 4) NOT NULL,
    currency_code CHAR(3) NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    version BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_receivables_assignor
        FOREIGN KEY (assignor_id) REFERENCES assignors(id),
    CONSTRAINT fk_receivables_type
        FOREIGN KEY (receivable_type_code) REFERENCES receivable_types(code),
    CONSTRAINT fk_receivables_currency
        FOREIGN KEY (currency_code) REFERENCES currencies(code),
    CONSTRAINT uk_receivables_assignor_external_reference
        UNIQUE (assignor_id, external_reference),
    CONSTRAINT chk_receivables_positive_face_value CHECK (face_value > 0),
    CONSTRAINT chk_receivables_valid_status CHECK (status IN ('AVAILABLE', 'SETTLED', 'CANCELLED')),
    CONSTRAINT chk_receivables_non_negative_version CHECK (version >= 0)
);

CREATE INDEX idx_receivables_assignor ON receivables (assignor_id);
CREATE INDEX idx_receivables_type ON receivables (receivable_type_code);
CREATE INDEX idx_receivables_currency ON receivables (currency_code);
CREATE INDEX idx_receivables_status ON receivables (status);
CREATE INDEX idx_receivables_due_date ON receivables (due_date);

CREATE TABLE settlements (
    id CHAR(36) NOT NULL,
    assignor_id CHAR(36) NOT NULL,
    source_currency_code CHAR(3) NOT NULL,
    payment_currency_code CHAR(3) NOT NULL,
    status VARCHAR(32) NOT NULL,
    base_rate DECIMAL(19, 8) NOT NULL,
    item_count INT NOT NULL,
    total_face_value DECIMAL(19, 4) NOT NULL,
    total_present_value DECIMAL(19, 4) NOT NULL,
    total_payment_value DECIMAL(19, 4) NOT NULL,
    settled_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_settlements_assignor
        FOREIGN KEY (assignor_id) REFERENCES assignors(id),
    CONSTRAINT fk_settlements_source_currency
        FOREIGN KEY (source_currency_code) REFERENCES currencies(code),
    CONSTRAINT fk_settlements_payment_currency
        FOREIGN KEY (payment_currency_code) REFERENCES currencies(code),
    CONSTRAINT chk_settlements_valid_status CHECK (status IN ('PENDING', 'SETTLED', 'FAILED', 'CANCELLED')),
    CONSTRAINT chk_settlements_non_negative_base_rate CHECK (base_rate >= 0),
    CONSTRAINT chk_settlements_positive_item_count CHECK (item_count > 0),
    CONSTRAINT chk_settlements_non_negative_total_face_value CHECK (total_face_value >= 0),
    CONSTRAINT chk_settlements_non_negative_total_present_value CHECK (total_present_value >= 0),
    CONSTRAINT chk_settlements_non_negative_total_payment_value CHECK (total_payment_value >= 0)
);

CREATE INDEX idx_settlements_assignor ON settlements (assignor_id);
CREATE INDEX idx_settlements_source_currency ON settlements (source_currency_code);
CREATE INDEX idx_settlements_payment_currency ON settlements (payment_currency_code);
CREATE INDEX idx_settlements_status ON settlements (status);
CREATE INDEX idx_settlements_settled_at ON settlements (settled_at);
CREATE INDEX idx_settlements_statement_default ON settlements (settled_at DESC, id);

CREATE TABLE settlement_items (
    id CHAR(36) NOT NULL,
    settlement_id CHAR(36) NOT NULL,
    receivable_id CHAR(36) NOT NULL,
    external_reference VARCHAR(128) NOT NULL,
    receivable_type_code VARCHAR(64) NOT NULL,
    face_value DECIMAL(19, 4) NOT NULL,
    source_currency_code CHAR(3) NOT NULL,
    payment_currency_code CHAR(3) NOT NULL,
    base_rate DECIMAL(19, 8) NOT NULL,
    spread DECIMAL(19, 8) NOT NULL,
    term_in_months DECIMAL(19, 8) NOT NULL,
    present_value_source DECIMAL(19, 4) NOT NULL,
    discount_value DECIMAL(19, 4) NOT NULL,
    payment_value DECIMAL(19, 4) NOT NULL,
    exchange_rate DECIMAL(19, 8) NULL,
    calculated_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_settlement_items_settlement
        FOREIGN KEY (settlement_id) REFERENCES settlements(id),
    CONSTRAINT fk_settlement_items_receivable
        FOREIGN KEY (receivable_id) REFERENCES receivables(id),
    CONSTRAINT fk_settlement_items_receivable_type
        FOREIGN KEY (receivable_type_code) REFERENCES receivable_types(code),
    CONSTRAINT fk_settlement_items_source_currency
        FOREIGN KEY (source_currency_code) REFERENCES currencies(code),
    CONSTRAINT fk_settlement_items_payment_currency
        FOREIGN KEY (payment_currency_code) REFERENCES currencies(code),
    CONSTRAINT uk_settlement_items_receivable
        UNIQUE (receivable_id),
    CONSTRAINT chk_settlement_items_positive_face_value CHECK (face_value > 0),
    CONSTRAINT chk_settlement_items_non_negative_base_rate CHECK (base_rate >= 0),
    CONSTRAINT chk_settlement_items_non_negative_spread CHECK (spread >= 0),
    CONSTRAINT chk_settlement_items_positive_term CHECK (term_in_months > 0),
    CONSTRAINT chk_settlement_items_non_negative_present_value CHECK (present_value_source >= 0),
    CONSTRAINT chk_settlement_items_non_negative_discount CHECK (discount_value >= 0),
    CONSTRAINT chk_settlement_items_non_negative_payment_value CHECK (payment_value >= 0),
    CONSTRAINT chk_settlement_items_positive_exchange_rate CHECK (exchange_rate IS NULL OR exchange_rate > 0)
);

CREATE INDEX idx_settlement_items_settlement ON settlement_items (settlement_id);
CREATE INDEX idx_settlement_items_receivable_type ON settlement_items (receivable_type_code);
CREATE INDEX idx_settlement_items_source_currency ON settlement_items (source_currency_code);
CREATE INDEX idx_settlement_items_payment_currency ON settlement_items (payment_currency_code);
CREATE INDEX idx_settlement_items_calculated_at ON settlement_items (calculated_at);
