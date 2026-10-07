import { useEffect, useMemo, useState } from "react";
import { API_URL, apiFetch } from "../../config/api";
import "./TournamentReadiness.css";

function TournamentReadiness({ torneoId, torneo, equipos, fechas = [], partidos, onGenerateFixture, onNextRound, onNavigate }) {
  const [rosters, setRosters] = useState({});

  useEffect(() => {
    let active = true;
    if (equipos.length === 0) return () => { active = false; };
    Promise.all(equipos.map(async (equipo) => {
      const response = await apiFetch(`${API_URL}/inscripciones/equipos/${equipo.idEquipo}`);
      if (!response.ok) return [equipo.idEquipo, null];
      const history = await response.json();
      const enrollment = history.find((item) => Number(item.idTorneo) === Number(torneoId) && item.estado === "ACTIVA");
      return [equipo.idEquipo, enrollment?.cantidadJugadores ?? 0];
    })).then((entries) => { if (active) setRosters(Object.fromEntries(entries)); });
    return () => { active = false; };
  }, [equipos, torneoId]);

  const loading = equipos.some((equipo) => !(equipo.idEquipo in rosters));
  const shortTeams = useMemo(() => equipos.filter((equipo) => (rosters[equipo.idEquipo] ?? 0) < 5), [equipos, rosters]);
  const requiredTeams = torneo?.cantidadEquipos ?? 2;
  const enoughTeams = equipos.length === requiredTeams;
  const rostersReady = !loading && enoughTeams && shortTeams.length === 0;
  const fixtureReady = fechas.some((fecha) => Number(fecha.numeroFecha) === 1);
  const ready = enoughTeams && rostersReady && fixtureReady;
  const pendientes = partidos.filter((partido) => !["FINALIZADO", "CANCELADO"].includes(partido.estado?.toUpperCase())).length;
  const usaLlave = torneo?.formato?.toLowerCase() === "eliminación directa" || torneo?.formato?.toLowerCase().includes("playoffs");
  const campeonDefinido = partidos.some((partido) => partido.observaciones === "Final" && partido.estado?.toUpperCase() === "FINALIZADO");
  const navigateTo = (id) => onNavigate ? onNavigate(id) : document.getElementById(id)?.scrollIntoView({ behavior: "smooth", block: "start" });

  return <section className={`tournament-readiness ${ready ? "ready" : ""}`}>
    <div className="readiness-heading"><div><span>Preparación del torneo</span><h2>{ready ? "Todo listo para jugar" : "Próximos pasos"}</h2></div><strong>{[enoughTeams, rostersReady, fixtureReady].filter(Boolean).length}/3 completos</strong></div>
    <div className="readiness-steps">
      <button className={enoughTeams ? "done" : "pending"} onClick={() => navigateTo("equipos")}><i>{enoughTeams ? "✓" : "1"}</i><span><b>Equipos</b><small>{equipos.length}/{requiredTeams} inscriptos</small></span></button>
      <button className={rostersReady ? "done" : "pending"} onClick={() => navigateTo("equipos")}><i>{rostersReady ? "✓" : "2"}</i><span><b>Planteles</b><small>{loading ? "Revisando…" : shortTeams.length ? `${shortTeams.length} incompletos` : "Mínimo 5 por equipo"}</small></span></button>
      <button className={fixtureReady ? "done" : "pending"} onClick={fixtureReady ? () => navigateTo("fixture") : onGenerateFixture}><i>{fixtureReady ? "✓" : "3"}</i><span><b>Fecha 1</b><small>{fixtureReady ? `${partidos.length} partidos cargados` : "Crear primera fecha"}</small></span></button>
    </div>
    {!loading && shortTeams.length > 0 && <p className="readiness-warning"><strong>Necesitan al menos 5 jugadores:</strong> {shortTeams.map((team) => `${team.nombre} (${rosters[team.idEquipo] ?? 0}/5)`).join(", ")}.</p>}
    <button className="readiness-primary" disabled={campeonDefinido} onClick={!fixtureReady ? onGenerateFixture : pendientes > 0 ? () => navigateTo("fixture") : usaLlave ? onNextRound : () => navigateTo("fixture")}>{!fixtureReady ? "Generar fixture" : pendientes > 0 ? `Cargar resultados pendientes (${pendientes})` : campeonDefinido ? "Torneo con campeón definido" : usaLlave ? "Generar próxima fase" : "Revisar torneo completo"}</button>
  </section>;
}

export default TournamentReadiness;
