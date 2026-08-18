ALTER TABLE equipo ADD COLUMN codigo_publico VARCHAR(10);
ALTER TABLE equipo ADD COLUMN descripcion VARCHAR(1000);
ALTER TABLE equipo ADD COLUMN foto_plantel VARCHAR(500);
ALTER TABLE equipo ADD COLUMN email_contacto VARCHAR(255);

UPDATE equipo
SET codigo_publico = 'EQ-' || UPPER(SUBSTRING(MD5(id_equipo::text || nombre) FROM 1 FOR 7));

ALTER TABLE equipo ALTER COLUMN codigo_publico SET NOT NULL;
ALTER TABLE equipo ADD CONSTRAINT uk_equipo_codigo_publico UNIQUE (codigo_publico);

ALTER TABLE jugador ADD COLUMN codigo_publico VARCHAR(11);
ALTER TABLE jugador ADD COLUMN apodo VARCHAR(255);
ALTER TABLE jugador ADD COLUMN pierna_habil VARCHAR(255);

UPDATE jugador
SET codigo_publico = 'JUG-' || UPPER(SUBSTRING(MD5(id_jugador::text || nombre || apellido) FROM 1 FOR 7));

ALTER TABLE jugador ALTER COLUMN codigo_publico SET NOT NULL;
ALTER TABLE jugador ADD CONSTRAINT uk_jugador_codigo_publico UNIQUE (codigo_publico);
