export const ROLES = {
  PUBLICO: "PUBLICO",
  JUGADOR: "JUGADOR",
  DIRECTOR_EQUIPO: "DIRECTOR_EQUIPO",
  ORGANIZADOR: "ORGANIZADOR",
  ADMIN: "ADMIN",
};

export function puedeAdministrar(usuario) {
  return usuario?.rol === ROLES.ADMIN || usuario?.rol === ROLES.ORGANIZADOR;
}

export function esAdmin(usuario) {
  return usuario?.rol === ROLES.ADMIN;
}

export function puedeAdministrarTorneo(usuario, torneo) {
  return usuario?.rol === ROLES.ADMIN ||
    (usuario?.rol === ROLES.ORGANIZADOR && torneo?.idPropietario === usuario?.idUsuario);
}
