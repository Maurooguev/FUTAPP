import { useEffect, useMemo, useRef, useState } from "react";
import { Link, useParams, useSearchParams } from "react-router-dom";

import useAuth from "../context/useAuth";
import { puedeAdministrarTorneo } from "../utils/roles";
import { API_URL, apiFetch } from "../config/api";

import TournamentHero from "../components/TournamentHero/TournamentHero";
import StatsCard from "../components/StatsCard/StatsCard";
import StandingsTable from "../components/StandingsTable/StandingsTable";
import TopScorersTable from "../components/TopScorersTable/TopScorersTable";
import TopCardsTable from "../components/TopCardsTable/TopCardsTable";
import MatchModal from "../components/MatchModal/MatchModal";
import FixtureByDate from "../components/FixtureByDate/FixtureByDate";
import ResultModal from "../components/ResultModal/ResultModal";
import TournamentFormModal from "../components/TournamentFormModal/TournamentFormModal";
import MatchFormModal from "../components/MatchFormModal/MatchFormModal";
import TeamManagement from "../components/TeamManagement/TeamManagement";
import DateFormModal from "../components/DateFormModal/DateFormModal";
import FixtureGeneratorModal from "../components/FixtureGeneratorModal/FixtureGeneratorModal";
import TournamentReadiness from "../components/TournamentReadiness/TournamentReadiness";
import TournamentExports from "../components/TournamentExports/TournamentExports";
import KnockoutBracket from "../components/KnockoutBracket/KnockoutBracket";
import EntityImage from "../components/EntityImage/EntityImage";

const TOURNAMENT_TABS = new Set(["resumen", "info", "tabla", "equipos", "stats", "fixture"]);

const formatMatchDate = (value) => value ? new Intl.DateTimeFormat("es-AR", { day: "2-digit", month: "short", hour: "2-digit", minute: "2-digit" }).format(new Date(value)) : "Horario a confirmar";

