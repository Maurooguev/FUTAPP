import { useState } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import { API_URL, apiFetch } from "../config/api";
import useAuth from "../context/useAuth";

function Cuenta() {
  const { usuario, logout, logoutAll } = useAuth();
  const navigate = useNavigate();
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmation, setConfirmation] = useState("");
  const [message, setMessage] = useState("");
  if (!usuario) return <Navigate to="/login" replace />;
  const roles = (usuario.roles || [usuario.rol]).map((rol) => rol.replaceAll("_", " ")).join(" · ");
  const submit = async (event) => {
    event.preventDefault();
    if (newPassword !== confirmation) return setMessage("Las contraseñas nuevas no coinciden");
    const response = await apiFetch(`${API_URL}/auth/password`, { method: "PUT", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ passwordActual: currentPassword, nuevaPassword: newPassword }) });
    const data = await response.json().catch(() => null);
    setMessage(data?.mensaje || data?.message || "No se pudo cambiar la contraseña");
    if (response.ok) { await logout(); navigate("/login"); }
  };
  const closeAllSessions = async () => {
    setMessage("");
    try { await logoutAll(); navigate("/login", { replace: true }); }
    catch (error) { setMessage(error.message); }
  };
  return <main className="profile-page"><div className="account-heading"><span>Mi cuenta</span><h1>{usuario.passwordChangeRequired ? "Creá tu contraseña" : usuario.nombre}</h1><p>{usuario.passwordChangeRequired ? "Antes de administrar tus torneos, reemplazá la contraseña temporal." : `${usuario.email} · ${roles}`}</p></div><section className="profile-panel account-security"><h2>{usuario.passwordChangeRequired ? "Primer ingreso" : "Seguridad"}</h2><p>{usuario.passwordChangeRequired ? "La nueva contraseña será únicamente tuya y cerrará la sesión temporal." : "Cambiar la contraseña cerrará tus sesiones abiertas."}</p><form className="personal-profile-form" onSubmit={submit}><label>Contraseña temporal o actual<input type="password" required value={currentPassword} onChange={(event) => setCurrentPassword(event.target.value)} /></label><label>Nueva contraseña<input type="password" minLength="10" required value={newPassword} onChange={(event) => setNewPassword(event.target.value)} /></label><label>Repetir nueva contraseña<input type="password" minLength="10" required value={confirmation} onChange={(event) => setConfirmation(event.target.value)} /></label>{message && <p className="profile-action-message">{message}</p>}<button type="submit">{usuario.passwordChangeRequired ? "Activar mi acceso" : "Cambiar contraseña"}</button></form>{!usuario.passwordChangeRequired && <div className="account-session-actions"><h3>Sesiones abiertas</h3><p>Si no reconocés una actividad o usaste otro dispositivo, cerrá todas las sesiones de la cuenta.</p><button type="button" onClick={closeAllSessions}>Cerrar todas las sesiones</button></div>}</section></main>;
}
export default Cuenta;
