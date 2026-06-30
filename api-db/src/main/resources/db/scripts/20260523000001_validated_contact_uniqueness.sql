-- // validated_contact_uniqueness
-- Migration SQL that makes the change goes here.
--
-- Enforce single-account ownership of a validated email or phone number.
-- Without these partial unique indexes two different Accounts can both
-- complete OTP validation for the same contact, silently forking the
-- identity inside our IDP. Filtering on `validated IS NOT NULL` means
-- pre-validation rows (and historical unverified entries) are not
-- restricted, only confirmed ownership.

CREATE UNIQUE INDEX IF NOT EXISTS uidx_account_emails_validated_lower
  ON account_emails (LOWER(email))
  WHERE validated IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uidx_account_phones_validated
  ON account_phones (phone)
  WHERE validated IS NOT NULL;

-- //@UNDO
-- SQL to undo the change goes here.

DROP INDEX IF EXISTS uidx_account_phones_validated;
DROP INDEX IF EXISTS uidx_account_emails_validated_lower;
