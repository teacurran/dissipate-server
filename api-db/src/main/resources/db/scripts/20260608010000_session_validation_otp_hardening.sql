-- // session_validation_otp_hardening
-- Migration SQL that makes the change goes here.
--
-- OTP hardening: until now session_validations OTPs never expired and could be
-- guessed without limit. Add an expiry timestamp and a per-OTP attempt counter
-- so the REST verify endpoint can reject expired/exhausted codes. Existing rows
-- get expires=NULL (treated as non-expiring legacy) and attempts=0.

ALTER TABLE public.session_validations
    ADD COLUMN expires timestamp(6) with time zone,
    ADD COLUMN attempts integer NOT NULL DEFAULT 0;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE public.session_validations
    DROP COLUMN IF EXISTS attempts,
    DROP COLUMN IF EXISTS expires;
