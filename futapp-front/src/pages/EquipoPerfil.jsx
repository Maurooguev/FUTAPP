import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { API_URL, apiFetch, assetUrl } from "../config/api";
import useAuth from "../context/useAuth";
import { tieneRol } from "../utils/roles";
import "./Profiles.css";
import TeamFormModal from "../components/TeamManagement/TeamFormModal";
import TeamRosterModal from "../components/TeamManagement/TeamRosterModal";
import FavoriteButton from "../components/FavoriteButton/FavoriteButton";

async function fetchJson(url, message) {
  const response = await fetch(url);
  if (!response.ok) throw new Error(message);
  return response.json();
}

function EquipoPerfil() {
  const { id } = useParams();
  const { usuario } = useAuth();
  const [equipo, setEquipo] = useState(null);
  const [jugadores, setJugadores] = useState([]);
  const [partidos, setPartidos] = useState([]);
  const [inscripciones, setInscripciones] = useState([]);
  const [error, setError] = useState("");
  const [claimMessage, setClaimMessage] = useState("");
  const [canManage, setCanManage] = useState(false);
  const [showEdit, setShowEdit] = useState(false);
  const [showRoster, setShowRoster] = useState(false);

  const claimTeam = async () => {
    const response = await apiFetch(`${API_URL}/reclamos/equipos/${id}`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ idTorneo: equipo.idTorneo, mensaje: "Solicito administrar este equipo" }) });
    const payload = await response.json().catch(() => null);
    setClaimMessage(response.ok ? "Solicitud enviada al organizador." : payload?.message || "No se pudo enviar la solicitud");
  };

  useEffect(() => {
    fetchJson(`${API_URL}/equipos/${id}`, "No se encontró el equipo")
      .then(async (team) => {
        setEquipo(team);
        const [matches, enrollments] = await Promise.all([
          fetchJson(`${API_URL}/partidos/torneo/${team.idTorneo}`, "No se pudieron cargar los partidos"),
          fetchJson(`${API_URL}/inscripciones/equipos/${id}`, "No se pudo cargar el historial de torneos"),
        ]);
        const currentEnrollment = enrollments.find((item) => item.idTorneo === team.idTorneo)
          ?? enrollments.find((item) => item.estado === "ACTIVA")
          ?? enrollments[0];
        const roster = currentEnrollment
          ? await fetchJson(`${API_URL}/inscripciones/${currentEnrollment.idInscripcion}/jugadores`, "No se pudo cargar el plantel")
          : [];
        return [roster.filter((player) => player.estado === "ACTIVA"), matches, enrollments];
      })
      .then(([players, matches, enrollments]) => {
        setJugadores(Array.isArray(players) ? players : []);
        setPartidos(Array.isArray(matches) ? matches : []);
        setInscripciones(Array.isArray(enrollments) ? enrollments : []);
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
    <section className="club-cover">
      {equipo.fotoPlantel ? <img src={assetUrl(equipo.fotoPlantel)} alt={`Plantel de ${equipo.nombre}`} /> : <div className="club-cover-placeholder">Foto del plantel</div>}
    </section>
    <section className="profile-identity">
      <div className="profile-avatar club-avatar">{equipo.escudo ? <img src={assetUrl(equipo.escudo)} alt={`Escudo de ${equipo.nombre}`} /> : equipo.nombre.slice(0, 2).toUpperCase()}</div>
      <div><span className="profile-code">{equipo.codigoPublico}</span><h1>{equipo.nombre}</h1><p>{equipo.barrio || "Barrio sin informar"}</p></div>
      <div className="profile-owner-actions">
        <FavoriteButton type="equipos" id={equipo.idEquipo} />
        {hasManageAccess && <><span className="profile-owner-badge">Administrás este club</span><button onClick={() => setShowEdit(true)}>Editar perfil</button><button onClick={() => setShowRoster(true)}>Gestionar plantel</button></>}
        {!hasManageAccess && tieneRol(usuario, "DIRECTOR_EQUIPO") && <button className="profile-claim-button" onClick={claimTeam}>Reclamar equipo</button>}
      </div>
    </section>
    {claimMessage && <p className="profile-action-message">{claimMessage}</p>}
    <div className="profile-layout">
      <section className="profile-panel"><h2>Sobre el club</h2><p>{equipo.descripcion || "El club todavía no agregó una descripción."}</p><dl><div><dt>Responsable</dt><dd>{equipo.responsable}</dd></div><div><dt>Contacto</dt><dd>{equipo.telefonoContacto}</dd></div>{equipo.emailContacto && <div><dt>Email</dt><dd>{equipo.emailContacto}</dd></div>}</dl></section>
      <section className="profile-panel"><h2>Historial de torneos</h2><div className="enrollment-list">{inscripciones.map((item) => <Link className="history-link" to={`/torneo/${item.idTorneo}`} key={item.idInscripcion}><strong>{item.nombreTorneo}</strong><small>{item.estado} · {item.cantidadJugadores} jugadores</small><span>Ver torneo →</span></Link>)}</div></section>
    </div>
    <section className="profile-panel"><div className="profile-section-head"><div><span>Plantel</span><h2>{jugadores.length} jugadores</h2></div></div><div className="player-mini-grid">{jugadores.map((jugador) => <Link to={`/jugador/${jugador.idJugador}`} className="player-mini" key={jugador.idJugador}><div>{jugador.foto ? <img src={assetUrl(jugador.foto)} alt="" /> : jugador.numeroCamiseta}</div><strong>{jugador.apodo || jugador.nombre}</strong><span>{jugador.posicion}</span></Link>)}</div></section>
    <section className="profile-panel"><h2>Partidos</h2><div className="profile-match-list">{historial.length === 0 ? <p>Todavía no hay partidos.</p> : historial.slice().reverse().map((partido) => <Link to={`/partido/${partido.idPartido}`} key={partido.idPartido}><span>{partido.nombreEquipoLocal}</span><strong>{partido.estado === "JUGADO" || partido.estado === "FINALIZADO" ? `${partido.golesLocal} - ${partido.golesVisitante}` : "vs"}</strong><span>{partido.nombreEquipoVisitante}</span></Link>)}</div></section>
    {showEdit && <TeamFormModal torneoId={equipo.idTorneo} equipo={equipo} onClose={() => setShowEdit(false)} onSaved={setEquipo} />}
    {showRoster && <TeamRosterModal equipo={equipo} torneoId={equipo.idTorneo} canManage={hasManageAccess} onClose={() => setShowRoster(false)} />}
  </main>;
}

export default EquipoPerfil;
