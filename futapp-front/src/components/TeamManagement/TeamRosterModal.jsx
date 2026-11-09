import { useEffect, useState } from "react";
import { API_URL, apiFetch } from "../../config/api";
import PlayerForm from "./PlayerForm";
import "../TournamentFormModal/TournamentFormModal.css";
import "./TeamManagement.css";
import { Link } from "react-router-dom";
import ExistingPlayerEnrollment from "./ExistingPlayerEnrollment";
import RosterEnrollmentForm from "./RosterEnrollmentForm";

function TeamRosterModal({ equipo, torneoId, canManage, onClose, onRosterChanged }) {
  const [players, setPlayers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editingPlayer, setEditingPlayer] = useState(null);
  const [actionError, setActionError] = useState("");
  const [actionNotice, setActionNotice] = useState("");
  const [enrollmentId, setEnrollmentId] = useState(null);
  const [showExisting, setShowExisting] = useState(false);
  const [editingEnrollment, setEditingEnrollment] = useState(null);
  const [reactivating, setReactivating] = useState(false);
  const [generatingPlayers, setGeneratingPlayers] = useState(false);
  const [tournamentName, setTournamentName] = useState("");
  const [reloadKey, setReloadKey] = useState(0);
  const [showQuickPlayers, setShowQuickPlayers] = useState(false);
  const [quickPlayerNames, setQuickPlayerNames] = useState("");
  const [savingQuickPlayers, setSavingQuickPlayers] = useState(false);
  const parsedPlayerNames = quickPlayerNames.split(/\r?\n/).map((name) => name.trim()).filter(Boolean);

  const saveQuickPlayers = async () => {
    if (!enrollmentId || parsedPlayerNames.length === 0) return;
    setSavingQuickPlayers(true); setActionError(""); setActionNotice("");
    try {
      const response = await apiFetch(`${API_URL}/inscripciones/${enrollmentId}/jugadores/lote`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ nombres: parsedPlayerNames }) });
      const payload = await response.json().catch(() => null);
      if (!response.ok) throw new Error(payload?.message || "No se pudieron cargar los jugadores");
      setQuickPlayerNames(""); setShowQuickPlayers(false); setReloadKey((current) => current + 1);
      onRosterChanged?.();
      setActionNotice(`${payload.length} ${payload.length === 1 ? "jugador cargado" : "jugadores cargados"}. Podés completar dorsales, posiciones y datos personales individualmente.`);
    } catch (requestError) { setActionError(requestError.message); }
    finally { setSavingQuickPlayers(false); }
  };

  const closeForm = () => {
    setShowForm(false);
    setEditingPlayer(null);
  };

  const savePlayer = async (savedPlayer, options = {}) => {
    setActionError("");
    if (enrollmentId) {
      const history = await apiFetch(`${API_URL}/inscripciones/jugadores/${savedPlayer.idJugador}`).then((res) => res.ok ? res.json() : []).catch(() => []);
      const alreadyEnrolled = Array.isArray(history) && history.some((item) => item.idInscripcionEquipo === enrollmentId);
      if (!alreadyEnrolled) {
        const response = await apiFetch(`${API_URL}/inscripciones/${enrollmentId}/jugadores`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ idJugador: savedPlayer.idJugador, numeroCamiseta: savedPlayer.numeroCamiseta, posicion: savedPlayer.posicion }) });
        if (!response.ok) {
          const payload = await response.json().catch(() => null);
          setActionError(payload?.message || "El jugador se creó, pero no pudo agregarse a este plantel");
          return;
        }
      }
    }
    closeForm();
    setActionNotice(options.message || (options.privateChangeRequested
      ? "Se envió una solicitud al jugador. Los cambios se aplicarán cuando la apruebe."
      : "La operación se realizó correctamente."));
    setReloadKey((current) => current + 1);
    onRosterChanged?.();
  };

  const deactivatePlayer = async (player) => {
    const confirmed = window.confirm(`¿Dar de baja a ${player.nombre} ${player.apellido} solamente de este torneo? Su perfil y su historial se conservarán.`);
    if (!confirmed) return;
    setActionError("");
    try {
      const response = await apiFetch(`${API_URL}/inscripciones/jugadores/${player.idInscripcionJugador}/baja`, { method: "PUT" });
      if (!response.ok) {
        const payload = await response.json().catch(() => null);
        throw new Error(payload?.message || "No se pudo dar de baja al jugador");
      }
      const updated = await response.json();
      setPlayers((current) => current.map((item) => item.idInscripcionJugador === updated.idInscripcionJugador ? { ...item, ...updated } : item));
      onRosterChanged?.();
    } catch (requestError) {
      setActionError(requestError.message);
    }
  };

  const generateTestPlayers = async () => {
    const suggested = Math.max(1, 5 - players.filter((player) => player.estado === "ACTIVA").length);
    const value = window.prompt("¿Cuántos jugadores de prueba querés generar?", String(suggested));
    if (value === null) return;
    const amount = Number(value);
    if (!Number.isInteger(amount) || amount < 1 || amount > 30) {
      setActionError("Ingresá una cantidad entre 1 y 30");
      return;
    }
    if (!window.confirm(`Se crearán ${amount} jugadores editables con nombres aleatorios. ¿Continuar?`)) return;
    setGeneratingPlayers(true);
    setActionError("");
    setActionNotice("");
    try {
      const response = await apiFetch(`${API_URL}/inscripciones/${enrollmentId}/jugadores/prueba?cantidad=${amount}`, { method: "POST" });
      const payload = await response.json().catch(() => null);
      if (!response.ok) throw new Error(payload?.message || "No se pudieron generar los jugadores");
      setActionNotice(`Se generaron ${payload.length} jugadores de prueba. Podés editar sus datos cuando quieras.`);
      setReloadKey((current) => current + 1);
      onRosterChanged?.();
    } catch (requestError) {
      setActionError(requestError.message);
    } finally {
      setGeneratingPlayers(false);
    }
  };

  useEffect(() => {
    setLoading(true);
    setActionError("");
    apiFetch(`${API_URL}/inscripciones/equipos/${equipo.idEquipo}`)
      .then(async (response) => {
        const payload = await response.json().catch(() => null);
        if (!response.ok) throw new Error(payload?.message || "No se pudo cargar la inscripción del equipo");
        return payload;
      })
      .then(async (history) => {
        const enrollment = Array.isArray(history) ? history.find((item) => item.idTorneo === Number(torneoId)) : null;
        if (!enrollment) throw new Error("No se encontró la inscripción del equipo");
        setEnrollmentId(enrollment.idInscripcion);
        setTournamentName(enrollment.nombreTorneo);
        const rosterResponse = await apiFetch(`${API_URL}/inscripciones/${enrollment.idInscripcion}/jugadores`);
        const roster = await rosterResponse.json().catch(() => null);
        if (!rosterResponse.ok) throw new Error(roster?.message || "No se pudo cargar el plantel del torneo");
        const fullPlayers = await Promise.all((Array.isArray(roster) ? roster : []).map(async (item) => {
          const player = await apiFetch(`${API_URL}/jugadores/${item.idJugador}`).then((response) => response.json());
          return { ...player, ...item, nombre: player.nombre, apellido: player.apellido };
        }));
        setPlayers(fullPlayers);
      })
      .catch((requestError) => setActionError(requestError.message))
      .finally(() => setLoading(false));
  }, [equipo.idEquipo, torneoId, reloadKey]);

  return (
    <div className="tournament-form-backdrop" onMouseDown={onClose}>
      <section className="tournament-form-modal roster-modal" role="dialog" aria-modal="true" onMouseDown={(e) => e.stopPropagation()}>
        <button type="button" className="tournament-form-close" aria-label="Cerrar" onClick={onClose}>×</button>
        <span className="tournament-form-chip">Plantel por torneo</span>
        <h2>{equipo.nombre}</h2>
        <p><strong>{tournamentName || "Torneo"}</strong> · {players.filter((player) => player.estado === "ACTIVA").length} activos · {players.filter((player) => player.estado === "INACTIVA").length} bajas.</p>
        {canManage && !showForm && !showExisting && <div className="roster-add-actions"><button type="button" className="tournament-form-save" onClick={() => setShowQuickPlayers((value) => !value)}>Carga rápida</button><button type="button" onClick={() => { setEditingPlayer(null); setShowForm(true); }}>+ Crear uno</button><button type="button" className="tournament-form-cancel" onClick={() => setShowExisting(true)}>Convocar existente</button><button type="button" className="roster-generate-players" onClick={generateTestPlayers} disabled={!enrollmentId || generatingPlayers}>{generatingPlayers ? "Generando..." : "Generar aleatorios"}</button></div>}
        {showQuickPlayers && <div className="quick-entry-panel roster-quick-entry"><div><strong>Cargar jugadores por lista</strong><small>Escribí nombre y apellido, uno por línea. Después podés editar dorsal, posición y el resto de sus datos.</small></div><textarea autoFocus rows="7" value={quickPlayerNames} onChange={(event) => setQuickPlayerNames(event.target.value)} placeholder={"Juan Pérez\nMateo González\nLucas Fernández"} /><div className="quick-entry-summary"><span>{parsedPlayerNames.length} {parsedPlayerNames.length === 1 ? "jugador" : "jugadores"}</span><div><button type="button" onClick={() => setShowQuickPlayers(false)}>Cancelar</button><button type="button" className="primary" disabled={savingQuickPlayers || parsedPlayerNames.length === 0} onClick={saveQuickPlayers}>{savingQuickPlayers ? "Cargando…" : "Cargar jugadores"}</button></div></div></div>}
        {showForm && <PlayerForm equipoId={equipo.idEquipo} torneoId={torneoId} player={editingPlayer} onCancel={closeForm} onSaved={savePlayer} />}
        {showExisting && enrollmentId && <ExistingPlayerEnrollment equipoId={equipo.idEquipo} enrollmentId={enrollmentId} currentPlayers={players} onCancel={() => setShowExisting(false)} onSaved={() => { setShowExisting(false); setReloadKey((current) => current + 1); onRosterChanged?.(); }} />}
        {actionNotice && <p className="roster-notice">{actionNotice}</p>}
        {actionError && <p className="tournament-form-error">{actionError}</p>}
        <div className="roster-list">
          {loading ? <p>Cargando plantel...</p> : players.length === 0 ? <p className="roster-empty">Todavía no hay jugadores.</p> : players.map((player) => (
            <article className={`roster-player ${player.estado === "INACTIVA" ? "inactive" : ""}`} key={player.idInscripcionJugador || player.idJugador}>
              <strong className="roster-number">{player.numeroCamiseta}</strong>
              <div><h3><Link to={`/jugador/${player.idJugador}`} onClick={onClose}>{player.nombre} {player.apellido}</Link></h3><p>{player.posicion || "Sin posición"}{player.apodo ? ` · ${player.apodo}` : ""}</p></div>
              <div className="roster-player-actions"><span className="roster-status">{player.estado}{player.fechaBaja ? ` · Baja ${player.fechaBaja}` : ""}</span>{canManage && player.estado === "ACTIVA" && <><button type="button" onClick={() => { setEditingEnrollment(player); setReactivating(false); }}>Dorsal/posición</button><button type="button" onClick={() => { setEditingPlayer(player); setShowForm(true); }}>Editar jugador</button><button type="button" className="danger" onClick={() => deactivatePlayer(player)}>Dar de baja</button></>}{canManage && player.estado === "INACTIVA" && <button type="button" className="reactivate" onClick={() => { setEditingEnrollment(player); setReactivating(true); }}>Reactivar</button>}</div>
              {editingEnrollment?.idInscripcionJugador === player.idInscripcionJugador && <RosterEnrollmentForm player={player} reactivate={reactivating} onCancel={() => setEditingEnrollment(null)} onSaved={(updated) => { setPlayers((current) => current.map((item) => item.idInscripcionJugador === updated.idInscripcionJugador ? { ...item, ...updated } : item)); setEditingEnrollment(null); onRosterChanged?.(); }} />}
            </article>
          ))}
        </div>
      </section>
    </div>
  );
}

export default TeamRosterModal;
