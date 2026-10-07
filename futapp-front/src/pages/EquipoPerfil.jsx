import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { API_URL, apiFetch, assetUrl } from "../config/api";
import useAuth from "../context/useAuth";
import { tieneRol } from "../utils/roles";
import "./Profiles.css";
import TeamFormModal from "../components/TeamManagement/TeamFormModal";
import TeamRosterModal from "../components/TeamManagement/TeamRosterModal";
import FavoriteButton from "../components/FavoriteButton/FavoriteButton";
import ImageLightbox from "../components/ImageLightbox/ImageLightbox";
import plantelGenerico from "../assets/plantel-generico.png";
import EntityImage from "../components/EntityImage/EntityImage";
import { equipoGenerico } from "../utils/defaultEntityImages";
import CopyableCode from "../components/CopyableCode/CopyableCode";

async function fetchJson(url, message) {
  const response = await apiFetch(url);
  if (!response.ok) throw new Error(message);
  return response.json();
}

function EquipoPerfil() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { usuario } = useAuth();
  const [equipo, setEquipo] = useState(null);
  const [jugadores, setJugadores] = useState([]);
  const [partidos, setPartidos] = useState([]);
  const [inscripciones, setInscripciones] = useState([]);
  const [titulos, setTitulos] = useState([]);
  const [error, setError] = useState("");
  const [claimMessage, setClaimMessage] = useState("");
  const [canManage, setCanManage] = useState(false);
  const [showEdit, setShowEdit] = useState(false);
  const [showRoster, setShowRoster] = useState(false);
  const [previewImage, setPreviewImage] = useState(null);

  const claimTeam = async () => {
    const response = await apiFetch(`${API_URL}/reclamos/equipos/${id}`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ idTorneo: equipo.idTorneo, mensaje: "Solicito administrar este equipo" }) });
    const payload = await response.json().catch(() => null);
    setClaimMessage(response.ok ? "Solicitud enviada al organizador." : payload?.message || "No se pudo enviar la solicitud");
  };

  useEffect(() => {
    fetchJson(`${API_URL}/equipos/${id}`, "No se encontró el equipo")
      .then(async (team) => {
        setEquipo(team);
        const [matches, enrollments, championships] = await Promise.all([
          fetchJson(`${API_URL}/partidos/torneo/${team.idTorneo}`, "No se pudieron cargar los partidos"),
          fetchJson(`${API_URL}/inscripciones/equipos/${id}`, "No se pudo cargar el historial de torneos"),
          fetchJson(`${API_URL}/torneos/campeones/equipo/${id}`, "No se pudieron cargar los títulos"),
        ]);
        const currentEnrollment = enrollments.find((item) => item.idTorneo === team.idTorneo)
          ?? enrollments.find((item) => item.estado === "ACTIVA")
          ?? enrollments[0];
        const roster = currentEnrollment
          ? await fetchJson(`${API_URL}/inscripciones/${currentEnrollment.idInscripcion}/jugadores`, "No se pudo cargar el plantel")
          : [];
        return [roster.filter((player) => player.estado === "ACTIVA"), matches, enrollments, championships];
      })
      .then(([players, matches, enrollments, championships]) => {
        setJugadores(Array.isArray(players) ? players : []);
        setPartidos(Array.isArray(matches) ? matches : []);
        setInscripciones(Array.isArray(enrollments) ? enrollments : []);
        setTitulos(Array.isArray(championships) ? championships : []);
      })
      .catch((requestError) => setError(`${requestError.message}. Actualizá la página para reintentar.`));
  }, [id]);

  useEffect(() => {
    if (!usuario) return;
    apiFetch(`${API_URL}/equipos/${id}/permisos`)
      .then((response) => response.ok ? response.json() : { puedeGestionar: false })
      .then((permissions) => setCanManage(Boolean(permissions.puedeGestionar)))
      .catch(() => setCanManage(false));
  }, [id, usuario]);

  const hasManageAccess = Boolean(usuario && canManage);

  const historial = useMemo(() => partidos.filter((partido) =>
    partido.idEquipoLocal === Number(id) || partido.idEquipoVisitante === Number(id)
  ), [partidos, id]);

  if (error) return <main className="profile-page"><div className="profile-message">{error}</div></main>;
  if (!equipo) return <main className="profile-page"><div className="profile-message">Cargando perfil...</div></main>;

  return <main className="profile-page">
    <button type="button" className="profile-back-link profile-back-button" onClick={() => navigate(-1)}>← Volver</button>
    <section className="club-cover">
      <img src={equipo.fotoPlantel ? assetUrl(equipo.fotoPlantel) : plantelGenerico} alt={equipo.fotoPlantel ? `Plantel de ${equipo.nombre}` : `Fondo genérico del plantel de ${equipo.nombre}`} onError={(event) => { event.currentTarget.onerror = null; event.currentTarget.src = plantelGenerico; }} />
    </section>
    <section className="profile-identity">
      <button type="button" className="profile-avatar club-avatar image-preview-trigger" onClick={() => setPreviewImage({ src: equipo.escudo ? assetUrl(equipo.escudo) : equipoGenerico, alt: `Escudo de ${equipo.nombre}` })} aria-label={`Ampliar escudo de ${equipo.nombre}`}><EntityImage type="team" value={equipo.escudo} alt={`Escudo de ${equipo.nombre}`} /></button>
      <div><CopyableCode className="profile-code" value={equipo.codigoPublico} /><h1>{equipo.nombre} {equipo.verificado && <span className="verified-badge" title="Equipo administrado por un director aprobado">✓ Verificado</span>} {titulos.length > 0 && <span className="club-stars" title={`${titulos.length} título${titulos.length === 1 ? "" : "s"}`}>{"★".repeat(titulos.length)}</span>}</h1><p>{equipo.barrio || "Barrio sin informar"}</p></div>
      <div className="profile-owner-actions">
        <FavoriteButton type="equipos" id={equipo.idEquipo} />
        {hasManageAccess && <><span className="profile-owner-badge">Administrás este club</span><button onClick={() => setShowEdit(true)}>Editar perfil</button><button onClick={() => setShowRoster(true)}>Gestionar plantel</button></>}
        {!hasManageAccess && !equipo.verificado && tieneRol(usuario, "DIRECTOR_EQUIPO") && <button className="profile-claim-button" onClick={claimTeam}>Reclamar equipo</button>}
      </div>
    </section>
    {claimMessage && <p className="profile-action-message">{claimMessage}</p>}
    <div className="profile-layout">
      <section className="profile-panel"><h2>Sobre el club</h2><p>{equipo.descripcion || "El club todavía no agregó una descripción."}</p><dl><div><dt>Responsable</dt><dd>{equipo.responsable}</dd></div><div><dt>Contacto</dt><dd>{equipo.telefonoContacto}</dd></div>{equipo.emailContacto && <div><dt>Email</dt><dd>{equipo.emailContacto}</dd></div>}</dl></section>
      <section className="profile-panel"><h2>Historial de torneos</h2><div className="enrollment-list">{inscripciones.map((item) => <Link className="history-link" to={`/torneo/${item.idTorneo}`} key={item.idInscripcion}><strong>{item.nombreTorneo}</strong><small>{item.estado} · {item.cantidadJugadores} jugadores</small><span>Ver torneo →</span></Link>)}</div></section>
    </div>
    {titulos.length > 0 && <section className="profile-panel"><h2>Títulos</h2><div className="club-title-list">{titulos.map((torneo) => <Link to={`/torneo/${torneo.idTorneo}`} key={torneo.idTorneo}><span>🏆</span><div><strong>{torneo.nombre}</strong><small>{torneo.formato} · {torneo.ubicacion}</small></div><b>Ver torneo →</b></Link>)}</div></section>}
    <section className="profile-panel"><div className="profile-section-head"><div><span>Plantel</span><h2>{jugadores.length} jugadores</h2></div></div><div className="player-mini-grid">{jugadores.map((jugador) => { const nombre = jugador.nombreJugador || "Jugador"; return <Link to={`/jugador/${jugador.idJugador}`} className="player-mini" key={jugador.idJugador} title={`Ver perfil de ${nombre}`}><div><EntityImage type="player" value={jugador.foto} alt="" /></div><strong>{nombre}</strong><span>{jugador.posicion || "Sin posición"}</span></Link>; })}</div></section>
    <section className="profile-panel"><h2>Partidos</h2><div className="profile-match-list">{historial.length === 0 ? <p>Todavía no hay partidos.</p> : historial.slice().reverse().map((partido) => <Link to={`/partido/${partido.idPartido}`} key={partido.idPartido}><span className="profile-match-team"><EntityImage type="team" value={partido.escudoEquipoLocal} alt="" /><span>{partido.nombreEquipoLocal}</span></span><strong>{partido.estado === "JUGADO" || partido.estado === "FINALIZADO" ? `${partido.golesLocal} - ${partido.golesVisitante}` : "vs"}</strong><span className="profile-match-team profile-match-team-away"><span>{partido.nombreEquipoVisitante}</span><EntityImage type="team" value={partido.escudoEquipoVisitante} alt="" /></span></Link>)}</div></section>
    {showEdit && <TeamFormModal torneoId={equipo.idTorneo} equipo={equipo} onClose={() => setShowEdit(false)} onSaved={setEquipo} />}
    {showRoster && <TeamRosterModal equipo={equipo} torneoId={equipo.idTorneo} canManage={hasManageAccess} onClose={() => setShowRoster(false)} />}
    {previewImage && <ImageLightbox {...previewImage} onClose={() => setPreviewImage(null)} />}
  </main>;
}

export default EquipoPerfil;
