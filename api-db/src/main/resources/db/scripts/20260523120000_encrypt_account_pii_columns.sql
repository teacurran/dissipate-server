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

-- // Encrypt Account PII columns at rest.
-- Migration SQL that makes the change goes here.

-- Drop the legacy plaintext PII columns. This is an experimental project and the
-- columns are believed to hold no preserved data; if any environment has values
-- they should be re-entered through the application so they land encrypted.
ALTER TABLE accounts DROP COLUMN IF EXISTS first_name;
ALTER TABLE accounts DROP COLUMN IF EXISTS last_name;
ALTER TABLE accounts DROP COLUMN IF EXISTS address1;
ALTER TABLE accounts DROP COLUMN IF EXISTS address2;
ALTER TABLE accounts DROP COLUMN IF EXISTS city;
ALTER TABLE accounts DROP COLUMN IF EXISTS postal_code;

-- Add the new encrypted byte[] columns. Values are AES-GCM ciphertexts produced
-- by EncryptedStringConverter; key sourced from the dissipate.pii.key config.
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS first_name_enc BYTEA;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS last_name_enc BYTEA;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS address1_enc BYTEA;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS address2_enc BYTEA;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS city_enc BYTEA;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS postal_code_enc BYTEA;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE accounts DROP COLUMN IF EXISTS first_name_enc;
ALTER TABLE accounts DROP COLUMN IF EXISTS last_name_enc;
ALTER TABLE accounts DROP COLUMN IF EXISTS address1_enc;
ALTER TABLE accounts DROP COLUMN IF EXISTS address2_enc;
ALTER TABLE accounts DROP COLUMN IF EXISTS city_enc;
ALTER TABLE accounts DROP COLUMN IF EXISTS postal_code_enc;

ALTER TABLE accounts ADD COLUMN IF NOT EXISTS first_name VARCHAR(255);
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS last_name VARCHAR(255);
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS address1 VARCHAR(255);
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS address2 VARCHAR(255);
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS city VARCHAR(255);
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS postal_code VARCHAR(255);
