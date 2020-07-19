ALTER TABLE public.incomes
    DROP CONSTRAINT incomes_financial_contract_id_fkey,
    ADD CONSTRAINT incomes_financial_contract_id_fkey
        FOREIGN KEY (financial_contract_id)
            REFERENCES financial_contracts(id)
            ON DELETE CASCADE;

ALTER TABLE public.income_discounts
    DROP CONSTRAINT income_discounts_income_id_fkey,
    ADD CONSTRAINT income_discounts_income_id_fkey
        FOREIGN KEY (income_id)
            REFERENCES incomes(id)
            ON DELETE CASCADE;