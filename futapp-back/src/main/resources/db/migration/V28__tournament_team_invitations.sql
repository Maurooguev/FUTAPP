ALTER TABLE solicitud_reclamo
    ADD COLUMN categoria VARCHAR(120);

DROP INDEX IF EXISTS uk_reclamo_equipo_pendiente;

CREATE UNIQUE INDEX uk_reclamo_equipo_pendiente
    ON solicitud_reclamo (id_solicitante, id_equipo)
    WHERE estado = 'PENDIENTE' AND tipo = 'EQUIPO';

CREATE UNIQUE INDEX uk_invitacion_equipo_torneo_pendiente
    ON solicitud_reclamo (id_equipo, id_torneo)
    WHERE estado = 'PENDIENTE' AND tipo = 'INVITACION_TORNEO';
