ALTER TABLE usuario ADD COLUMN IF NOT EXISTS failed_login_attempts INTEGER NOT NULL DEFAULT 0;
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS login_failure_started_at TIMESTAMP;
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS locked_until TIMESTAMP;

CREATE INDEX IF NOT EXISTS ix_usuario_locked_until ON usuario (locked_until);
