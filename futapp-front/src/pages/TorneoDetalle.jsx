import { useEffect, useMemo, useState } from "react";
import { useParams } from "react-router-dom";

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

function TorneoDetalle() {
  const { id } = useParams();
  const { usuario } = useAuth();

  const [torneo, setTorneo] = useState(null);
  const puedeEditar = puedeAdministrarTorneo(usuario, torneo);
  const [tabla, setTabla] = useState([]);
  const [goleadores, setGoleadores] = useState([]);
  const [fechas, setFechas] = useState([]);
  const [partidos, setPartidos] = useState([]);
  const [tarjetas, setTarjetas] = useState([]);
  const [equipos, setEquipos] = useState([]);
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
    const destino = accion === "iniciar" ? "iniciar el torneo" : "finalizar definitivamente el torneo";
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
    fetch(`${API_URL}/torneos/${id}`)
      .then((res) => res.json())
      .then((data) => setTorneo(data))
      .catch((err) => console.error("Torneo:", err));

    fetch(`${API_URL}/torneos/${id}/tabla`)
      .then((res) => res.json())
      .then((data) => setTabla(data))
      .catch((err) => console.error("Tabla:", err));

    fetch(`${API_URL}/goles/torneo/${id}/goleadores`)
      .then((res) => res.json())
      .then((data) => setGoleadores(data))
      .catch((err) => console.error("Goleadores:", err));

    fetch(`${API_URL}/fechas/torneo/${id}`)
      .then((res) => res.json())
      .then((data) => setFechas(data))
      .catch((err) => console.error("Fechas:", err));

    fetch(`${API_URL}/partidos/torneo/${id}`)
      .then((res) => res.json())
      .then((data) => setPartidos(data))
      .catch((err) => console.error("Partidos:", err));

    fetch(`${API_URL}/tarjetas`)
      .then((res) => res.json())
      .then((data) => setTarjetas(data))
      .catch((err) => console.error("Tarjetas:", err));

    fetch(`${API_URL}/equipos/torneo/${id}`)
      .then((res) => res.json())
      .then((data) => setEquipos(data))
      .catch((err) => console.error("Equipos:", err));
  }, [id]);

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
        return a.fechaHora.localeCompare(b.fechaHora);
      })
      .slice(0, 6);
  }, [partidos]);

  const partidosJugados = useMemo(() => {
    return partidos.filter(
      (partido) => partido.estado?.toUpperCase() === "FINALIZADO"
    ).length;
  }, [partidos]);

  if (!torneo) {
    return <p style={{ padding: "20px" }}>Cargando...</p>;
  }

const recargarDatosTorneo = () => {
  fetch(`${API_URL}/torneos/${id}/tabla`)
    .then((res) => res.json())
    .then((data) => setTabla(data));

  fetch(`${API_URL}/partidos/torneo/${id}`)
    .then((res) => res.json())
    .then((data) => setPartidos(data));

  fetch(`${API_URL}/goles/torneo/${id}/goleadores`)
    .then((res) => res.json())
    .then((data) => setGoleadores(data));

  fetch(`${API_URL}/tarjetas`)
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

  const rehacerFixture = async () => {
    if (!window.confirm("¿Eliminar todo el fixture y volver a generarlo? Las jornadas se conservarán. Esta acción solo está disponible si todavía no hay resultados ni incidencias.")) return;
    setAdminError(""); setGenerandoFixture(true);
    try {
      const response = await apiFetch(`${API_URL}/partidos/torneo/${id}/fixture`, { method: "DELETE" });
      if (!response.ok) { const payload = await response.json().catch(() => null); throw new Error(payload?.message || "No se pudo rehacer el fixture"); }
      setPartidos([]); setMostrarGeneradorFixture(true);
    } catch (requestError) { setAdminError(requestError.message); } finally { setGenerandoFixture(false); }
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

  return (
    <div className="torneo-page">
      <TournamentHero torneo={torneo} fechaActual={fechaActual} />

      {puedeEditar && (
        <div className="admin-actions">
          <button onClick={() => setMostrarEdicion(true)}>Editar torneo</button>
          {torneo.estado?.toUpperCase() === "BORRADOR" && <button onClick={() => cambiarEstadoTorneo("iniciar")}>Iniciar torneo</button>}
          {torneo.estado?.toUpperCase() === "ACTIVO" && <button onClick={() => cambiarEstadoTorneo("finalizar")}>Finalizar torneo</button>}
          <button onClick={() => { setPartidoResultado(null); setMostrarResultado(true); }}>Cargar resultado</button>
          <button onClick={() => setMostrarFecha(true)}>Agregar jornada</button>
          <button onClick={() => setMostrarPartido(true)}>Agregar partido</button>
          {torneo.formato?.toUpperCase() === "LIGA" && (
            <button onClick={partidos.length > 0 ? rehacerFixture : abrirGeneradorFixture} disabled={generandoFixture}>
              {generandoFixture ? "Procesando..." : partidos.length > 0 ? "Rehacer fixture" : "Generar fixture automático"}
            </button>
          )}
        </div>
      )}
      {adminError && <div className="admin-actions"><p className="tournament-form-error">{adminError}</p></div>}

      <main className="torneo-content">
        <div className="torneo-shell">
          {puedeEditar && <TournamentReadiness torneoId={id} equipos={equipos} partidos={partidos} onGenerateFixture={abrirGeneradorFixture} />}
          <div className="torneo-stats-grid" id="resumen">
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
          </div>

          <StandingsTable tabla={tabla} />

          <TeamManagement
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
          />

          <div className="torneo-dobles" id="stats">
            <TopScorersTable goleadores={topGoleadores} />
            <TopCardsTable jugadores={topAmarillas} />
          </div>

          <FixtureByDate
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
          />

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
              onResultadoGuardado={(partidoActualizado) => {
                setPartidos((current) => current.map((partido) => partido.idPartido === partidoActualizado.idPartido ? partidoActualizado : partido));
                recargarDatosTorneo();
                setPartidoSeleccionado(partidoActualizado);
              }}
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
    </div>
  );
}

export default TorneoDetalle;
