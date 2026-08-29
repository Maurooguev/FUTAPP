ALTER TABLE partido ADD COLUMN id_mvp BIGINT;
ALTER TABLE partido ADD CONSTRAINT fk_partido_mvp FOREIGN KEY (id_mvp) REFERENCES jugador(id_jugador);
