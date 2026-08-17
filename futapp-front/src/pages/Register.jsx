import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import useAuth from "../context/useAuth";

function Register() {
  const [nombre, setNombre] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const { register } = useAuth();
  const navigate = useNavigate();

  const manejarRegistro = async (e) => {
    e.preventDefault();

    setError("");
    try { await register(nombre, email, password); navigate("/"); } catch (registerError) { setError(registerError.message); }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1>Registrarse</h1>
        <p>Creá tu cuenta de prueba en FUTAPP.</p>

        <form onSubmit={manejarRegistro}>
          <label>Nombre</label>
          <input
            type="text"
            placeholder="Tu nombre"
            value={nombre}
            onChange={(e) => setNombre(e.target.value)}
            required
          />

          <label>Email</label>
          <input
            type="email"
            placeholder="tu@email.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />

          <label>Contraseña</label><input type="password" minLength="8" value={password} onChange={(e) => setPassword(e.target.value)} required />
          {error && <p className="tournament-form-error">{error}</p>}

          <button type="submit" className="auth-btn">
            Crear cuenta
          </button>
        </form>

        <p className="auth-link">
          ¿Ya tenés cuenta? <Link to="/login">Ingresar</Link>
        </p>
      </div>
    </div>
  );
}

export default Register;
