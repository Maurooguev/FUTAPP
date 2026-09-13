import { useEffect, useState } from "react";
import { Link, Navigate } from "react-router-dom";
import { API_URL, apiFetch } from "../config/api";
import useAuth from "../context/useAuth";
import { tieneRol } from "../utils/roles";
import "./Profiles.css";

function MiPerfilRedirect() {
  const { usuario } = useAuth();
  const [profile, setProfile] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    apiFetch(`${API_URL}/perfil-jugador/mio`)
      .then(async (response) => {
        const body = await response.json().catch(() => null);
        if (!response.ok) throw new Error(body?.message || "No se pudo abrir tu perfil");
        setProfile(body);
      })
      .catch((requestError) => setError(requestError.message));
  }, []);

  if (profile) return <Navigate to={`/jugador/${profile.idJugador}`} replace />;
  if (error && (tieneRol(usuario, "ORGANIZADOR") || tieneRol(usuario, "ADMIN"))) {
    return <main className="profile-page"><div className="profile-message profile-organizer-fallback"><span>Perfil de organizador</span><h1>Creá tu perfil público</h1><p>Tu cuenta no está vinculada a un jugador, pero sí organiza torneos. Podés generar un perfil para mostrar tu descripción, fotos y competencias publicadas.</p><Link to="/mi-perfil-organizador">Generar perfil de organizador</Link></div></main>;
  }
  return <main className="profile-page"><div className="profile-message">{error ? <><p>{error}</p><Link to="/">Volver al inicio</Link></> : "Abriendo tu perfil..."}</div></main>;
}

export default MiPerfilRedirect;
