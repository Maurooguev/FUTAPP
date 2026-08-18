import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { API_URL } from "../config/api";
import "./Profiles.css";

async function fetchJson(url, message) {
  const response = await fetch(url);
  if (!response.ok) throw new Error(message);
  return response.json();
}

function EquipoPerfil() {
  const { id } = useParams();
  const [equipo, setEquipo] = useState(null);
  const [jugadores, setJugadores] = useState([]);
  const [partidos, setPartidos] = useState([]);
  const [inscripciones, setInscripciones] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    fetchJson(`${API_URL}/equipos/${id}`, "No se encontró el equipo")
      .then(async (team) => {
        setEquipo(team);
        return Promise.all([
          fetchJson(`${API_URL}/jugadores/equipo/${id}`, "No se pudo cargar el plantel"),
          fetchJson(`${API_URL}/partidos/torneo/${team.idTorneo}`, "No se pudieron cargar los partidos"),
          fetchJson(`${API_URL}/inscripciones/equipos/${id}`, "No se pudo cargar el historial de torneos"),
        ]);
      })
      .then(([players, matches, enrollments]) => {
        setJugadores(Array.isArray(players) ? players : []);
        setPartidos(Array.isArray(matches) ? matches : []);
        setInscripciones(Array.isArray(enrollments) ? enrollments : []);
      })
      .catch((requestError) => setError(`${requestError.message}. Actualizá la página para reintentar.`));
  }, [id]);

  const historial = useMemo(() => partidos.filter((partido) =>
    partido.idEquipoLocal === Number(id) || partido.idEquipoVisitante === Number(id)
  ), [partidos, id]);

  if (error) return <main className="profile-page"><div className="profile-message">{error}</div></main>;
  if (!equipo) return <main className="profile-page"><div className="profile-message">Cargando perfil...</div></main>;

  return <main className="profile-page">
    <section className="club-cover">
      {equipo.fotoPlantel ? <img src={equipo.fotoPlantel} alt={`Plantel de ${equipo.nombre}`} /> : <div className="club-cover-placeholder">Foto del plantel</div>}
    </section>
    <section className="profile-identity">
      <div className="profile-avatar club-avatar">{equipo.escudo ? <img src={equipo.escudo} alt={`Escudo de ${equipo.nombre}`} /> : equipo.nombre.slice(0, 2).toUpperCase()}</div>
      <div><span className="profile-code">{equipo.codigoPublico}</span><h1>{equipo.nombre}</h1><p>{equipo.barrio || "Barrio sin informar"}</p></div>
    </section>
    <div className="profile-layout">
      <section className="profile-panel"><h2>Sobre el club</h2><p>{equipo.descripcion || "El club todavía no agregó una descripción."}</p><dl><div><dt>Responsable</dt><dd>{equipo.responsable}</dd></div><div><dt>Contacto</dt><dd>{equipo.telefonoContacto}</dd></div>{equipo.emailContacto && <div><dt>Email</dt><dd>{equipo.emailContacto}</dd></div>}</dl></section>
      <section className="profile-panel"><h2>Historial de torneos</h2><div className="enrollment-list">{inscripciones.map((item) => <Link className="history-link" to={`/torneo/${item.idTorneo}`} key={item.idInscripcion}><strong>{item.nombreTorneo}</strong><small>{item.estado} · {item.cantidadJugadores} jugadores</small><span>Ver torneo →</span></Link>)}</div></section>
    </div>
    <section className="profile-panel"><div className="profile-section-head"><div><span>Plantel</span><h2>{jugadores.length} jugadores</h2></div></div><div className="player-mini-grid">{jugadores.map((jugador) => <Link to={`/jugador/${jugador.idJugador}`} className="player-mini" key={jugador.idJugador}><div>{jugador.foto ? <img src={jugador.foto} alt="" /> : jugador.numeroCamiseta}</div><strong>{jugador.apodo || jugador.nombre}</strong><span>{jugador.posicion}</span></Link>)}</div></section>
    <section className="profile-panel"><h2>Partidos</h2><div className="profile-match-list">{historial.length === 0 ? <p>Todavía no hay partidos.</p> : historial.slice().reverse().map((partido) => <Link to={`/partido/${partido.idPartido}`} key={partido.idPartido}><span>{partido.nombreEquipoLocal}</span><strong>{partido.estado === "JUGADO" || partido.estado === "FINALIZADO" ? `${partido.golesLocal} - ${partido.golesVisitante}` : "vs"}</strong><span>{partido.nombreEquipoVisitante}</span></Link>)}</div></section>
  </main>;
}

export default EquipoPerfil;
