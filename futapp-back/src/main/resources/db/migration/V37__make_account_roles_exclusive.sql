DELETE FROM usuario_rol asignado
USING usuario cuenta
WHERE asignado.id_usuario = cuenta.id_usuario
  AND asignado.rol <> cuenta.rol;

INSERT INTO usuario_rol (id_usuario, rol)
SELECT cuenta.id_usuario, cuenta.rol
FROM usuario cuenta
WHERE NOT EXISTS (
    SELECT 1
    FROM usuario_rol asignado
    WHERE asignado.id_usuario = cuenta.id_usuario
      AND asignado.rol = cuenta.rol
);
