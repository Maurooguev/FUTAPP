import { useEffect, useMemo, useState } from "react";
import { API_URL } from "../../config/api";
import "./TournamentReadiness.css";

function TournamentReadiness({ torneoId, torneo, equipos, partidos, onGenerateFixture, onNextRound }) {
  const [rosters, setRosters] = useState({});

  useEffect(() => {
    let active = true;
    if (equipos.length === 0) return () => { active = false; };
    Promise.all(equipos.map(async (equipo) => {
      const response = await fetch(`${API_URL}/inscripciones/equipos/${equipo.idEquipo}`);
      if (!response.ok) return [equipo.idEquipo, null];
      const history = await response.json();
      const enrollment = history.find((item) => Number(item.idTorneo) === Number(torneoId) && item.estado === "ACTIVA");
      return [equipo.idEquipo, enrollment?.cantidadJugadores ?? 0];
    })).then((entries) => { if (active) setRosters(Object.fromEntries(entries)); });
    return () => { active = false; };
  }, [equipos, torneoId]);

  const loading = equipos.some((equipo) => !(equipo.idEquipo in rosters));
  const emptyTeams = useMemo(() => equipos.filter((equipo) => rosters[equipo.idEquipo] == null || rosters[equipo.idEquipo] === 0), [equipos, rosters]);
  const shortTeams = useMemo(() => equipos.filter((equipo) => rosters[equipo.idEquipo] > 0 && rosters[equipo.idEquipo] < 5), [equipos, rosters]);
  const enoughTeams = equipos.length >= 2;
  const rostersReady = !loading && equipos.length >= 2 && emptyTeams.length === 0;
  const fixtureReady = partidos.length > 0;
  const ready = enoughTeams && rostersReady && fixtureReady;
  const pendientes = partidos.filter((partido) => !["FINALIZADO", "CANCELADO"].includes(partido.estado?.toUpperCase())).length;
  const usaLlave = torneo?.formato?.toLowerCase() === "eliminación directa" || torneo?.formato?.toLowerCase().includes("playoffs");
  const campeonDefinido = partidos.some((partido) => partido.observaciones === "Final" && partido.estado?.toUpperCase() === "FINALIZADO");
  const scrollTo = (id) => document.getElementById(id)?.scrollIntoView({ behavior: "smooth", block: "start" });

  return <section className={`tournament-readiness ${ready ? "ready" : ""}`}>
    <div className="readiness-heading"><div><span>Preparación del torneo</span><h2>{ready ? "Todo listo para jugar" : "Próximos pasos"}</h2></div><strong>{[enoughTeams, rostersReady, fixtureReady].filter(Boolean).length}/3 completos</strong></div>
    <div className="readiness-steps">
      <button className={enoughTeams ? "done" : "pending"} onClick={() => scrollTo("equipos")}><i>{enoughTeams ? "✓" : "1"}</i><span><b>Equipos</b><small>{equipos.length} inscriptos</small></span></button>
      <button className={rostersReady ? "done" : "pending"} onClick={() => scrollTo("equipos")}><i>{rostersReady ? "✓" : "2"}</i><span><b>Planteles</b><small>{loading ? "Revisando…" : emptyTeams.length ? `${emptyTeams.length} sin jugadores` : "Todos iniciados"}</small></span></button>
      <button className={fixtureReady ? "done" : "pending"} onClick={fixtureReady ? () => scrollTo("fixture") : onGenerateFixture}><i>{fixtureReady ? "✓" : "3"}</i><span><b>Fixture</b><small>{fixtureReady ? `${partidos.length} partidos` : "Generar calendario"}</small></span></button>
    </div>
    {!loading && emptyTeams.length > 0 && <p className="readiness-warning"><strong>Sin jugadores:</strong> {emptyTeams.map((team) => team.nombre).join(", ")}.</p>}
    {!loading && emptyTeams.length === 0 && shortTeams.length > 0 && <p className="readiness-note">Planteles con menos de 5 jugadores: {shortTeams.map((team) => team.nombre).join(", ")}. Podés continuar, pero conviene completarlos.</p>}
    <button className="readiness-primary" disabled={campeonDefinido} onClick={!fixtureReady ? onGenerateFixture : pendientes > 0 ? () => scrollTo("fixture") : usaLlave ? onNextRound : () => scrollTo("fixture")}>{!fixtureReady ? "Generar fixture" : pendientes > 0 ? `Cargar resultados pendientes (${pendientes})` : campeonDefinido ? "Torneo con campeón definido" : usaLlave ? "Generar próxima fase" : "Revisar torneo completo"}</button>
  </section>;
}

export default TournamentReadiness;
