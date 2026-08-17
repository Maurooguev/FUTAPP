export const ROLES = {
  PUBLICO: "PUBLICO",
  JUGADOR: "JUGADOR",
  ORGANIZADOR: "ORGANIZADOR",
  ADMIN: "ADMIN",
};

export function puedeAdministrar(usuario) {
  return usuario?.rol === ROLES.ADMIN || usuario?.rol === ROLES.ORGANIZADOR;
}

export function esAdmin(usuario) {
  return usuario?.rol === ROLES.ADMIN;
}