import { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { API_URL } from "../config/api";

function ResetPassword() {
  const [params] = useSearchParams();
  const [password, setPassword] = useState("");
  const [confirmation, setConfirmation] = useState("");
  const [error, setError] = useState("");
  const [done, setDone] = useState(false);
  const navigate = useNavigate();
  const token = params.get("token") || "";
  const submit = async (event) => {
    event.preventDefault();
    if (password !== confirmation) return setError("Las contraseñas no coinciden");
    const response = await fetch(`${API_URL}/auth/password/reset`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ token, nuevaPassword: password }) });
    const data = await response.json().catch(() => null);
    if (!response.ok) return setError(data?.message || "No se pudo restablecer la contraseña");
    setDone(true);
    setTimeout(() => navigate("/login"), 1800);
  };
  return <div className="auth-page"><div className="auth-card"><h1>Nueva contraseña</h1><p>Elegí una contraseña segura de al menos 10 caracteres.</p>{!token ? <p className="tournament-form-error">El enlace no contiene un token válido.</p> : done ? <div className="auth-success">Contraseña actualizada. Te estamos llevando al ingreso…</div> : <form onSubmit={submit}><label>Nueva contraseña</label><input type="password" minLength="10" required value={password} onChange={(event) => setPassword(event.target.value)} /><label>Repetir contraseña</label><input type="password" minLength="10" required value={confirmation} onChange={(event) => setConfirmation(event.target.value)} />{error && <p className="tournament-form-error">{error}</p>}<button className="auth-btn">Guardar contraseña</button></form>}<p className="auth-link"><Link to="/login">Volver a ingresar</Link></p></div></div>;
}
export default ResetPassword;
