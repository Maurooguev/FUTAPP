import { useEffect, useState } from "react";
import { API_URL, apiFetch } from "../../config/api";
import PlayerForm from "./PlayerForm";
import "../TournamentFormModal/TournamentFormModal.css";
import "./TeamManagement.css";
import { Link } from "react-router-dom";
import ExistingPlayerEnrollment from "./ExistingPlayerEnrollment";
import RosterEnrollmentForm from "./RosterEnrollmentForm";

function TeamRosterModal({ equipo, torneoId, canManage, onClose }) {
  const [players, setPlayers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editingPlayer, setEditingPlayer] = useState(null);
  const [actionError, setActionError] = useState("");
  const [enrollmentId, setEnrollmentId] = useState(null);
  const [showExisting, setShowExisting] = useState(false);
  const [editingEnrollment, setEditingEnrollment] = useState(null);
  const [reactivating, setReactivating] = useState(false);
  const [tournamentName, setTournamentName] = useState("");
  const [reloadKey, setReloadKey] = useState(0);

  const closeForm = () => {
    setShowForm(false);
    setEditingPlayer(null);
  };

  const savePlayer = async (savedPlayer) => {
    if (enrollmentId) {
      const history = await fetch(`${API_URL}/inscripciones/jugadores/${savedPlayer.idJugador}`).then((res) => res.json()).catch(() => []);
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
    setReloadKey((current) => current + 1);
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
    } catch (requestError) {
      setActionError(requestError.message);
    }
  };

  useEffect(() => {
    fetch(`${API_URL}/inscripciones/equipos/${equipo.idEquipo}`)
      .then((response) => response.json())
      .then(async (history) => {
        const enrollment = Array.isArray(history) ? history.find((item) => item.idTorneo === Number(torneoId)) : null;
        if (!enrollment) throw new Error("No se encontró la inscripción del equipo");
        setEnrollmentId(enrollment.idInscripcion);
        setTournamentName(enrollment.nombreTorneo);
        const roster = await fetch(`${API_URL}/inscripciones/${enrollment.idInscripcion}/jugadores`).then((response) => response.json());
        const fullPlayers = await Promise.all((Array.isArray(roster) ? roster : []).map(async (item) => {
          const player = await fetch(`${API_URL}/jugadores/${item.idJugador}`).then((response) => response.json());
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
        {canManage && !showForm && !showExisting && <div className="roster-add-actions"><button type="button" className="tournament-form-save" onClick={() => { setEditingPlayer(null); setShowForm(true); }}>+ Crear jugador</button><button type="button" className="tournament-form-cancel" onClick={() => setShowExisting(true)}>Agregar jugador existente</button></div>}
        {showForm && <PlayerForm equipoId={equipo.idEquipo} player={editingPlayer} onCancel={closeForm} onSaved={savePlayer} />}
        {showExisting && enrollmentId && <ExistingPlayerEnrollment equipoId={equipo.idEquipo} enrollmentId={enrollmentId} currentPlayers={players} onCancel={() => setShowExisting(false)} onSaved={() => { setShowExisting(false); setReloadKey((current) => current + 1); }} />}
        {actionError && <p className="tournament-form-error">{actionError}</p>}
        <div className="roster-list">
          {loading ? <p>Cargando plantel...</p> : players.length === 0 ? <p className="roster-empty">Todavía no hay jugadores.</p> : players.map((player) => (
            <article className={`roster-player ${player.estado === "INACTIVA" ? "inactive" : ""}`} key={player.idInscripcionJugador || player.idJugador}>
              <strong className="roster-number">{player.numeroCamiseta}</strong>
              <div><h3><Link to={`/jugador/${player.idJugador}`} onClick={onClose}>{player.nombre} {player.apellido}</Link></h3><p>{player.posicion}{player.apodo ? ` · ${player.apodo}` : ""}</p></div>
              <div className="roster-player-actions"><span className="roster-status">{player.estado}{player.fechaBaja ? ` · Baja ${player.fechaBaja}` : ""}</span>{canManage && player.estado === "ACTIVA" && <><button type="button" onClick={() => { setEditingEnrollment(player); setReactivating(false); }}>Dorsal/posición</button><button type="button" onClick={() => { setEditingPlayer(player); setShowForm(true); }}>Editar jugador</button><button type="button" className="danger" onClick={() => deactivatePlayer(player)}>Dar de baja</button></>}{canManage && player.estado === "INACTIVA" && <button type="button" className="reactivate" onClick={() => { setEditingEnrollment(player); setReactivating(true); }}>Reactivar</button>}</div>
              {editingEnrollment?.idInscripcionJugador === player.idInscripcionJugador && <RosterEnrollmentForm player={player} reactivate={reactivating} onCancel={() => setEditingEnrollment(null)} onSaved={(updated) => { setPlayers((current) => current.map((item) => item.idInscripcionJugador === updated.idInscripcionJugador ? { ...item, ...updated } : item)); setEditingEnrollment(null); }} />}
            </article>
          ))}
        </div>
      </section>
    </div>
  );
}

export default TeamRosterModal;
