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
