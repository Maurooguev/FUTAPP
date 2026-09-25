UPDATE equipo_administrador administrador
SET estado = 'INACTIVO'
FROM usuario cuenta
WHERE administrador.id_usuario = cuenta.id_usuario
  AND administrador.estado = 'ACTIVO'
  AND cuenta.rol NOT IN ('DIRECTOR_EQUIPO', 'ADMIN');
