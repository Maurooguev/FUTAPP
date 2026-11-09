import { useState } from "react";
import { Navigate } from "react-router-dom";
import { API_URL, apiFetch } from "../config/api";
import useAuth from "../context/useAuth";
import { tieneRol } from "../utils/roles";

function AdminOrganizadores() {
  const { usuario } = useAuth();
  const [nombre, setNombre] = useState("");
  const [email, setEmail] = useState("");
  const [invitation, setInvitation] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  if (!usuario) return <Navigate to="/login" replace />;
  if (!tieneRol(usuario, "ADMIN")) return <Navigate to="/mis-torneos" replace />;

  const submit = async (event) => {
    event.preventDefault();
    setLoading(true); setError(""); setInvitation(null);
    const response = await apiFetch(`${API_URL}/admin/organizers`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ nombre, email }) });
    const payload = await response.json().catch(() => null);
    setLoading(false);
    if (!response.ok) return setError(payload?.message || "No se pudo crear la invitación");
    setInvitation(payload); setNombre(""); setEmail("");
  };

  const copy = async () => {
    await navigator.clipboard.writeText(`Acceso FUTAPP\nEmail: ${invitation.email}\nContraseña temporal: ${invitation.temporaryPassword}\nIngresá en: ${window.location.origin}/login`);
  };

  return <main className="profile-page"><div className="account-heading"><span>Administración</span><h1>Invitar organizador</h1><p>Creá una cuenta privada y entregá una contraseña temporal de un solo uso.</p></div><section className="profile-panel account-security"><form className="personal-profile-form" onSubmit={submit}><label>Nombre del organizador<input required maxLength="100" value={nombre} onChange={(event) => setNombre(event.target.value)} /></label><label>Email<input type="email" required value={email} onChange={(event) => setEmail(event.target.value)} /></label>{error && <p className="profile-action-message">{error}</p>}<button disabled={loading}>{loading ? "Creando…" : "Crear acceso"}</button></form>{invitation && <div className="auth-dev-link"><span>Acceso creado</span><p>Esta contraseña se muestra una sola vez. El organizador deberá cambiarla al ingresar.</p><strong>{invitation.email}</strong><code>{invitation.temporaryPassword}</code><button type="button" onClick={copy}>Copiar invitación</button></div>}</section></main>;
}

export default AdminOrganizadores;
