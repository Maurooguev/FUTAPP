ALTER TABLE auditoria_administrativa
    ADD COLUMN usuario VARCHAR(160);

ALTER TABLE auditoria_administrativa
    DROP CONSTRAINT fk_auditoria_administrativa_usuario;
