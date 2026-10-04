CREATE TABLE sancion_jugador (
    id_sancion BIGSERIAL PRIMARY KEY,
    id_tarjeta BIGINT NOT NULL UNIQUE REFERENCES tarjeta(id_tarjeta) ON DELETE CASCADE,
    fechas_total INTEGER NOT NULL DEFAULT 1 CHECK (fechas_total >= 1),
    motivo VARCHAR(500),
    creada_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO sancion_jugador (id_tarjeta, fechas_total, motivo)
SELECT id_tarjeta, 1, motivo
FROM tarjeta
WHERE UPPER(tipo_tarjeta) = 'ROJA';

