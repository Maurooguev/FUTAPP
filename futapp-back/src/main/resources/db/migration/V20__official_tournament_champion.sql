ALTER TABLE torneo
    ADD COLUMN id_campeon BIGINT REFERENCES equipo(id_equipo) ON DELETE SET NULL;
