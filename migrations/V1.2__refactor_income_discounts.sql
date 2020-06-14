ALTER TABLE public.income_discounts DROP COLUMN is_active;
ALTER TABLE public.income_discounts DROP COLUMN occurrences;

ALTER TABLE public.income_discounts ADD COLUMN aliquot DOUBLE PRECISION;