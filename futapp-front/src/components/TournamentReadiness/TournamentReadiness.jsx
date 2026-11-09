import { useEffect, useMemo, useState } from "react";
import { API_URL, apiFetch } from "../../config/api";
import "./TournamentReadiness.css";

function TournamentReadiness({ torneoId, torneo, equipos, fechas = [], partidos, refreshKey = 0, onGenerateFixture, onNextRound, onPublish, onStart, onNavigate }) {
  const [rosters, setRosters] = useState({});
  const [copied, setCopied] = useState(false);

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
  }, [equipos, torneoId, refreshKey]);

  const loading = equipos.some((equipo) => !(equipo.idEquipo in rosters));
  const shortTeams = useMemo(() => equipos.filter((equipo) => (rosters[equipo.idEquipo] ?? 0) < 5), [equipos, rosters]);
  const requiredTeams = torneo?.cantidadEquipos ?? 2;
  const state = torneo?.estado?.toUpperCase();
  const dataReady = Boolean(torneo?.nombre && torneo?.ubicacion && torneo?.formato);
  const enoughTeams = equipos.length === requiredTeams;
  const rostersReady = !loading && enoughTeams && shortTeams.length === 0;
  const fixtureReady = fechas.some((fecha) => Number(fecha.numeroFecha) === 1);
  const published = state !== "BORRADOR";
  const ready = dataReady && enoughTeams && rostersReady && fixtureReady;
  const pendientes = partidos.filter((partido) => !["FINALIZADO", "CANCELADO"].includes(partido.estado?.toUpperCase())).length;
  const usaLlave = torneo?.formato?.toLowerCase() === "eliminación directa" || torneo?.formato?.toLowerCase().includes("playoffs");
  const campeonDefinido = partidos.some((partido) => partido.observaciones === "Final" && partido.estado?.toUpperCase() === "FINALIZADO");
  const navigateTo = (id) => onNavigate ? onNavigate(id) : document.getElementById(id)?.scrollIntoView({ behavior: "smooth", block: "start" });
  const primary = !dataReady
    ? { label: "Completar datos", action: () => navigateTo("info") }
    : !enoughTeams
      ? { label: equipos.length < requiredTeams ? `Agregar equipos (${equipos.length}/${requiredTeams})` : `Ajustar equipos (${equipos.length}/${requiredTeams})`, action: () => navigateTo("equipos") }
      : loading
        ? { label: "Revisando planteles…", action: () => {}, disabled: true }
      : !rostersReady
        ? { label: "Completar planteles", action: () => navigateTo("equipos") }
        : !fixtureReady
          ? { label: "Generar fixture", action: onGenerateFixture }
          : state === "BORRADOR"
            ? { label: "Publicar torneo", action: onPublish }
            : state === "PUBLICADO"
              ? { label: "Iniciar torneo", action: onStart }
              : pendientes > 0
                ? { label: `Cargar resultados pendientes (${pendientes})`, action: () => navigateTo("fixture") }
                : campeonDefinido
                  ? { label: "Torneo con campeón definido", action: () => navigateTo("resumen"), disabled: true }
                  : usaLlave
                    ? { label: "Generar próxima fase", action: onNextRound }
                    : { label: "Revisar torneo completo", action: () => navigateTo("fixture") };
  const copyPublicLink = async () => {
    await navigator.clipboard.writeText(`${window.location.origin}/torneo/${torneoId}`);
    setCopied(true); window.setTimeout(() => setCopied(false), 1800);
  };

  const setupCompleted = [enoughTeams, rostersReady, fixtureReady, published].filter(Boolean).length;

  return <section className={`tournament-readiness ${ready ? "ready" : ""}`}>
    <div className="readiness-heading"><div><span>Preparación del torneo</span><h2>{ready ? (state === "BORRADOR" ? "Listo para publicar" : "Todo listo para jugar") : "Completá la preparación"}</h2></div><strong>{setupCompleted}/4 completos</strong></div>
    <div className="readiness-steps">
      <button className={enoughTeams ? "done" : "pending"} onClick={() => navigateTo("equipos")}><i>{enoughTeams ? "✓" : "1"}</i><span><b>Equipos</b><small>{equipos.length}/{requiredTeams} inscriptos</small></span></button>
      <button className={rostersReady ? "done" : "pending"} onClick={() => navigateTo("equipos")}><i>{rostersReady ? "✓" : "2"}</i><span><b>Planteles</b><small>{loading ? "Revisando…" : shortTeams.length ? `${shortTeams.length} incompletos` : "Mínimo 5 por equipo"}</small></span></button>
      <button className={fixtureReady ? "done" : "pending"} onClick={rostersReady ? (fixtureReady ? () => navigateTo("fixture") : onGenerateFixture) : () => navigateTo("equipos")}><i>{fixtureReady ? "✓" : "3"}</i><span><b>Fixture</b><small>{fixtureReady ? `${partidos.length} partidos cargados` : rostersReady ? "Listo para generar" : "Primero completá planteles"}</small></span></button>
      <button type="button" className={published ? "done" : "pending"} disabled={!published && !ready} onClick={ready && state === "BORRADOR" ? onPublish : undefined}><i>{published ? "✓" : "4"}</i><span><b>Publicar</b><small>{published ? "Torneo publicado" : ready ? "Listo para publicar" : "Al completar lo anterior"}</small></span></button>
    </div>
    {!dataReady && <p className="readiness-warning"><strong>Faltan datos básicos:</strong> revisá el nombre, la ubicación y el formato del torneo.</p>}
    {equipos.length > requiredTeams && <p className="readiness-warning"><strong>Hay equipos de más:</strong> configuraste {requiredTeams} cupos y actualmente hay {equipos.length}. Quitá {equipos.length - requiredTeams} antes de continuar.</p>}
    {!loading && shortTeams.length > 0 && <p className="readiness-warning"><strong>Necesitan al menos 5 jugadores:</strong> {shortTeams.map((team) => `${team.nombre} (${rosters[team.idEquipo] ?? 0}/5)`).join(", ")}.</p>}
    {ready && state !== "BORRADOR" && <div className="readiness-share"><div><strong>Enlace público listo</strong><small>Compartilo con equipos, jugadores y seguidores.</small></div><button type="button" onClick={copyPublicLink}>{copied ? "Copiado ✓" : "Copiar enlace"}</button></div>}
    <button className={`readiness-primary ${ready && ["BORRADOR", "PUBLICADO"].includes(state) ? "lifecycle" : ""}`} disabled={primary.disabled} onClick={primary.action}>{primary.label}</button>
  </section>;
}

export default TournamentReadiness;
