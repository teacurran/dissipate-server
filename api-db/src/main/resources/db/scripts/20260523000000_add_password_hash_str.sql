--
--    Migrate password hashing to Argon2id.
--
--    Adds a self-describing PHC-format Argon2id column alongside the legacy
--    PBKDF2 columns. The legacy columns are kept (for now) so existing
--    accounts can still verify their password; on first successful login the
--    hash is rewritten to password_hash_str and the legacy fields are cleared.
--

-- // Add password_hash_str column to accounts
ALTER TABLE accounts
  ADD COLUMN IF NOT EXISTS password_hash_str VARCHAR(255);

-- //@UNDO
ALTER TABLE accounts
  DROP COLUMN IF EXISTS password_hash_str;
