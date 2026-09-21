ALTER TABLE solicitud_reclamo
    ADD COLUMN id_equipo_destino BIGINT REFERENCES equipo(id_equipo);

CREATE UNIQUE INDEX uk_fusion_equipo_pendiente
    ON solicitud_reclamo (id_solicitante, id_equipo, id_equipo_destino)
    WHERE estado = 'PENDIENTE' AND tipo = 'FUSION_EQUIPO';
