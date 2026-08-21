CREATE TABLE password_reset_token (
    token_hash VARCHAR(64) PRIMARY KEY,
    id_usuario BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    CONSTRAINT fk_password_reset_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
);

CREATE INDEX ix_password_reset_usuario ON password_reset_token (id_usuario);
CREATE INDEX ix_password_reset_expiration ON password_reset_token (expires_at);
