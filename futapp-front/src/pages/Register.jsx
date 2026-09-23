import { useState } from "react";
import { useNavigate, Link, useSearchParams } from "react-router-dom";
import useAuth from "../context/useAuth";

function Register() {
  const [nombre, setNombre] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [rol, setRol] = useState("JUGADOR");
  const [error, setError] = useState("");

  const { register } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const next = searchParams.get("next");
  const safeNext = next?.startsWith("/") && !next.startsWith("//") ? next : "/";
  const intent = searchParams.get("intent");
  const intentCopy = {
    "crear-torneo": "Creá tu cuenta y empezá a armar tu torneo.",
    "guardar-favoritos": "Creá tu cuenta para guardar torneos, equipos y jugadores.",
    "crear-perfil": "Creá tu cuenta para generar y completar tu perfil.",
  }[intent];

  const manejarRegistro = async (e) => {
    e.preventDefault();

    setError("");
    try { await register(nombre, email, password, rol); navigate(safeNext, { replace: true }); } catch (registerError) { setError(registerError.message); }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1>Registrarse</h1>
        <p>{intentCopy || "Creá tu cuenta en FUTAPP."}</p>

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
          <label>Tipo de cuenta</label><select value={rol} onChange={(e) => setRol(e.target.value)}><option value="JUGADOR">Jugador</option><option value="DIRECTOR_EQUIPO">Director de equipo</option><option value="ORGANIZADOR">Organizador de torneo</option></select>
          {error && <p className="tournament-form-error">{error}</p>}

          <button type="submit" className="auth-btn">
            Crear cuenta
          </button>
        </form>

        <p className="auth-link">
          ¿Ya tenés cuenta? <Link to={`/login?next=${encodeURIComponent(safeNext)}${intent ? `&intent=${encodeURIComponent(intent)}` : ""}`}>Ingresar</Link>
        </p>
      </div>
    </div>
  );
}

export default Register;
