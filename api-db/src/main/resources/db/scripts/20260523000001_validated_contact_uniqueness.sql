--
--    Copyright 2010-2023 the original author or authors.
--
--    Licensed under the Apache License, Version 2.0 (the "License");
--    you may not use this file except in compliance with the License.
--    You may obtain a copy of the License at
--
--       https://www.apache.org/licenses/LICENSE-2.0
--
--    Unless required by applicable law or agreed to in writing, software
--    distributed under the License is distributed on an "AS IS" BASIS,
--    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--    See the License for the specific language governing permissions and
--    limitations under the License.
--

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
