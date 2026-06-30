-- // add_account_role
-- Migration SQL that makes the change goes here.
--
-- The gRPC auth pipeline enforces a per-method MethodPolicy.min_role against the
-- caller's account role. Persist that role on accounts as a VARCHAR name (matching
-- the existing status column style), defaulting every account to USER.

ALTER TABLE public.accounts
    ADD COLUMN role character varying(255) NOT NULL DEFAULT 'USER',
    ADD CONSTRAINT accounts_role_check
        CHECK (((role)::text = ANY ((ARRAY['USER'::character varying, 'VERIFIED'::character varying, 'ADMIN'::character varying])::text[])));

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE public.accounts
    DROP CONSTRAINT IF EXISTS accounts_role_check,
    DROP COLUMN IF EXISTS role;
