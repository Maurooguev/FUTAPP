import { useEffect, useState } from "react";
import AuthContext from "./auth-context";
import { API_URL, apiFetch } from "../config/api";

function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(() => {
    const guardado = localStorage.getItem("usuario");
    const token = localStorage.getItem("authToken");
    return guardado && token ? JSON.parse(guardado) : null;
  });

  const saveSession = (session) => {
    const nuevoUsuario = { idUsuario: session.idUsuario, nombre: session.nombre, email: session.email, rol: session.rol };
    setUsuario(nuevoUsuario);
    localStorage.setItem("usuario", JSON.stringify(nuevoUsuario));
    localStorage.setItem("authToken", session.token);
  };

  useEffect(() => {
    if (!localStorage.getItem("authToken")) return;

    apiFetch(`${API_URL}/auth/me`)
      .then(async (response) => {
        if (!response.ok) throw new Error("La sesión ya no es válida");
        const session = await response.json();
        const usuarioActual = {
          idUsuario: session.idUsuario,
          nombre: session.nombre,
          email: session.email,
          rol: session.rol,
        };
        setUsuario(usuarioActual);
        localStorage.setItem("usuario", JSON.stringify(usuarioActual));
      })
      .catch(() => {
        setUsuario(null);
        localStorage.removeItem("usuario");
        localStorage.removeItem("authToken");
      });
  }, []);

  const login = async (email, password) => {
    const response = await fetch(`${API_URL}/auth/login`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ email, password }) });
    const payload = await response.json().catch(() => null);
    if (!response.ok) throw new Error(payload?.message || "No se pudo iniciar sesión");
    saveSession(payload);
  };

  const register = async (nombre, email, password, rol) => {
    const response = await fetch(`${API_URL}/auth/register`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ nombre, email, password, rol }) });
    const payload = await response.json().catch(() => null);
    if (!response.ok) throw new Error(payload?.message || "No se pudo crear la cuenta");
    saveSession(payload);
  };

  const logout = async () => {
    await apiFetch(`${API_URL}/auth/logout`, { method: "POST" }).catch(() => null);
    setUsuario(null);
    localStorage.removeItem("usuario");
    localStorage.removeItem("authToken");
  };

  return (
    <AuthContext.Provider value={{ usuario, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export default AuthProvider;
