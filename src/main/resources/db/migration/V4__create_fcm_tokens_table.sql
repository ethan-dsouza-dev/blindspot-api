CREATE TABLE fcm_tokens (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token      TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_fcm_tokens_token UNIQUE (token)
);

CREATE INDEX idx_fcm_tokens_user_id ON fcm_tokens(user_id);

-- Carry over any token already registered under the old single-column design.
INSERT INTO fcm_tokens (user_id, token)
SELECT id, fcm_token FROM users WHERE fcm_token IS NOT NULL;

ALTER TABLE users DROP COLUMN fcm_token;