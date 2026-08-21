import { useState } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import { API_URL, apiFetch } from "../config/api";
import useAuth from "../context/useAuth";

function Cuenta() {
  const { usuario, logout } = useAuth();
  const navigate = useNavigate();
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmation, setConfirmation] = useState("");
  const [message, setMessage] = useState("");
  if (!usuario) return <Navigate to="/login" replace />;
  const submit = async (event) => {
    event.preventDefault();
    if (newPassword !== confirmation) return setMessage("Las contraseñas nuevas no coinciden");
    const response = await apiFetch(`${API_URL}/auth/password`, { method: "PUT", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ passwordActual: currentPassword, nuevaPassword: newPassword }) });
    const data = await response.json().catch(() => null);
    setMessage(data?.mensaje || data?.message || "No se pudo cambiar la contraseña");
    if (response.ok) { await logout(); navigate("/login"); }
  };
  return <main className="profile-page"><div className="account-heading"><span>Mi cuenta</span><h1>{usuario.nombre}</h1><p>{usuario.email} · {usuario.rol.replaceAll("_", " ")}</p></div><section className="profile-panel account-security"><h2>Seguridad</h2><p>Cambiar la contraseña cerrará tus sesiones abiertas.</p><form className="personal-profile-form" onSubmit={submit}><label>Contraseña actual<input type="password" required value={currentPassword} onChange={(event) => setCurrentPassword(event.target.value)} /></label><label>Nueva contraseña<input type="password" minLength="8" required value={newPassword} onChange={(event) => setNewPassword(event.target.value)} /></label><label>Repetir nueva contraseña<input type="password" minLength="8" required value={confirmation} onChange={(event) => setConfirmation(event.target.value)} /></label>{message && <p className="profile-action-message">{message}</p>}<button type="submit">Cambiar contraseña</button></form></section></main>;
}
export default Cuenta;
