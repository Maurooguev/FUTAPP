ALTER TABLE solicitud_reclamo
    DROP CONSTRAINT IF EXISTS ck_reclamo_objetivo;

ALTER TABLE solicitud_reclamo
    ADD CONSTRAINT ck_reclamo_objetivo CHECK (
        (tipo = 'EQUIPO' AND id_equipo IS NOT NULL AND id_jugador IS NULL AND id_equipo_destino IS NULL)
        OR (tipo = 'JUGADOR' AND id_jugador IS NOT NULL AND id_equipo IS NULL AND id_equipo_destino IS NULL)
        OR (tipo = 'FUSION_EQUIPO' AND id_equipo IS NOT NULL AND id_equipo_destino IS NOT NULL AND id_jugador IS NULL)
        OR (tipo = 'INVITACION_TORNEO' AND id_equipo IS NOT NULL AND id_jugador IS NULL AND id_equipo_destino IS NULL)
    );
