export const ROLES = {
  PUBLICO: "PUBLICO",
  JUGADOR: "JUGADOR",
  DIRECTOR_EQUIPO: "DIRECTOR_EQUIPO",
  ORGANIZADOR: "ORGANIZADOR",
  ADMIN: "ADMIN",
};

export function tieneRol(usuario, rol) {
  return usuario?.roles?.includes(rol) || usuario?.rol === rol;
}

export function puedeAdministrar(usuario) {
  return Boolean(usuario);
}

export function esAdmin(usuario) {
  return tieneRol(usuario, ROLES.ADMIN);
}

export function puedeAdministrarTorneo(usuario, torneo) {
  return tieneRol(usuario, ROLES.ADMIN) ||
    torneo?.idPropietario === usuario?.idUsuario;
}
