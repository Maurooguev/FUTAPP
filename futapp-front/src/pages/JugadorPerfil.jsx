import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { API_URL } from "../config/api";
import "./Profiles.css";

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
  const [jugador, setJugador] = useState(null);
  const [equipo, setEquipo] = useState(null);
  const [partidos, setPartidos] = useState([]);
  const [goles, setGoles] = useState([]);
  const [tarjetas, setTarjetas] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    fetch(`${API_URL}/jugadores/${id}`)
      .then(async (response) => {
        if (!response.ok) throw new Error("No se encontró el jugador");
        const player = await response.json();
        setJugador(player);
        const team = await fetch(`${API_URL}/equipos/${player.idEquipo}`).then((res) => res.json());
        setEquipo(team);
        return Promise.all([
          fetch(`${API_URL}/partidos/torneo/${team.idTorneo}`).then((res) => res.json()),
          fetch(`${API_URL}/goles/jugador/${id}`).then((res) => res.json()),
          fetch(`${API_URL}/tarjetas/jugador/${id}`).then((res) => res.json()),
        ]);
      })
      .then(([matches, goals, cards]) => { setPartidos(matches); setGoles(goals); setTarjetas(cards); })
      .catch((requestError) => setError(requestError.message));
  }, [id]);

  const historial = useMemo(() => equipo ? partidos.filter((partido) =>
    partido.idEquipoLocal === equipo.idEquipo || partido.idEquipoVisitante === equipo.idEquipo
  ) : [], [partidos, equipo]);
  const amarillas = tarjetas.filter((tarjeta) => tarjeta.tipoTarjeta === "AMARILLA").length;
  const rojas = tarjetas.filter((tarjeta) => tarjeta.tipoTarjeta === "ROJA").length;

  if (error) return <main className="profile-page"><div className="profile-message">{error}</div></main>;
  if (!jugador || !equipo) return <main className="profile-page"><div className="profile-message">Cargando perfil...</div></main>;

  return <main className="profile-page player-profile">
    <section className="player-hero">
      <div className="profile-avatar player-avatar">{jugador.foto ? <img src={jugador.foto} alt={`${jugador.nombre} ${jugador.apellido}`} /> : jugador.numeroCamiseta}</div>
      <div><span className="profile-code">{jugador.codigoPublico}</span><h1>{jugador.nombre} {jugador.apellido}</h1>{jugador.apodo && <p className="player-nickname">“{jugador.apodo}”</p>}<Link to={`/equipo/${equipo.idEquipo}`}>{equipo.nombre}</Link></div>
      <strong className="shirt-number">#{jugador.numeroCamiseta}</strong>
    </section>
    <section className="player-facts"><div><span>Posición</span><strong>{jugador.posicion}</strong></div><div><span>Edad</span><strong>{calcularEdad(jugador.fechaNacimiento) ?? "—"}</strong></div><div><span>Nacimiento</span><strong>{jugador.fechaNacimiento || "—"}</strong></div><div><span>Pierna hábil</span><strong>{jugador.piernaHabil || "—"}</strong></div></section>
    <section className="profile-panel"><h2>Estadísticas</h2><div className="player-stats"><div><strong>{historial.filter((p) => p.estado === "JUGADO" || p.estado === "FINALIZADO").length}</strong><span>Partidos del plantel</span></div><div><strong>{goles.length}</strong><span>Goles</span></div><div><strong>{amarillas}</strong><span>Amarillas</span></div><div><strong>{rojas}</strong><span>Rojas</span></div></div></section>
    <section className="profile-panel"><h2>Historial de partidos</h2><p className="profile-note">Primera versión: se muestran los partidos del plantel. Más adelante registraremos convocatorias y minutos individuales.</p><div className="profile-match-list">{historial.length === 0 ? <p>Todavía no hay partidos.</p> : historial.slice().reverse().map((partido) => <Link to={`/partido/${partido.idPartido}`} key={partido.idPartido}><span>{partido.nombreEquipoLocal}</span><strong>{partido.estado === "JUGADO" || partido.estado === "FINALIZADO" ? `${partido.golesLocal} - ${partido.golesVisitante}` : "vs"}</strong><span>{partido.nombreEquipoVisitante}</span></Link>)}</div></section>
  </main>;
}

export default JugadorPerfil;
