ALTER TABLE solicitud_reclamo
    ADD COLUMN IF NOT EXISTS id_usuario_destino BIGINT REFERENCES usuario(id_usuario),
    ADD COLUMN IF NOT EXISTS email_destino VARCHAR(160);

CREATE INDEX IF NOT EXISTS ix_reclamo_email_destino
    ON solicitud_reclamo (LOWER(email_destino), estado);

ALTER TABLE solicitud_reclamo DROP CONSTRAINT IF EXISTS ck_reclamo_objetivo;
ALTER TABLE solicitud_reclamo ADD CONSTRAINT ck_reclamo_objetivo CHECK (
    (tipo = 'EQUIPO' AND id_equipo IS NOT NULL AND id_jugador IS NULL AND id_equipo_destino IS NULL)
    OR (tipo = 'JUGADOR' AND id_jugador IS NOT NULL AND id_equipo IS NULL AND id_equipo_destino IS NULL)
    OR (tipo = 'FUSION_EQUIPO' AND id_equipo IS NOT NULL AND id_equipo_destino IS NOT NULL AND id_jugador IS NULL)
    OR (tipo = 'VINCULACION_EQUIPO' AND id_equipo IS NOT NULL AND id_equipo_destino IS NULL AND id_jugador IS NULL AND email_destino IS NOT NULL)
    OR (tipo = 'INVITACION_TORNEO' AND id_equipo IS NOT NULL AND id_jugador IS NULL AND id_equipo_destino IS NULL)
    OR (tipo = 'CAMBIO_JUGADOR' AND id_jugador IS NOT NULL AND id_equipo IS NOT NULL AND id_equipo_destino IS NULL)
);
