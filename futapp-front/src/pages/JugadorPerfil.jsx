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
  const [goles, setGoles] = useState([]);
  const [tarjetas, setTarjetas] = useState([]);
  const [inscripciones, setInscripciones] = useState([]);
  const [error, setError] = useState("");
  const [claimMessage, setClaimMessage] = useState("");
  const [ownPlayerId, setOwnPlayerId] = useState(null);
  const [organizerProfileExists, setOrganizerProfileExists] = useState(false);
  const [previewImage, setPreviewImage] = useState(null);
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
          fetchJson(`${API_URL}/goles/jugador/${id}`, "No se pudieron cargar los goles"),
          fetchJson(`${API_URL}/tarjetas/jugador/${id}`, "No se pudieron cargar las tarjetas"),
          fetchJson(`${API_URL}/inscripciones/jugadores/${id}`, "No se pudo cargar la trayectoria"),
        ]);
      })
      .then(([matches, goals, cards, enrollments]) => {
        setPartidos(Array.isArray(matches) ? matches : []);
        setGoles(Array.isArray(goals) ? goals : []);
        setTarjetas(Array.isArray(cards) ? cards : []);
        setInscripciones(Array.isArray(enrollments) ? enrollments : []);
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

  const historial = useMemo(() => equipo ? partidos.filter((partido) =>
    partido.idEquipoLocal === equipo.idEquipo || partido.idEquipoVisitante === equipo.idEquipo
  ) : [], [partidos, equipo]);
  const equiposTrayectoria = useMemo(() => {
    const agrupados = new Map();
    inscripciones.forEach((item) => {
      const actual = agrupados.get(item.idEquipo) || { idEquipo: item.idEquipo, nombreEquipo: item.nombreEquipo, torneos: 0 };
      actual.torneos += 1;
      agrupados.set(item.idEquipo, actual);
    });
    if (equipo && !agrupados.has(equipo.idEquipo)) {
      agrupados.set(equipo.idEquipo, { idEquipo: equipo.idEquipo, nombreEquipo: equipo.nombre, torneos: 0 });
    }
    return [...agrupados.values()].map((item) => ({ ...item, actual: item.idEquipo === jugador?.idEquipo }));
  }, [inscripciones, equipo, jugador?.idEquipo]);
  const amarillas = tarjetas.filter((tarjeta) => tarjeta.tipoTarjeta === "AMARILLA").length;
  const rojas = tarjetas.filter((tarjeta) => tarjeta.tipoTarjeta === "ROJA").length;

  if (error) return <main className="profile-page"><div className="profile-message">{error}</div></main>;
  if (!jugador || !equipo) return <main className="profile-page"><div className="profile-message">Cargando perfil...</div></main>;

  return <main className="profile-page player-profile">
    <button type="button" className="profile-back-link profile-back-button" onClick={() => navigate(-1)}>← Volver</button>
    <section className="player-hero">
      <button type="button" className="profile-avatar player-avatar image-preview-trigger" onClick={() => setPreviewImage({ src: jugador.foto ? assetUrl(jugador.foto) : jugadorGenerico, alt: `${jugador.nombre} ${jugador.apellido}` })} aria-label={`Ampliar foto de ${jugador.nombre} ${jugador.apellido}`}><EntityImage type="player" value={jugador.foto} alt={`${jugador.nombre} ${jugador.apellido}`} /></button>
      <div><CopyableCode className="profile-code" value={jugador.codigoPublico} /><h1>{jugador.nombre} {jugador.apellido} {jugador.verificado && <span className="verified-badge" title="Perfil vinculado con la cuenta del jugador">✓ Verificado</span>}</h1>{jugador.apodo && <p className="player-nickname">“{jugador.apodo}”</p>}<Link to={`/equipo/${equipo.idEquipo}`}>{equipo.nombre}</Link></div>
      <div className="player-hero-side"><strong className="shirt-number">#{jugador.numeroCamiseta}</strong><FavoriteButton type="jugadores" id={jugador.idJugador} /><span className="profile-views">👁 {jugador.visitas || 0} visitas</span></div>
      {isOwnProfile ? <div className="player-profile-actions"><Link className="profile-edit-link" to="/mi-perfil-jugador/editar">Editar perfil de jugador</Link>{(tieneRol(usuario, "ORGANIZADOR") || tieneRol(usuario, "ADMIN")) && <Link className="profile-organizer-link" to={organizerProfileExists ? "/mi-perfil-organizador" : "/mi-perfil-organizador/editar"}>{organizerProfileExists ? "Ver perfil de organizador" : "Generar perfil de organizador"}</Link>}</div> : tieneRol(usuario, "JUGADOR") && !jugador.verificado && <button className="profile-claim-button" onClick={claimPlayer}>Este jugador soy yo</button>}
    </section>
    {claimMessage && <p className="profile-action-message">{claimMessage}</p>}
    <section className="player-facts"><div><span>Posición</span><strong>{jugador.posicion}</strong></div><div><span>Edad</span><strong>{calcularEdad(jugador.fechaNacimiento) ?? "—"}</strong></div><div><span>Nacimiento</span><strong>{jugador.fechaNacimiento || "—"}</strong></div><div><span>Pierna hábil</span><strong>{jugador.piernaHabil || "—"}</strong></div></section>
    <section className="profile-panel"><h2>Equipos</h2><p className="profile-note">Clubes que representa actualmente y equipos que integró anteriormente.</p><div className="career-list team-career-list">{equiposTrayectoria.map((item) => <article key={item.idEquipo}><Link className="career-team-link" to={`/equipo/${item.idEquipo}`}>{item.nombreEquipo}</Link><strong className={`career-status ${item.actual ? "active" : "historical"}`}>{item.actual ? "Equipo actual" : "Histórico"}</strong><small>{item.torneos} {item.torneos === 1 ? "torneo registrado" : "torneos registrados"}</small></article>)}</div></section>
    <section className="profile-panel"><h2>Torneos</h2><p className="profile-note">Los torneos activos se separan de las participaciones que ya finalizaron o en las que fue dado de baja.</p><div className="career-list tournament-career-list">{inscripciones.length === 0 ? <p>Todavía no registra participaciones.</p> : inscripciones.map((item) => <article className={item.estado === "ACTIVA" ? "active" : "historical"} key={item.idInscripcionJugador}><Link className="career-team-link" to={`/equipo/${item.idEquipo}`}>{item.nombreEquipo}</Link><Link className="career-tournament-link" to={`/torneo/${item.idTorneo}`}><span>{item.nombreTorneo}</span><b>Ver torneo →</b></Link><div className="career-enrollment-detail"><strong className={`career-status ${item.estado === "ACTIVA" ? "active" : "historical"}`}>{item.estado === "ACTIVA" ? "Activo" : "Baja"}</strong><small>#{item.numeroCamiseta || "—"} · {item.posicion || "Sin posición"}{item.fechaBaja ? ` · ${item.fechaBaja}` : ""}</small></div></article>)}</div></section>
    <section className="profile-panel"><h2>Estadísticas</h2><div className="player-stats"><div><strong>{historial.filter((p) => p.estado === "JUGADO" || p.estado === "FINALIZADO").length}</strong><span>Partidos del plantel</span></div><div><strong>{goles.length}</strong><span>Goles</span></div><div><strong>{amarillas}</strong><span>Amarillas</span></div><div><strong>{rojas}</strong><span>Rojas</span></div></div></section>
    <section className="profile-panel"><h2>Historial de partidos</h2><p className="profile-note">Primera versión: se muestran los partidos del plantel. Más adelante registraremos convocatorias y minutos individuales.</p><div className="profile-match-list">{historial.length === 0 ? <p>Todavía no hay partidos.</p> : historial.slice().reverse().map((partido) => <Link to={`/partido/${partido.idPartido}`} key={partido.idPartido}><span className="profile-match-team"><EntityImage type="team" value={partido.escudoEquipoLocal} alt="" /><span>{partido.nombreEquipoLocal}</span></span><strong>{partido.estado === "JUGADO" || partido.estado === "FINALIZADO" ? `${partido.golesLocal} - ${partido.golesVisitante}` : "vs"}</strong><span className="profile-match-team profile-match-team-away"><span>{partido.nombreEquipoVisitante}</span><EntityImage type="team" value={partido.escudoEquipoVisitante} alt="" /></span></Link>)}</div></section>
    {previewImage && <ImageLightbox {...previewImage} onClose={() => setPreviewImage(null)} />}
  </main>;
}

export default JugadorPerfil;