function TorneoDetalle() {
  const { id } = useParams();
  const [searchParams, setSearchParams] = useSearchParams();
  const { usuario } = useAuth();

  const [torneo, setTorneo] = useState(null);
  const puedeEditar = puedeAdministrarTorneo(usuario, torneo);
  const [tabla, setTabla] = useState([]);
  const [goleadores, setGoleadores] = useState([]);
  const [fechas, setFechas] = useState([]);
  const [partidos, setPartidos] = useState([]);
  const [tarjetas, setTarjetas] = useState([]);
  const [equipos, setEquipos] = useState([]);
  const [estructura, setEstructura] = useState(null);
  const [partidoSeleccionado, setPartidoSeleccionado] = useState(null);
  const [mostrarResultado, setMostrarResultado] = useState(false);
  const [partidoResultado, setPartidoResultado] = useState(null);
  const [mostrarEdicion, setMostrarEdicion] = useState(false);
  const [mostrarPartido, setMostrarPartido] = useState(false);
  const [mostrarFecha, setMostrarFecha] = useState(false);
  const [partidoEnEdicion, setPartidoEnEdicion] = useState(null);
  const [fechaEnEdicion, setFechaEnEdicion] = useState(null);
  const [adminError, setAdminError] = useState("");
  const [generandoFixture, setGenerandoFixture] = useState(false);
  const [mostrarGeneradorFixture, setMostrarGeneradorFixture] = useState(false);
  const [perfilOrganizador, setPerfilOrganizador] = useState(null);
  const [loadError, setLoadError] = useState("");
  const [reloadKey, setReloadKey] = useState(0);
  const [showMoreActions, setShowMoreActions] = useState(false);
  const visitedTournament = useRef(null);
  const [activeTab, setActiveTab] = useState(() => {
    const requested = window.location.hash.replace("#", "");
    return TOURNAMENT_TABS.has(requested) ? requested : "resumen";
  });

  const changeTab = (tab) => {
    setActiveTab(tab);
    window.history.replaceState(null, "", `${window.location.pathname}${window.location.search}#${tab}`);
  };

  useEffect(() => {
    if (searchParams.get("accion") !== "resultado" || partidos.length === 0) return;
    const partido = partidos.find((item) => item.idPartido === Number(searchParams.get("partido")));
    if (!partido) return;
    setPartidoResultado(partido);
    setMostrarResultado(true);
    setSearchParams({}, { replace: true });
  }, [partidos, searchParams, setSearchParams]);

  useEffect(() => {
    if (activeTab === "tabla" && torneo?.formato?.toLowerCase() === "eliminación directa") setActiveTab("resumen");
  }, [activeTab, torneo?.formato]);

  const eliminarRecurso = async (url, message, onSuccess) => {
    if (!window.confirm(message)) return;
    setAdminError("");
    try {
      const response = await apiFetch(url, { method: "DELETE" });
      if (!response.ok) { const payload = await response.json().catch(() => null); throw new Error(payload?.message || "No se pudo eliminar"); }
      onSuccess();
    } catch (requestError) { setAdminError(requestError.message); }
  };

  const cambiarEstadoTorneo = async (accion) => {
    const destino = accion === "iniciar" ? "iniciar el torneo" : accion === "publicar" ? "publicar el torneo" : "finalizar definitivamente el torneo";
    if (!window.confirm(`¿Querés ${destino}?`)) return;
    setAdminError("");
    try {
      const response = await apiFetch(`${API_URL}/torneos/${id}/${accion}`, { method: "PUT" });
      const payload = await response.json().catch(() => null);
      if (!response.ok) throw new Error(payload?.message || `No se pudo ${accion} el torneo`);
      setTorneo(payload);
    } catch (requestError) { setAdminError(requestError.message); }
  };
  
  useEffect(() => {
    let active = true;
    const getJson = async (path) => {
      const response = await apiFetch(`${API_URL}${path}`);
      if (!response.ok) throw new Error(`Error ${response.status}`);
      return response.json();
    };
    Promise.all([
      getJson(`/torneos/${id}`), getJson(`/torneos/${id}/tabla`),
      getJson(`/goles/torneo/${id}/goleadores`), getJson(`/fechas/torneo/${id}`),
      getJson(`/partidos/torneo/${id}`), getJson(`/tarjetas/torneo/${id}`),
      getJson(`/equipos/torneo/${id}`), getJson(`/torneos/${id}/estructura`),
    ]).then(([tournament, standings, scorers, dates, matches, cards, teams, structure]) => {
      if (!active) return;
      setTorneo(tournament); setTabla(standings); setGoleadores(scorers); setFechas(dates);
      setPartidos(matches); setTarjetas(cards); setEquipos(teams); setEstructura(structure);
      setLoadError("");
    }).catch(() => {
      if (active) setLoadError("No pudimos cargar el torneo. Verificá que el backend esté iniciado.");
    });
    return () => { active = false; };
  }, [id, reloadKey]);

  useEffect(() => {
    let active = true;
    const refreshTeams = async () => {
      try {
        const response = await apiFetch(`${API_URL}/equipos/torneo/${id}`, { cache: "no-store" });
        if (!response.ok) return;
        const teams = await response.json();
        if (active) setEquipos(Array.isArray(teams) ? teams : []);
      } catch { /* La carga principal conserva el último estado disponible. */ }
    };
    const onFocus = () => refreshTeams();
    const onVisibility = () => { if (document.visibilityState === "visible") refreshTeams(); };
    const onStorage = (event) => { if (event.key === "tournamentDataUpdatedAt") refreshTeams(); };
    window.addEventListener("focus", onFocus);
    window.addEventListener("requests-updated", onFocus);
    window.addEventListener("storage", onStorage);
    document.addEventListener("visibilitychange", onVisibility);
    return () => {
      active = false;
      window.removeEventListener("focus", onFocus);
      window.removeEventListener("requests-updated", onFocus);
      window.removeEventListener("storage", onStorage);
      document.removeEventListener("visibilitychange", onVisibility);
    };
  }, [id]);

  useEffect(() => {
    if (!torneo?.idTorneo || visitedTournament.current === id) return;
    visitedTournament.current = id;
    fetch(`${API_URL}/torneos/${id}/visitas`, { method: "POST" }).then((response) => response.ok ? response.json() : null)
      .then((data) => { if (data) setTorneo((current) => current ? { ...current, visitas: data.visitas } : current); }).catch(() => null);
  }, [id, torneo?.idTorneo]);

  useEffect(() => {
    if (!torneo?.idPropietario) { setPerfilOrganizador(null); return; }
    apiFetch(`${API_URL}/perfiles-organizador/${torneo.idPropietario}`)
      .then((response) => response.ok ? response.json() : null)
      .then(setPerfilOrganizador)
      .catch(() => setPerfilOrganizador(null));
  }, [torneo?.idPropietario]);

  const fechaActual = useMemo(() => {
    if (fechas.length === 0) return null;

    const fechasOrdenadas = [...fechas].sort(
      (a, b) => a.numeroFecha - b.numeroFecha
    );

    const activa = fechasOrdenadas.find(
      (fecha) => fecha.estado?.toUpperCase() === "ACTIVA"
    );

    return activa || fechasOrdenadas[fechasOrdenadas.length - 1];
  }, [fechas]);

  const topGoleadores = useMemo(() => {
    return [...goleadores].slice(0, 5);
  }, [goleadores]);

  const topAmarillas = useMemo(() => {
    if (tarjetas.length === 0 || partidos.length === 0) return [];

    const idsPartidosTorneo = new Set(partidos.map((p) => p.idPartido));

    const amarillas = tarjetas.filter(
      (tarjeta) =>
        idsPartidosTorneo.has(tarjeta.idPartido) &&
        tarjeta.tipoTarjeta?.toUpperCase() === "AMARILLA"
    );

    const acumulado = {};

    for (const tarjeta of amarillas) {
      const key = tarjeta.idJugador;

      if (!acumulado[key]) {
        acumulado[key] = {
          idJugador: tarjeta.idJugador,
          nombreJugador: tarjeta.nombreJugador,
          idEquipo: tarjeta.idEquipo,
          nombreEquipo: tarjeta.nombreEquipo,
          cantidad: 0,
        };
      }

      acumulado[key].cantidad += 1;
    }

    return Object.values(acumulado)
      .sort(
        (a, b) =>
          b.cantidad - a.cantidad ||
          a.nombreJugador.localeCompare(b.nombreJugador)
      )
      .slice(0, 5);
  }, [tarjetas, partidos]);

  const proximosPartidos = useMemo(() => {
    return partidos
      .filter((partido) => partido.estado?.toUpperCase() === "PROGRAMADO")
      .sort((a, b) => {
        if (a.idFecha !== b.idFecha) return a.idFecha - b.idFecha;
        return (a.fechaHora || "9999-12-31T23:59").localeCompare(b.fechaHora || "9999-12-31T23:59");
      })
      .slice(0, 6);
  }, [partidos]);

  const partidosJugados = useMemo(() => {
    return partidos.filter(
      (partido) => partido.estado?.toUpperCase() === "FINALIZADO"
    ).length;
  }, [partidos]);
  const siguientePartidoPendiente = useMemo(() => [...partidos]
    .filter((partido) => ["PROGRAMADO", "EN_JUEGO"].includes(partido.estado?.toUpperCase()))
    .sort((a, b) => (a.fechaHora || "9999-12-31T23:59").localeCompare(b.fechaHora || "9999-12-31T23:59"))[0] || null, [partidos]);
  const ultimoResultado = useMemo(() => [...partidos]
    .filter((partido) => partido.estado?.toUpperCase() === "FINALIZADO")
    .sort((a, b) => (b.fechaHora || "").localeCompare(a.fechaHora || "") || b.idPartido - a.idPartido)[0] || null, [partidos]);
  const podio = useMemo(() => tabla.slice(0, 3), [tabla]);
  const buscarEquipo = (teamId) => equipos.find((equipo) => equipo.idEquipo === teamId);
  const cantidadEquiposLlave = estructura?.divisiones?.[0]?.fases?.find((fase) => fase.tipo === "ELIMINACION_DIRECTA")?.clasificados || equipos.length;
  const usaLlave = torneo?.formato?.toLowerCase() === "eliminación directa" || torneo?.formato?.toLowerCase().includes("playoffs");
  const filaCampeon = tabla.find((fila) => fila.idEquipo === torneo?.idCampeon) || tabla[0];
  const campeonLiga = torneo?.estado?.toUpperCase() === "FINALIZADO" && !usaLlave && torneo?.idCampeon
    ? { ...filaCampeon, idEquipo: torneo.idCampeon, nombreEquipo: torneo.nombreCampeon }
    : null;

  if (!torneo) {
    if (loadError) return <div className="empty-state" role="alert"><h3>No se pudo cargar el torneo</h3><p>{loadError}</p><button type="button" onClick={() => setReloadKey((value) => value + 1)}>Reintentar</button></div>;
    return <p style={{ padding: "20px" }}>Cargando...</p>;
  }

const recargarDatosTorneo = () => {
  apiFetch(`${API_URL}/torneos/${id}/tabla`)
    .then((res) => res.json())
    .then((data) => setTabla(data));

  apiFetch(`${API_URL}/partidos/torneo/${id}`)
    .then((res) => res.json())
    .then((data) => setPartidos(data));

  apiFetch(`${API_URL}/goles/torneo/${id}/goleadores`)
    .then((res) => res.json())
    .then((data) => setGoleadores(data));

  apiFetch(`${API_URL}/tarjetas/torneo/${id}`)
    .then((res) => res.json())
    .then((data) => setTarjetas(data));
};

  const abrirGeneradorFixture = () => {
    if (equipos.length < 2) {
      setAdminError("Agregá al menos 2 equipos antes de generar el fixture.");
      return;
    }
    if (partidos.length > 0) {
      setAdminError("Para generar el fixture automático, el torneo no debe tener partidos cargados.");
      return;
    }
    setAdminError("");
    setMostrarGeneradorFixture(true);
  };

  const abrirProximoResultado = () => {
    setPartidoResultado(siguientePartidoPendiente);
    setMostrarResultado(true);
  };

  const rehacerFixture = async () => {
    if (!window.confirm("¿Eliminar todo el fixture y volver a generarlo? Las jornadas se conservarán. Esta acción solo está disponible si todavía no hay resultados ni incidencias.")) return;
    setAdminError(""); setGenerandoFixture(true);
    try {
      const response = await apiFetch(`${API_URL}/partidos/torneo/${id}/fixture`, { method: "DELETE" });
      if (!response.ok) { const payload = await response.json().catch(() => null); throw new Error(payload?.message || "No se pudo rehacer el fixture"); }
      setPartidos([]); setMostrarGeneradorFixture(true);
    } catch (requestError) { setAdminError(requestError.message); } finally { setGenerandoFixture(false); }
  };

  const generarSiguienteLlave = async () => {
    setAdminError(""); setGenerandoFixture(true);
    try {
      const response = await apiFetch(`${API_URL}/partidos/torneo/${id}/generar-siguiente-llave`, { method: "POST" });
      const payload = await response.json().catch(() => null);
      if (!response.ok) throw new Error(payload?.message || "No se pudo generar la siguiente ronda");
      const [partidosResponse, fechasResponse] = await Promise.all([fetch(`${API_URL}/partidos/torneo/${id}`), fetch(`${API_URL}/fechas/torneo/${id}`)]);
      setPartidos(await partidosResponse.json()); setFechas(await fechasResponse.json());
    } catch (requestError) { setAdminError(requestError.message); } finally { setGenerandoFixture(false); }
  };

  const simularTorneo = async () => {
    if (!window.confirm("¿Simular todos los resultados? Se cargarán marcadores, goles, tarjetas y MVP de prueba, y se avanzarán los playoffs.")) return;
    setAdminError(""); setGenerandoFixture(true);
    try {
      const response = await apiFetch(`${API_URL}/partidos/torneo/${id}/simular`, { method: "POST" });
      const payload = await response.json().catch(() => null);
      if (!response.ok) throw new Error(payload?.message || "No se pudo simular el torneo");
      setPartidos(payload); recargarDatosTorneo();
    } catch (requestError) { setAdminError(requestError.message); } finally { setGenerandoFixture(false); }
  };

  const guardarResultadoRapido = async (partido, score) => {
    const eliminatorio = ["Final", "Semifinales", "Cuartos de final", "Octavos de final", "Primera ronda eliminatoria"].includes(partido.observaciones);
    if (eliminatorio && score.local === score.visitante) {
      setPartidoResultado({ ...partido, golesLocal: score.local, golesVisitante: score.visitante });
      setMostrarResultado(true);
      return;
    }
    setAdminError("");
    const response = await apiFetch(`${API_URL}/partidos/${partido.idPartido}/resultado`, {
      method: "PUT", headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ golesLocal: score.local, golesVisitante: score.visitante }),
    });
    const payload = await response.json().catch(() => null);
    if (!response.ok) { const message = payload?.message || "No se pudo guardar el resultado"; setAdminError(message); throw new Error(message); }
    setPartidos((current) => current.map((item) => item.idPartido === payload.idPartido ? payload : item));
    recargarDatosTorneo();
  };

  const suspenderPartido = async (partido) => {
    const motivo = window.prompt(`Motivo de la suspensión de ${partido.nombreEquipoLocal} vs ${partido.nombreEquipoVisitante}:`);
    if (motivo === null) return;
    if (!motivo.trim()) { setAdminError("Indicá el motivo de la suspensión."); return; }
    setAdminError("");
    try {
      const response = await apiFetch(`${API_URL}/partidos/${partido.idPartido}`, { method: "PUT", headers: { "Content-Type": "application/json" }, body: JSON.stringify({
        fechaHora: partido.fechaHora, estado: "SUSPENDIDO", golesLocal: partido.golesLocal ?? 0, golesVisitante: partido.golesVisitante ?? 0,
        observaciones: motivo.trim(), cancha: partido.cancha, idTorneo: Number(id), idFecha: partido.idFecha,
        idEquipoLocal: partido.idEquipoLocal, idEquipoVisitante: partido.idEquipoVisitante,
      }) });
      const payload = await response.json().catch(() => null);
      if (!response.ok) throw new Error(payload?.message || "No se pudo suspender el partido");
      setPartidos((current) => current.map((item) => item.idPartido === payload.idPartido ? payload : item));
    } catch (requestError) { setAdminError(requestError.message); }
  };

  const primaryAdminAction = (() => {
    if (torneo.estado?.toUpperCase() === "FINALIZADO") return { icon: "🏆", label: "Torneo finalizado", action: () => changeTab("resumen"), disabled: false };
    if (equipos.length < 2) return { icon: "👥", label: "Agregar equipos", action: () => changeTab("equipos"), disabled: false };
    if (partidos.length === 0) return { icon: "▦", label: usaLlave ? "Generar llaves" : "Generar fixture", action: abrirGeneradorFixture, disabled: generandoFixture };
    if (siguientePartidoPendiente) return { icon: "⚽", label: "Cargar próximo resultado", action: abrirProximoResultado, disabled: false };
    if (usaLlave && !torneo.idCampeon) return { icon: "→", label: "Generar siguiente fase", action: generarSiguienteLlave, disabled: generandoFixture };
    if (torneo.estado?.toUpperCase() === "ACTIVO") return { icon: "✓", label: "Finalizar torneo", action: () => cambiarEstadoTorneo("finalizar"), disabled: false };
    return { icon: "▦", label: "Revisar fixture", action: () => changeTab("fixture"), disabled: false };
  })();

  return (
    <div className="torneo-page">
      <TournamentHero torneo={torneo} fechaActual={fechaActual} perfilOrganizador={perfilOrganizador} activeTab={activeTab} onTabChange={changeTab} />

      {puedeEditar && <div className="tournament-admin-bar">
        <div className="tournament-admin-main">
          <button className="admin-primary-action" type="button" onClick={primaryAdminAction.action} disabled={primaryAdminAction.disabled}>
            <span>{primaryAdminAction.icon}</span><span><small>Siguiente acción</small><strong>{primaryAdminAction.label}</strong></span>
          </button>
          <div className="admin-context-actions">
            {activeTab === "resumen" && <><button type="button" onClick={() => setMostrarEdicion(true)}>Editar torneo</button>{torneo.estado?.toUpperCase() === "BORRADOR" && <button type="button" onClick={() => cambiarEstadoTorneo("publicar")}>Publicar</button>}{["BORRADOR", "PUBLICADO"].includes(torneo.estado?.toUpperCase()) && <button type="button" onClick={() => cambiarEstadoTorneo("iniciar")}>Iniciar</button>}{torneo.estado?.toUpperCase() === "ACTIVO" && <button type="button" onClick={() => cambiarEstadoTorneo("finalizar")}>Finalizar</button>}</>}
            {activeTab === "fixture" && <><button type="button" onClick={() => setMostrarFecha(true)}>+ Jornada</button><button type="button" onClick={() => setMostrarPartido(true)}>+ Partido</button><button type="button" onClick={partidos.length > 0 ? rehacerFixture : abrirGeneradorFixture} disabled={generandoFixture}>{partidos.length > 0 ? "Rehacer fixture" : "Generar fixture"}</button></>}
            {activeTab === "equipos" && <span>Administrá los clubes desde esta sección</span>}
            {activeTab === "stats" && <span>Goles y tarjetas se cargan desde cada partido</span>}
            {activeTab === "info" && <span>La información pública se modifica desde Editar torneo</span>}
          </div>
          <div className="admin-more-wrap">
            <button className="admin-more-toggle" type="button" aria-expanded={showMoreActions} onClick={() => setShowMoreActions((value) => !value)}>Más <span>⋯</span></button>
            {showMoreActions && <div className="admin-more-menu">
              <button type="button" onClick={() => { setPartidoResultado(null); setMostrarResultado(true); setShowMoreActions(false); }}>Elegir partido para resultado</button>
              <button type="button" onClick={() => { setMostrarEdicion(true); setShowMoreActions(false); }}>Editar torneo</button>
              <TournamentExports torneo={torneo} tabla={tabla} fechas={fechas} partidos={partidos} goleadores={goleadores} />
              {partidos.length > 0 && <button type="button" className="simulation-button" onClick={() => { setShowMoreActions(false); simularTorneo(); }} disabled={generandoFixture}>Simular torneo</button>}
            </div>}
          </div>
        </div>
        {adminError && <p className="tournament-form-error">{adminError}</p>}
      </div>}

      <main className="torneo-content">
        <div className="torneo-shell">
          {activeTab === "resumen" && puedeEditar && <TournamentReadiness torneoId={id} torneo={torneo} equipos={equipos} partidos={partidos} onGenerateFixture={abrirGeneradorFixture} onNextRound={generarSiguienteLlave} onNavigate={changeTab} />}
          {activeTab === "resumen" && campeonLiga && <div className="league-champion">
            <span className="league-champion-trophy">🏆</span>
            <div><small>Campeón del torneo</small><Link to={`/equipo/${campeonLiga.idEquipo}`}>{campeonLiga.nombreEquipo}</Link><p>{campeonLiga.puntos} puntos · {campeonLiga.ganados} partidos ganados</p></div>
          </div>}
          {activeTab === "resumen" && <div className="torneo-stats-grid" id="resumen">
            <StatsCard label="Torneo" value={torneo.nombre} />
            <StatsCard label="Partidos jugados" value={partidosJugados} />
            <StatsCard label="Próximos partidos" value={proximosPartidos.length} />
            <StatsCard
              label="Goleador líder"
              value={
                topGoleadores.length > 0
                  ? topGoleadores[0].nombreJugador
                  : "Sin datos"
              }
            />
          </div>}
          {activeTab === "resumen" && <section className="tournament-summary-highlights">
            {!usaLlave ? <article className="summary-highlight-card summary-podium"><header><span>Clasificación</span><h2>Podio actual</h2></header><div>{podio.length > 0 ? podio.map((fila, index) => { const team = buscarEquipo(fila.idEquipo); return <Link to={`/equipo/${fila.idEquipo}`} key={fila.idEquipo}><b>{index + 1}</b><EntityImage type="team" value={team?.escudo} alt="" /><strong>{fila.nombreEquipo}</strong><span>{fila.puntos} pts</span></Link>; }) : <p className="summary-highlight-empty">La tabla todavía no tiene posiciones.</p>}</div><button type="button" onClick={() => changeTab("tabla")}>Ver tabla completa →</button></article> : <article className="summary-highlight-card summary-knockout"><header><span>Eliminación directa</span><h2>{siguientePartidoPendiente?.observaciones || ultimoResultado?.observaciones || "Llave del torneo"}</h2></header><p>{siguientePartidoPendiente ? "La competencia continúa con el próximo cruce programado." : torneo.idCampeon ? `Campeón: ${torneo.nombreCampeon}` : "Revisá los cruces y resultados de cada fase."}</p><button type="button" onClick={() => changeTab("fixture")}>Ver llave completa →</button></article>}
            <article className="summary-highlight-card summary-featured-match"><header><span>Próximo partido</span><h2>{siguientePartidoPendiente ? formatMatchDate(siguientePartidoPendiente.fechaHora) : "Sin partidos programados"}</h2></header>{siguientePartidoPendiente ? <Link to={`/partido/${siguientePartidoPendiente.idPartido}`} className="summary-match-teams"><span><EntityImage type="team" value={siguientePartidoPendiente.escudoEquipoLocal} alt="" /><strong>{siguientePartidoPendiente.nombreEquipoLocal}</strong></span><b>VS</b><span><EntityImage type="team" value={siguientePartidoPendiente.escudoEquipoVisitante} alt="" /><strong>{siguientePartidoPendiente.nombreEquipoVisitante}</strong></span></Link> : <p className="summary-highlight-empty">No hay un próximo encuentro confirmado.</p>}<button type="button" onClick={() => changeTab("fixture")}>Ver fixture →</button></article>
            <article className="summary-highlight-card summary-last-result"><header><span>Último resultado</span><h2>{ultimoResultado ? formatMatchDate(ultimoResultado.fechaHora) : "Sin resultados"}</h2></header>{ultimoResultado ? <Link to={`/partido/${ultimoResultado.idPartido}`} className="summary-result-row"><span><EntityImage type="team" value={ultimoResultado.escudoEquipoLocal} alt="" /><strong>{ultimoResultado.nombreEquipoLocal}</strong></span><b>{ultimoResultado.golesLocal} - {ultimoResultado.golesVisitante}</b><span><EntityImage type="team" value={ultimoResultado.escudoEquipoVisitante} alt="" /><strong>{ultimoResultado.nombreEquipoVisitante}</strong></span></Link> : <p className="summary-highlight-empty">Todavía no se cargaron resultados.</p>}<button type="button" onClick={() => changeTab("fixture")}>Ver resultados →</button></article>
          </section>}
          {activeTab === "resumen" && <div className="tournament-overview-actions" aria-label="Accesos rápidos del torneo">
            <button type="button" onClick={() => changeTab("fixture")}><span>Próximo partido</span><strong>{proximosPartidos[0] ? `${proximosPartidos[0].nombreEquipoLocal} vs ${proximosPartidos[0].nombreEquipoVisitante}` : "Ver fixture"}</strong><b>Ir al fixture ›</b></button>
            <button type="button" onClick={() => changeTab("equipos")}><span>Participantes</span><strong>{equipos.length} equipos</strong><b>Ver equipos ›</b></button>
            <button type="button" onClick={() => changeTab("stats")}><span>Rendimiento</span><strong>{topGoleadores[0]?.nombreJugador || "Sin estadísticas"}</strong><b>Ver estadísticas ›</b></button>
          </div>}

          {activeTab === "info" && <section className="tournament-info-page" id="info">
            <div className="tournament-info-page-heading"><span>Información del torneo</span><h2>Todo lo que necesitás saber</h2><p>Organización, sede, reglamento y premios de la competencia.</p></div>
            <div className="tournament-info-page-grid">
              <article className="tournament-info-card"><span className="tournament-info-card-icon">⚽</span><div><small>Organización</small><h3>{perfilOrganizador?.nombrePublico || torneo.nombrePropietario || "Organizador no informado"}</h3>{perfilOrganizador ? <Link to={`/organizador/${perfilOrganizador.idUsuario}`}>Ver perfil del organizador →</Link> : <p>El torneo todavía no publicó un perfil de organizador.</p>}</div></article>
              <article className="tournament-info-card"><span className="tournament-info-card-icon">📍</span><div><small>Ubicación</small><h3>{torneo.direccionSede || "Complejo o predio no informado"}</h3><p>{torneo.ubicacion || "Ciudad o barrio no informado"}</p>{torneo.googleMapsUrl ? <a className="tournament-info-map" href={torneo.googleMapsUrl} target="_blank" rel="noopener noreferrer">Cómo llegar con Google Maps →</a> : <p className="tournament-info-missing">El organizador todavía no agregó el enlace de ubicación.</p>}</div></article>
              <article className="tournament-info-card tournament-info-card-wide"><span className="tournament-info-card-icon">📋</span><div><small>Reglamento</small><h3>Reglas de la competencia</h3>{torneo.reglasTexto ? <p className="tournament-info-long-text">{torneo.reglasTexto}</p> : <p className="tournament-info-missing">Todavía no hay información sobre las reglas.</p>}</div></article>
              <article className="tournament-info-card tournament-info-card-wide"><span className="tournament-info-card-icon">🏅</span><div><small>Premios</small><h3>Reconocimientos del torneo</h3>{torneo.premiosTexto ? <p className="tournament-info-long-text">{torneo.premiosTexto}</p> : <p className="tournament-info-missing">Todavía no hay información sobre los premios.</p>}</div></article>
            </div>
          </section>}

          {activeTab === "tabla" && torneo.formato?.toLowerCase() !== "eliminación directa" && torneo.formato?.toLowerCase() !== "zonas + playoffs" && <StandingsTable tabla={tabla} />}
          {activeTab === "tabla" && torneo.formato?.toLowerCase() === "zonas + playoffs" && estructura?.divisiones?.[0]?.fases?.[0]?.zonas?.map((zona) => {
            const ids = new Set(estructura.divisiones[0].cupos.filter((cupo) => cupo.idZona === zona.id).map((cupo) => cupo.idEquipo));
            return <StandingsTable key={zona.id} title={`Tabla ${zona.nombre}`} tabla={tabla.filter((fila) => ids.has(fila.idEquipo))} />;
          })}

          {activeTab === "equipos" && <TeamManagement
            torneoId={id}
            equipos={equipos}
            canManage={puedeEditar}
            onTeamDeleted={(teamId) => setEquipos((current) => current.filter((team) => team.idEquipo !== teamId))}
            onTeamSaved={(savedTeam) => {
              setEquipos((current) => {
                const exists = current.some((team) => team.idEquipo === savedTeam.idEquipo);
                return exists
                  ? current.map((team) => team.idEquipo === savedTeam.idEquipo ? savedTeam : team)
                  : [...current, savedTeam];
              });
            }}
          />}

          {activeTab === "stats" && <div className="torneo-dobles" id="stats">
            <TopScorersTable goleadores={topGoleadores} />
            <TopCardsTable jugadores={topAmarillas} />
          </div>}

          {activeTab === "fixture" && (torneo.formato?.toLowerCase() === "eliminación directa" || torneo.formato?.toLowerCase().includes("playoffs")) && <KnockoutBracket equipos={equipos} teamCount={cantidadEquiposLlave} partidos={partidos} canManage={puedeEditar} onNextRound={generarSiguienteLlave} loading={generandoFixture} />}

          {activeTab === "fixture" && <FixtureByDate
            fechas={fechas}
            partidos={partidos}
            onSelectPartido={setPartidoSeleccionado}
            canManage={puedeEditar}
            onEditDate={(fecha) => { setFechaEnEdicion(fecha); setMostrarFecha(true); }}
            onCloseDate={async (fecha) => {
              if (!window.confirm(`¿Cerrar la fecha ${fecha.numeroFecha}? Todos sus partidos deben estar finalizados.`)) return;
              setAdminError("");
              try {
                const response = await apiFetch(`${API_URL}/fechas/${fecha.idFecha}/cerrar`, { method: "PUT" });
                const payload = await response.json().catch(() => null);
                if (!response.ok) throw new Error(payload?.message || "No se pudo cerrar la jornada");
                const fechasResponse = await fetch(`${API_URL}/fechas/torneo/${id}`);
                setFechas(await fechasResponse.json());
              } catch (requestError) { setAdminError(requestError.message); }
            }}
            onDeleteDate={(fecha) => eliminarRecurso(`${API_URL}/fechas/${fecha.idFecha}`, `¿Eliminar la fecha ${fecha.numeroFecha}?`, () => setFechas((current) => current.filter((item) => item.idFecha !== fecha.idFecha)))}
            onEditMatch={(partido) => { setPartidoEnEdicion(partido); setMostrarPartido(true); }}
            onSuspendMatch={suspenderPartido}
            onDeleteMatch={(partido) => eliminarRecurso(`${API_URL}/partidos/${partido.idPartido}`, `¿Eliminar ${partido.nombreEquipoLocal} vs ${partido.nombreEquipoVisitante}?`, () => setPartidos((current) => current.filter((item) => item.idPartido !== partido.idPartido)))}
            onLoadResult={(partido) => { setPartidoResultado(partido); setMostrarResultado(true); }}
            onQuickResult={guardarResultadoRapido}
          />}

          <MatchModal
            key={partidoSeleccionado?.idPartido ?? "closed-match"}
            partido={partidoSeleccionado}
            onClose={() => setPartidoSeleccionado(null)}
            canManage={puedeEditar}
            onChanged={recargarDatosTorneo}
          />
          {mostrarResultado && (
            <ResultModal
              partidos={partidos}
              partidoInicial={partidoResultado}
              onClose={() => { setMostrarResultado(false); setPartidoResultado(null); }}
              onResultadoGuardado={(partidoActualizado, abrirDetalles = false) => {
                setPartidos((current) => current.map((partido) => partido.idPartido === partidoActualizado.idPartido ? partidoActualizado : partido));
                recargarDatosTorneo();
                if (abrirDetalles) setPartidoSeleccionado(partidoActualizado);
              }}
              onFechaCerrada={(fechaActualizada) => setFechas((current) => current.map((fecha) => fecha.idFecha === fechaActualizada.idFecha ? fechaActualizada : fecha))}
          />
)}
          {mostrarEdicion && (
            <TournamentFormModal
              torneo={torneo}
              onClose={() => setMostrarEdicion(false)}
              onSaved={setTorneo}
            />
          )}
          {mostrarPartido && (
            <MatchFormModal
              torneoId={id}
              fechas={fechas}
              equipos={equipos}
              partido={partidoEnEdicion}
              onClose={() => { setMostrarPartido(false); setPartidoEnEdicion(null); }}
              onSaved={(savedMatch) => {
                setPartidos((current) => current.some((item) => item.idPartido === savedMatch.idPartido) ? current.map((item) => item.idPartido === savedMatch.idPartido ? savedMatch : item) : [...current, savedMatch]);
              }}
            />
          )}
          {mostrarFecha && (
            <DateFormModal
              torneoId={id}
              nextNumber={Math.max(0, ...fechas.map((fecha) => fecha.numeroFecha)) + 1}
              fecha={fechaEnEdicion}
              onClose={() => { setMostrarFecha(false); setFechaEnEdicion(null); }}
              onSaved={(savedDate) => setFechas((current) => current.some((item) => item.idFecha === savedDate.idFecha) ? current.map((item) => item.idFecha === savedDate.idFecha ? savedDate : item) : [...current, savedDate])}
            />
          )}
          {mostrarGeneradorFixture && <FixtureGeneratorModal torneo={torneo} equipos={equipos} fechas={fechas} onClose={() => setMostrarGeneradorFixture(false)} onGenerated={async (generatedMatches) => { setGenerandoFixture(false); setPartidos(generatedMatches); const response = await fetch(`${API_URL}/fechas/torneo/${id}`); setFechas(await response.json()); }} />}
        </div>
      </main>
      {puedeEditar && <nav className="tournament-mobile-actions" aria-label="Acciones rápidas del organizador">
        <button type="button" className={activeTab === "resumen" ? "active" : ""} onClick={() => changeTab("resumen")}><span>⌂</span>Resumen</button>
        <button type="button" className={activeTab === "equipos" ? "active" : ""} onClick={() => changeTab("equipos")}><span>♙</span>Equipos</button>
        <button type="button" className={activeTab === "fixture" ? "active" : ""} onClick={() => changeTab("fixture")}><span>▦</span>Fixture</button>
        <button type="button" className="result" onClick={abrirProximoResultado} disabled={!siguientePartidoPendiente}><span>＋</span>Resultado</button>
      </nav>}
    </div>
  );
}

export default TorneoDetalle;
