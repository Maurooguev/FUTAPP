ALTER TABLE equipo ADD COLUMN id_creador BIGINT;
ALTER TABLE equipo ADD CONSTRAINT fk_equipo_creador FOREIGN KEY (id_creador) REFERENCES usuario (id_usuario);

UPDATE equipo e
SET id_creador = t.id_propietario
FROM torneo t
WHERE e.id_torneo = t.id_torneo AND e.id_creador IS NULL;

CREATE INDEX ix_equipo_creador ON equipo (id_creador);
