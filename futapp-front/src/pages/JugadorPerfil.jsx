import { useEffect, useMemo, useRef, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { API_URL, apiFetch, assetUrl } from "../config/api";
import useAuth from "../context/useAuth";
import { tieneRol } from "../utils/roles";
import "./Profiles.css";
import FavoriteButton from "../components/FavoriteButton/FavoriteButton";
import ImageLightbox from "../components/ImageLightbox/ImageLightbox";
import EntityImage from "../components/EntityImage/EntityImage";
import { jugadorGenerico } from "../utils/defaultEntityImages";
import CopyableCode from "../components/CopyableCode/CopyableCode";
import TournamentIcon from "../components/TournamentIcon/TournamentIcon";

async function fetchJson(url, message) {
  const response = await apiFetch(url);
  if (!response.ok) throw new Error(message);
  return response.json();
}

function calcularEdad(fecha) {
  if (!fecha) return null;
  const nacimiento = new Date(`${fecha}T00:00:00`);
  if (Number.isNaN(nacimiento.getTime())) return null;
  const hoy = new Date();
  let edad = hoy.getFullYear() - nacimiento.getFullYear();
  if (hoy < new Date(hoy.getFullYear(), nacimiento.getMonth(), nacimiento.getDate())) edad--;
  return edad;
}

function JugadorPerfil() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { usuario } = useAuth();
  const [jugador, setJugador] = useState(null);
  const [equipo, setEquipo] = useState(null);
  const [partidos, setPartidos] = useState([]);
  const [inscripciones, setInscripciones] = useState([]);
  const [estadisticas, setEstadisticas] = useState(null);
  const [error, setError] = useState("");
  const [claimMessage, setClaimMessage] = useState("");
  const [ownPlayerId, setOwnPlayerId] = useState(null);
  const [organizerProfileExists, setOrganizerProfileExists] = useState(false);
  const [previewImage, setPreviewImage] = useState(null);
  const [showAllStats, setShowAllStats] = useState(false);
  const [expandedMatch, setExpandedMatch] = useState(null);
  const [matchGoals, setMatchGoals] = useState({});
  const [loadingMatch, setLoadingMatch] = useState(null);
  const visitedPlayer = useRef(null);

  const claimPlayer = async () => {
    const response = await apiFetch(`${API_URL}/reclamos/jugadores/${id}`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ idTorneo: equipo.idTorneo, mensaje: "Este perfil soy yo" }) });
    const payload = await response.json().catch(() => null);
    setClaimMessage(response.ok ? "Solicitud enviada para aprobación." : payload?.message || "No se pudo enviar la solicitud");
  };

  useEffect(() => {
    fetchJson(`${API_URL}/jugadores/${id}`, "No se encontró el jugador")
      .then(async (player) => {
        setJugador(player);
        const team = await fetchJson(`${API_URL}/equipos/${player.idEquipo}`, "No se pudo cargar el equipo");
        setEquipo(team);
        return Promise.all([
          fetchJson(`${API_URL}/partidos/torneo/${team.idTorneo}`, "No se pudieron cargar los partidos"),
          fetchJson(`${API_URL}/inscripciones/jugadores/${id}`, "No se pudo cargar la trayectoria"),
          fetchJson(`${API_URL}/jugadores/${id}/estadisticas`, "No se pudieron cargar las estadísticas"),
        ]);
      })
      .then(([matches, enrollments, playerStats]) => {
        setPartidos(Array.isArray(matches) ? matches : []);
        setInscripciones(Array.isArray(enrollments) ? enrollments : []);
        setEstadisticas(playerStats);
      })
      .catch((requestError) => setError(`${requestError.message}. Actualizá la página para reintentar.`));
  }, [id]);

  useEffect(() => {
    if (!jugador?.idJugador || visitedPlayer.current === id) return;
    visitedPlayer.current = id;
    fetch(`${API_URL}/jugadores/${id}/visitas`, { method: "POST" }).then((response) => response.ok ? response.json() : null)
      .then((data) => { if (data) setJugador((current) => current ? { ...current, visitas: data.visitas } : current); }).catch(() => null);
  }, [id, jugador?.idJugador]);

  useEffect(() => {
    if (!tieneRol(usuario, "JUGADOR")) return;
    apiFetch(`${API_URL}/perfil-jugador/mio`)
      .then((response) => response.ok ? response.json() : null)
      .then((profile) => setOwnPlayerId(profile?.idJugador ?? null))
      .catch(() => setOwnPlayerId(null));
  }, [usuario]);

  useEffect(() => {
    if (!tieneRol(usuario, "ORGANIZADOR") && !tieneRol(usuario, "ADMIN")) return;
    apiFetch(`${API_URL}/perfiles-organizador/mio`)
      .then((response) => setOrganizerProfileExists(response.ok))
      .catch(() => setOrganizerProfileExists(false));
  }, [usuario]);

  const isOwnProfile = ownPlayerId === Number(id);

  const toggleMatchSummary = async (partido) => {
    if (expandedMatch === partido.idPartido) {
      setExpandedMatch(null);
      return;
    }
    setExpandedMatch(partido.idPartido);
    if (matchGoals[partido.idPartido]) return;
    setLoadingMatch(partido.idPartido);
    try {
      const goals = await fetchJson(`${API_URL}/goles/partido/${partido.idPartido}`, "No se pudieron cargar los goles");
      setMatchGoals((current) => ({ ...current, [partido.idPartido]: Array.isArray(goals) ? goals : [] }));
    } catch {
      setMatchGoals((current) => ({ ...current, [partido.idPartido]: [] }));
    } finally {
      setLoadingMatch(null);
    }
  };

  const historial = useMemo(() => equipo ? partidos.filter((partido) =>
    partido.idEquipoLocal === equipo.idEquipo || partido.idEquipoVisitante === equipo.idEquipo
  ) : [], [partidos, equipo]);
  const tieneEstadisticas = estadisticas && [estadisticas.partidos, estadisticas.goles, estadisticas.amarillas, estadisticas.rojas,
    estadisticas.vecesMvp, estadisticas.torneosDisputados, estadisticas.equiposRepresentados,
    estadisticas.vecesGoleador, estadisticas.vallasInvictas].some((valor) => Number(valor) > 0)
    || estadisticas?.promedioGolesPorPartido != null;
  const tarjetasEstadisticas = estadisticas ? [
    estadisticas.goles > 0 && { clave: "goles", valor: estadisticas.goles, etiqueta: "Goles", sticker: "⚽", detalle: "Goles oficiales registrados" },
    estadisticas.vecesMvp > 0 && { clave: "mvp", valor: estadisticas.vecesMvp, etiqueta: "MVP", sticker: "⭐", detalle: "Veces elegido figura del partido" },
    estadisticas.campeonatos.length > 0 && { clave: "campeonatos", valor: estadisticas.campeonatos.length, etiqueta: "Campeonatos", sticker: "🏆", detalle: "Torneos ganados con sus equipos" },
    estadisticas.vecesGoleador > 0 && { clave: "goleador", valor: estadisticas.vecesGoleador, etiqueta: "Goleador de torneo", sticker: "👑", detalle: "Torneos finalizados como máximo goleador" },
    estadisticas.vallasInvictas > 0 && { clave: "vallas", valor: estadisticas.vallasInvictas, etiqueta: "Vallas invictas", sticker: "🧤", detalle: "Partidos del equipo sin recibir goles" },
    estadisticas.promedioGolesPorPartido != null && { clave: "promedio", valor: estadisticas.promedioGolesPorPartido, etiqueta: "Goles por partido", sticker: "📈", detalle: "Promedio en los partidos computados" },
    estadisticas.partidos > 0 && { clave: "partidos", valor: estadisticas.partidos, etiqueta: "Partidos", sticker: "📅", detalle: "Partidos jugados por sus equipos mientras estuvo inscripto" },
    estadisticas.torneosDisputados > 0 && { clave: "torneos", valor: estadisticas.torneosDisputados, etiqueta: "Torneos", sticker: "🏟️", detalle: "Competencias en las que estuvo inscripto" },
    estadisticas.equiposRepresentados > 0 && { clave: "equipos", valor: estadisticas.equiposRepresentados, etiqueta: "Equipos", sticker: "🛡️", detalle: "Clubes representados en su trayectoria" },
    estadisticas.amarillas > 0 && { clave: "amarillas", valor: estadisticas.amarillas, etiqueta: "Amarillas", sticker: "🟨", detalle: "Tarjetas amarillas recibidas", clase: "yellow" },
    estadisticas.rojas > 0 && { clave: "rojas", valor: estadisticas.rojas, etiqueta: "Rojas", sticker: "🟥", detalle: "Expulsiones registradas", clase: "red" },
  ].filter(Boolean) : [];
  const tarjetasPrincipales = tarjetasEstadisticas.slice(0, 4);

  if (error) return <main className="profile-page"><div className="profile-message">{error}</div></main>;
  if (!jugador || !equipo) return <main className="profile-page"><div className="profile-message">Cargando perfil...</div></main>;

  return <main className="profile-page player-profile">
    <button type="button" className="profile-back-link profile-back-button" onClick={() => navigate(-1)}>← Volver</button>
    <section className="player-hero">
      <button type="button" className="profile-avatar player-avatar image-preview-trigger" onClick={() => setPreviewImage({ src: jugador.foto ? assetUrl(jugador.foto) : jugadorGenerico, alt: `${jugador.nombre} ${jugador.apellido}` })} aria-label={`Ampliar foto de ${jugador.nombre} ${jugador.apellido}`}><EntityImage type="player" value={jugador.foto} alt={`${jugador.nombre} ${jugador.apellido}`} /></button>
      <div><CopyableCode className="profile-code" value={jugador.codigoPublico} /><h1>{jugador.nombre} {jugador.apellido} {jugador.verificado && <span className="verified-badge" title="Perfil vinculado con la cuenta del jugador">✓ Verificado</span>}</h1>{jugador.apodo && <p className="player-nickname">“{jugador.apodo}”</p>}<Link className="player-current-team" to={`/equipo/${equipo.idEquipo}`}><EntityImage type="team" value={equipo.escudo} alt="" /><span>{equipo.nombre}</span></Link></div>
      <div className="player-hero-side"><strong className="shirt-number">#{jugador.numeroCamiseta}</strong><FavoriteButton type="jugadores" id={jugador.idJugador} /><span className="profile-views">👁 {jugador.visitas || 0} visitas</span></div>
      {isOwnProfile ? <div className="player-profile-actions"><Link className="profile-edit-link" to="/mi-perfil-jugador/editar">Editar perfil de jugador</Link>{(tieneRol(usuario, "ORGANIZADOR") || tieneRol(usuario, "ADMIN")) && <Link className="profile-organizer-link" to={organizerProfileExists ? "/mi-perfil-organizador" : "/mi-perfil-organizador/editar"}>{organizerProfileExists ? "Ver perfil de organizador" : "Generar perfil de organizador"}</Link>}</div> : tieneRol(usuario, "JUGADOR") && !jugador.verificado && <button className="profile-claim-button" onClick={claimPlayer}>Este jugador soy yo</button>}
    </section>
    {claimMessage && <p className="profile-action-message">{claimMessage}</p>}
    <section className="player-facts"><div><span>Posición</span><strong>{jugador.posicion}</strong></div><div><span>Edad</span><strong>{calcularEdad(jugador.fechaNacimiento) ?? "—"}</strong></div><div><span>Nacimiento</span><strong>{jugador.fechaNacimiento || "—"}</strong></div><div><span>Pierna hábil</span><strong>{jugador.piernaHabil || "—"}</strong></div></section>
    {inscripciones.length > 0 && <section className="profile-panel">
      <h2>Torneos</h2>
      <div className="player-tournament-grid">
        {inscripciones.length === 0 ? <p>Todavía no registra participaciones.</p> : inscripciones.map((item) => {
          const activa = item.estado === "ACTIVA";
          return <article className={`player-tournament-card ${activa ? "active" : "historical"}`} key={item.idInscripcionJugador}>
            <Link className="player-tournament-main" to={`/torneo/${item.idTorneo}`}>
              <TournamentIcon />
              <span><small>Torneo</small><strong>{item.nombreTorneo}</strong></span>
              <b className={`career-status ${activa ? "active" : "historical"}`}>{activa ? "Activo" : "Baja"}</b>
            </Link>
            <div className="player-tournament-meta">
              <Link to={`/equipo/${item.idEquipo}`}><EntityImage type="team" value={item.escudoEquipo} alt="" /><span>{item.nombreEquipo}</span></Link>
              <small>#{item.numeroCamiseta || "—"} · {item.posicion || "Sin posición"}{item.fechaBaja ? ` · Baja ${item.fechaBaja}` : ""}</small>
            </div>
          </article>;
        })}
      </div>
    </section>}
    {tieneEstadisticas && <section className="profile-panel"><h2>Estadísticas</h2><div className="player-stats">{tarjetasPrincipales.map((tarjeta) => <div className={tarjeta.clase ? `discipline-stat ${tarjeta.clase}` : ""} key={tarjeta.clave}><span className="stat-content"><strong>{tarjeta.valor}</strong><span>{tarjeta.etiqueta}</span></span></div>)}</div>{tarjetasEstadisticas.length > 4 && <button type="button" className="stats-toggle stats-toggle-bottom" onClick={() => setShowAllStats(true)}>Ver todas las estadísticas →</button>}</section>}
    {historial.length > 0 && <section className="profile-panel club-matches-panel"><div className="profile-section-head"><div><span>Actividad</span><h2>Historial de partidos</h2></div></div><p className="profile-note">Se muestran los partidos del plantel mientras estuvo inscripto.</p><div className="profile-match-list club-match-list">{historial.slice().reverse().map((partido) => { const abierto = expandedMatch === partido.idPartido; const goals = matchGoals[partido.idPartido] || []; return <article className={`club-match-item ${abierto ? "open" : ""}`} key={partido.idPartido}><button type="button" className="club-match-row" onClick={() => toggleMatchSummary(partido)} aria-expanded={abierto}><span className="profile-match-team"><EntityImage type="team" value={partido.escudoEquipoLocal} alt="" /><span>{partido.nombreEquipoLocal}</span></span><strong>{["JUGADO", "FINALIZADO"].includes(partido.estado?.toUpperCase()) ? `${partido.golesLocal} - ${partido.golesVisitante}` : "vs"}</strong><span className="profile-match-team profile-match-team-away"><span>{partido.nombreEquipoVisitante}</span><EntityImage type="team" value={partido.escudoEquipoVisitante} alt="" /></span><span className="club-match-chevron" aria-hidden="true">⌄</span></button>{abierto && <div className="club-match-summary">{loadingMatch === partido.idPartido ? <p>Cargando resumen...</p> : goals.length > 0 ? <div className="club-match-scorers"><div>{goals.filter((gol) => gol.idEquipo === partido.idEquipoLocal).map((gol) => <span key={gol.idGol}><b>{gol.minuto ? `${gol.minuto}'` : "Gol"}</b> {gol.nombreJugador}</span>)}</div><div>{goals.filter((gol) => gol.idEquipo === partido.idEquipoVisitante).map((gol) => <span key={gol.idGol}><b>{gol.minuto ? `${gol.minuto}'` : "Gol"}</b> {gol.nombreJugador}</span>)}</div></div> : <p>{["JUGADO", "FINALIZADO"].includes(partido.estado?.toUpperCase()) ? "No hay goleadores cargados." : "Partido programado."}</p>}<div className="club-match-summary-footer"><small>{partido.fechaHora ? new Date(partido.fechaHora).toLocaleString("es-AR", { day: "2-digit", month: "short", hour: "2-digit", minute: "2-digit" }) : "Fecha a confirmar"}{partido.cancha ? ` · ${partido.cancha}` : ""}</small><Link to={`/partido/${partido.idPartido}`}>Ver detalle completo →</Link></div></div>}</article>; })}</div></section>}
    {showAllStats && <div className="player-stats-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) setShowAllStats(false); }}><section className="player-stats-modal" role="dialog" aria-modal="true" aria-labelledby="player-stats-title"><header><div><span>Rendimiento</span><h2 id="player-stats-title">Todas las estadísticas</h2></div><button type="button" onClick={() => setShowAllStats(false)} aria-label="Cerrar estadísticas">✕</button></header><div className="player-stats-detail">{tarjetasEstadisticas.map((tarjeta) => <article className={tarjeta.clase ? `discipline-stat ${tarjeta.clase}` : ""} key={tarjeta.clave}><span className="stat-sticker" aria-hidden="true">{tarjeta.sticker}</span><span className="stat-content"><strong>{tarjeta.valor}</strong><span>{tarjeta.etiqueta}</span><small>{tarjeta.detalle}</small></span></article>)}</div></section></div>}
    {previewImage && <ImageLightbox {...previewImage} onClose={() => setPreviewImage(null)} />}
  </main>;
}

export default JugadorPerfil;
