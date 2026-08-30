-- Toda cuenta común puede conservar o reclamar un perfil de jugador.
INSERT INTO usuario_rol (id_usuario, rol)
SELECT id_usuario, 'JUGADOR'
FROM usuario
WHERE rol <> 'ADMIN'
ON CONFLICT (id_usuario, rol) DO NOTHING;

-- Ser propietario de al menos un torneo otorga capacidad de organizador.
INSERT INTO usuario_rol (id_usuario, rol)
SELECT DISTINCT id_propietario, 'ORGANIZADOR'
FROM torneo
WHERE id_propietario IS NOT NULL
ON CONFLICT (id_usuario, rol) DO NOTHING;

-- Conservamos el campo legado como rol principal para clientes antiguos.
UPDATE usuario
SET rol = 'ORGANIZADOR'
WHERE id_usuario IN (
    SELECT DISTINCT id_propietario FROM torneo WHERE id_propietario IS NOT NULL
)
AND rol = 'JUGADOR';
