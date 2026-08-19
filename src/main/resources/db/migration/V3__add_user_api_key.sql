-- The X-User-Id header used for auth so far let any caller impersonate any user by
-- guessing a sequential id, with no secret involved. Replace it with a per-user API key:
-- only its SHA-256 hash is stored, the raw key is shown once at signup.
ALTER TABLE users ADD COLUMN api_key_hash VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE users ALTER COLUMN api_key_hash DROP DEFAULT;
CREATE UNIQUE INDEX uk_users_api_key_hash ON users (api_key_hash);
