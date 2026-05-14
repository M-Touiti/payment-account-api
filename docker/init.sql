CREATE TABLE IF NOT EXISTS users (
    id          UUID            NOT NULL PRIMARY KEY,
    email       VARCHAR(255)    NOT NULL UNIQUE,
    password_hash VARCHAR(255)  NOT NULL,
    role        VARCHAR(20)     NOT NULL,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    enabled     BOOLEAN         NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS accounts (
    id              UUID            NOT NULL PRIMARY KEY,
    user_id         UUID            NOT NULL REFERENCES users(id),
    account_number  VARCHAR(50)     NOT NULL UNIQUE,
    type            VARCHAR(20)     NOT NULL,
    currency        VARCHAR(10)     NOT NULL,
    balance         NUMERIC(19,4)   NOT NULL DEFAULT 0,
    status          VARCHAR(20)     NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS transactions (
    id              UUID            NOT NULL PRIMARY KEY,
    account_id      UUID            NOT NULL REFERENCES accounts(id),
    type            VARCHAR(10)     NOT NULL,
    amount          NUMERIC(19,4)   NOT NULL,
    description     VARCHAR(500)    NOT NULL,
    balance_after   NUMERIC(19,4)   NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_accounts_user_id ON accounts(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_account_id ON transactions(account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_type ON transactions(type);
