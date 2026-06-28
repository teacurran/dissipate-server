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
