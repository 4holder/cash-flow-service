CREATE TABLE IF NOT EXISTS public.financial_info(
id                      VARCHAR(37) PRIMARY KEY,
user_id                 VARCHAR(37) NOT NULL,
number_of_dependents    INTEGER,
created_at              timestamptz DEFAULT NOW() NOT NULL,
modified_at             timestamptz DEFAULT NOW() NOT NULL
);

CREATE TABLE IF NOT EXISTS public.deductions(
id                      VARCHAR(37) PRIMARY KEY,
financial_info_id       VARCHAR(37) NOT NULL,
deduction_type          VARCHAR,
currency                INTEGER,
value_in_cents          BIGINT,
created_at              timestamptz DEFAULT NOW() NOT NULL,
modified_at             timestamptz DEFAULT NOW() NOT NULL,
FOREIGN KEY (financial_info_id) REFERENCES public.financial_info (id)
);

CREATE TABLE IF NOT EXISTS public.irrf_reference(
id                      VARCHAR(37) PRIMARY KEY,
reference_year          BIGINT,
start_date              timestamptz NOT NULL,
end_date                timestamptz,
created_at              timestamptz DEFAULT NOW() NOT NULL,
modified_at             timestamptz DEFAULT NOW() NOT NULL
);

CREATE TABLE IF NOT EXISTS public.irrf_reference_intervals(
id                      VARCHAR(37) PRIMARY KEY,
start_range             BIGINT NOT NULL,
end_range               BIGINT,
aliquot                 BIGINT,
end_date                timestamptz,
created_at              timestamptz DEFAULT NOW() NOT NULL,
modified_at             timestamptz DEFAULT NOW() NOT NULL
);

CREATE TABLE IF NOT EXISTS public.inss_reference(
id                      VARCHAR(37) PRIMARY KEY,
reference_year          BIGINT,
end_range               BIGINT,
start_date              timestamptz NOT NULL,
end_date                timestamptz,
created_at              timestamptz DEFAULT NOW() NOT NULL,
modified_at             timestamptz DEFAULT NOW() NOT NULL
);

CREATE TABLE IF NOT EXISTS public.inss_reference_intervals(
id                      VARCHAR(37) PRIMARY KEY,
start_range             BIGINT NOT NULL,
end_range               BIGINT,
created_at              timestamptz DEFAULT NOW() NOT NULL,
modified_at             timestamptz DEFAULT NOW() NOT NULL
);
