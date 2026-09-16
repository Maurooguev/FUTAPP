ALTER TABLE partido ADD COLUMN fecha_hora_nueva TIMESTAMP;

UPDATE partido
SET fecha_hora_nueva = CASE
    WHEN fecha_hora IS NULL OR BTRIM(fecha_hora) = '' THEN NULL
    WHEN fecha_hora ~ '^\d{4,6}-\d{2}-\d{2}[ T]\d{2}:\d{2}(:\d{2})?$'
        THEN REPLACE(fecha_hora, 'T', ' ')::TIMESTAMP
    ELSE NULL
END;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM partido
        WHERE fecha_hora IS NOT NULL AND BTRIM(fecha_hora) <> '' AND fecha_hora_nueva IS NULL
    ) THEN
        RAISE EXCEPTION 'Hay fechas de partido con formato inválido; se cancela la migración para no perder datos';
    END IF;
END $$;

ALTER TABLE partido DROP COLUMN fecha_hora;
ALTER TABLE partido RENAME COLUMN fecha_hora_nueva TO fecha_hora;
CREATE INDEX ix_partido_fecha_hora ON partido (fecha_hora);
