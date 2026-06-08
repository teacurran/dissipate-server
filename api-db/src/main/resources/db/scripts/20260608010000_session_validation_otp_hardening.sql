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
