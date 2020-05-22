CREATE TABLE IF NOT EXISTS public.financial_contracts (
    id                      VARCHAR(37) PRIMARY KEY NOT NULL,
    user_id                 VARCHAR(37) NOT NULL,
    name                    VARCHAR NOT NULL,
    contract_type           VARCHAR NOT NULL,
    company_cnpj            VARCHAR(14),
    is_active               BOOLEAN DEFAULT true NOT NULL,
    gross_amount_in_cents   BIGINT NOT NULL,
    currency                VARCHAR(3) NOT NULL default 'BRL',
    start_date              timestamptz,
    end_date                timestamptz,
    created_at              timestamptz NOT NULL,
    modified_at             timestamptz NOT NULL
);

CREATE INDEX user_id_idx ON public.financial_contracts (user_id);

CREATE TABLE IF NOT EXISTS public.incomes(
    id                      VARCHAR(37) PRIMARY KEY,
    financial_contract_id   VARCHAR(37),
    name                    VARCHAR NOT NULL,
    value_in_cents          BIGINT NOT NULL,
    currency                VARCHAR(3) NOT NULL default 'BRL',
    is_active               BOOLEAN DEFAULT true NOT NULL,
    income_type             VARCHAR NOT NULL,
    occurrences             VARCHAR NOT NULL,
    created_at              timestamptz NOT NULL,
    modified_at             timestamptz NOT NULL,
    FOREIGN KEY (financial_contract_id) REFERENCES public.financial_contracts (id)
);

CREATE TABLE IF NOT EXISTS public.income_discounts(
    id                      VARCHAR(37) PRIMARY KEY,
    income_id               VARCHAR(37) NOT NULL,
    name                    VARCHAR NOT NULL,
    discount_type           VARCHAR NOT NULL,
    currency                VARCHAR(3) NOT NULL default 'BRL',
    value_in_cents          BIGINT NOT NULL,
    is_active               BOOLEAN DEFAULT true NOT NULL,
    occurrences             VARCHAR NOT NULL,
    created_at              timestamptz NOT NULL,
    modified_at             timestamptz NOT NULL,
    FOREIGN KEY (income_id) REFERENCES public.incomes (id)
);

