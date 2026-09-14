WITH duplicados AS (
    SELECT id_solicitud,
           ROW_NUMBER() OVER (
               PARTITION BY id_solicitante, COALESCE(id_equipo, -1), COALESCE(id_jugador, -1)
               ORDER BY fecha_solicitud, id_solicitud
           ) AS posicion
    FROM solicitud_reclamo
    WHERE estado = 'PENDIENTE'
)
UPDATE solicitud_reclamo s
SET estado = 'RECHAZADA',
    motivo_resolucion = 'Solicitud duplicada cerrada automáticamente',
    fecha_resolucion = CURRENT_TIMESTAMP
FROM duplicados d
WHERE s.id_solicitud = d.id_solicitud AND d.posicion > 1;

CREATE UNIQUE INDEX uk_reclamo_equipo_pendiente
    ON solicitud_reclamo (id_solicitante, id_equipo)
    WHERE estado = 'PENDIENTE' AND id_equipo IS NOT NULL;

CREATE UNIQUE INDEX uk_reclamo_jugador_pendiente
    ON solicitud_reclamo (id_solicitante, id_jugador)
    WHERE estado = 'PENDIENTE' AND id_jugador IS NOT NULL;
