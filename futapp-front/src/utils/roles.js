export const ROLES = {
  PUBLICO: "PUBLICO",
  JUGADOR: "JUGADOR",
  DIRECTOR_EQUIPO: "DIRECTOR_EQUIPO",
  ORGANIZADOR: "ORGANIZADOR",
  ADMIN: "ADMIN",
};

export function puedeAdministrar(usuario) {
  return Boolean(usuario);
}

export function esAdmin(usuario) {
  return usuario?.rol === ROLES.ADMIN;
}

export function puedeAdministrarTorneo(usuario, torneo) {
  return usuario?.rol === ROLES.ADMIN ||
    torneo?.idPropietario === usuario?.idUsuario;
}
