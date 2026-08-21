import { useState } from "react";
import { Link } from "react-router-dom";
import { API_URL } from "../config/api";

function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [message, setMessage] = useState("");
  const [resetToken, setResetToken] = useState("");
  const [loading, setLoading] = useState(false);
  const submit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setResetToken("");
    const response = await fetch(`${API_URL}/auth/password/forgot`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ email }) });
    const data = await response.json().catch(() => null);
    setMessage(data?.mensaje || data?.message || "No se pudo procesar la solicitud.");
    if (response.ok && data?.tokenDesarrollo) setResetToken(data.tokenDesarrollo);
    setLoading(false);
  };
  return <div className="auth-page"><div className="auth-card"><h1>Recuperar contraseña</h1><p>Ingresá el email de tu cuenta. El enlace vencerá en 30 minutos.</p><form onSubmit={submit}><label>Email</label><input type="email" required value={email} onChange={(event) => setEmail(event.target.value)} />{message && <p className="auth-feedback">{message}</p>}<button className="auth-btn" disabled={loading}>{loading ? "Generando…" : "Enviar enlace"}</button></form>{resetToken && <div className="auth-dev-link"><span>Modo desarrollo</span><p>Hasta conectar el correo, usá este enlace de prueba:</p><Link to={`/reset-password?token=${resetToken}`}>Restablecer contraseña →</Link></div>}<p className="auth-link"><Link to="/login">Volver a ingresar</Link></p></div></div>;
}
export default ForgotPassword;
