ALTER TABLE torneo
    ADD COLUMN IF NOT EXISTS motivo_cancelacion VARCHAR(500);
