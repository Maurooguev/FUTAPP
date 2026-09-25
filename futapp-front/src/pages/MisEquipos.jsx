import { useEffect, useState } from "react";
import { Link, Navigate } from "react-router-dom";
import { API_URL, apiFetch, assetUrl } from "../config/api";
import useAuth from "../context/useAuth";
import "./MisEquipos.css";
import TeamMergeModal from "../components/TeamManagement/TeamMergeModal";
import plantelGenerico from "../assets/plantel-generico.png";
import EntityImage from "../components/EntityImage/EntityImage";
import { tieneRol } from "../utils/roles";

function MisEquipos() {
  const { usuario } = useAuth();
  const [equipos, setEquipos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showMerge, setShowMerge] = useState(false);

  useEffect(() => {
    if (!usuario) return;
    apiFetch(`${API_URL}/equipos/mios`)
      .then(async (response) => {
        const body = await response.json().catch(() => null);
        if (!response.ok) throw new Error(body?.message || "No se pudieron cargar tus equipos");
        setEquipos(Array.isArray(body) ? body : []);
      })
      .catch((requestError) => setError(requestError.message))
      .finally(() => setLoading(false));
  }, [usuario]);

  if (!usuario) return <Navigate to="/login" replace />;
  if (!tieneRol(usuario, "DIRECTOR_EQUIPO") && !tieneRol(usuario, "ADMIN")) return <Navigate to="/" replace />;

  return <main className="my-teams-page"><div className="my-teams-shell">
    <div className="my-teams-heading"><div><span>Panel del director</span><h1>Mis equipos</h1><p>Gestioná los perfiles y planteles de los clubes que tenés aprobados.</p></div><div className="my-teams-heading-actions">{equipos.length >= 2 && <button onClick={() => setShowMerge(true)}>Fusionar duplicados</button>}<Link to="/solicitudes">Solicitudes de jugadores</Link></div></div>
    {error && <div className="profile-message">{error}</div>}
    {loading ? <p>Cargando equipos...</p> : equipos.length === 0 ? <section className="my-teams-empty"><h2>Todavía no administrás ningún equipo</h2><p>Entrá al perfil de un club, reclamalo y esperá la aprobación del organizador.</p><Link to="/">Explorar torneos</Link></section> : <div className="my-teams-grid">{equipos.map((equipo) => <article className="my-team-card" key={equipo.idEquipo}>
      <Link className="my-team-cover" to={`/equipo/${equipo.idEquipo}`}><img src={equipo.fotoPlantel ? assetUrl(equipo.fotoPlantel) : plantelGenerico} alt={equipo.fotoPlantel ? `Plantel de ${equipo.nombre}` : `Fondo genérico del plantel de ${equipo.nombre}`} onError={(event) => { event.currentTarget.onerror = null; event.currentTarget.src = plantelGenerico; }} /></Link>
      <div className="my-team-body"><Link className="my-team-avatar" to={`/equipo/${equipo.idEquipo}`}><EntityImage type="team" value={equipo.escudo} alt={`Escudo de ${equipo.nombre}`} /></Link><div><small>{equipo.codigoPublico}</small><Link to={`/equipo/${equipo.idEquipo}`}><h2>{equipo.nombre}</h2></Link><p>{equipo.barrio || "Barrio sin informar"} · {equipo.nombreTorneo}</p></div></div>
      <div className="my-team-actions"><Link to={`/equipo/${equipo.idEquipo}`}>Ver perfil</Link><Link className="primary" to={`/equipo/${equipo.idEquipo}`}>Gestionar equipo</Link></div>
    </article>)}</div>}
    {showMerge && <TeamMergeModal equipos={equipos} onClose={() => setShowMerge(false)} onMerged={(_, duplicateId) => setEquipos((current) => current.filter((team) => team.idEquipo !== duplicateId))} />}
  </div></main>;
}

export default MisEquipos;
