import { useState } from "react";
import { useNavigate, Link, useSearchParams } from "react-router-dom";
import useAuth from "../context/useAuth";

function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const { login } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const next = searchParams.get("next");
  const safeNext = next?.startsWith("/") && !next.startsWith("//") ? next : "/";
  const intent = searchParams.get("intent");

  const manejarLogin = async (e) => {
    e.preventDefault();
    setError("");
    try { await login(email, password); navigate(safeNext, { replace: true }); } catch (loginError) { setError(loginError.message); }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1>Ingresar</h1>
        <p>{intent ? "Ingresá para continuar con lo que elegiste." : "Ingresá con tu cuenta de FUTAPP."}</p>

        <form onSubmit={manejarLogin}>
          <label>Email</label>
          <input
            type="email" required value={email} onChange={(e) => setEmail(e.target.value)}
          />
          <label>Contraseña</label><input type="password" required value={password} onChange={(e) => setPassword(e.target.value)} />
          <Link className="auth-forgot-link" to="/forgot-password">Olvidé mi contraseña</Link>
          {error && <p className="tournament-form-error">{error}</p>}

          <button type="submit" className="auth-btn">
            Ingresar
          </button>
        </form>

        <p className="auth-link">
          ¿No tenés cuenta? <Link to={`/register?next=${encodeURIComponent(safeNext)}${intent ? `&intent=${encodeURIComponent(intent)}` : ""}`}>Registrarse</Link>
        </p>
      </div>
    </div>
  );
}

export default Login;
