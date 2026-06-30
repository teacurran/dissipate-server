-- // account_login_lockout
-- Migration SQL that makes the change goes here.
--
-- Brute-force protection for password login: count consecutive failures and
-- lock the account until a timestamp once a threshold is exceeded. Existing
-- rows start at 0 failures / not locked.

ALTER TABLE public.accounts
    ADD COLUMN failed_login_attempts integer NOT NULL DEFAULT 0,
    ADD COLUMN locked_until timestamp(6) with time zone;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE public.accounts
    DROP COLUMN IF EXISTS locked_until,
    DROP COLUMN IF EXISTS failed_login_attempts;
