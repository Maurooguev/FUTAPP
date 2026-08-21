import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import useAuth from "../context/useAuth";

function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const { login } = useAuth();
  const navigate = useNavigate();

  const manejarLogin = async (e) => {
    e.preventDefault();
    setError("");
    try { await login(email, password); navigate("/"); } catch (loginError) { setError(loginError.message); }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1>Ingresar</h1>
        <p>Ingresá con tu cuenta de FUTAPP.</p>

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
          ¿No tenés cuenta? <Link to="/register">Registrarse</Link>
        </p>
      </div>
    </div>
  );
}

export default Login;
