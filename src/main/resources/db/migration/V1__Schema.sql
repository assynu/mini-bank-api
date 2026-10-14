CREATE TABLE accounts
(
    id         UUID PRIMARY KEY,
    owner      VARCHAR(255) NOT NULL,
    currency   CHAR(3)      NOT NULL,
    balance    NUMERIC      NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT accounts_balance_not_negative CHECK (balance >= 0)
);

CREATE TABLE transfers
(
    id              UUID PRIMARY KEY,
    from_account_id UUID        NOT NULL,
    to_account_id   UUID        NOT NULL,
    currency        CHAR(3)     NOT NULL,
    amount          NUMERIC     NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT transfers_from_account_fk FOREIGN KEY (from_account_id) REFERENCES accounts (id),
    CONSTRAINT transfers_to_account_fk FOREIGN KEY (to_account_id) REFERENCES accounts (id),
    CONSTRAINT transfers_amount_positive CHECK (amount > 0),
    CONSTRAINT transfers_from_different_than_to CHECK (from_account_id <> to_account_id)
);

CREATE INDEX transfers_from_account_id_idx ON transfers (from_account_id);
CREATE INDEX transfers_to_account_id_idx ON transfers (to_account_id);

CREATE TYPE account_history_type AS ENUM ('DEPOSIT', 'TRANSFER');

CREATE TABLE account_history
(
    id          UUID PRIMARY KEY,
    account_id  UUID                 NOT NULL,
    transfer_id UUID,
    type        account_history_type NOT NULL,
    amount      NUMERIC              NOT NULL,
    created_at  TIMESTAMPTZ          NOT NULL DEFAULT now(),

    CONSTRAINT account_history_account_fk FOREIGN KEY (account_id) REFERENCES accounts (id),
    CONSTRAINT account_history_transfer_fk FOREIGN KEY (transfer_id) REFERENCES transfers (id),
    CONSTRAINT account_history_amount CHECK (amount <> 0),
    CONSTRAINT account_history_transfer_matches_type CHECK (
        (type = 'DEPOSIT' AND transfer_id IS NULL) OR
        (type = 'TRANSFER' AND transfer_id IS NOT NULL)
        )
);

CREATE INDEX account_history_account_id_idx ON account_history (account_id);
CREATE INDEX account_history_transfer_id_idx ON account_history (transfer_id);