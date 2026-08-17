import { useEffect, useState } from "react";
import { API_URL, apiFetch } from "../../config/api";
import PlayerForm from "./PlayerForm";
import "../TournamentFormModal/TournamentFormModal.css";
import "./TeamManagement.css";

function TeamRosterModal({ equipo, canManage, onClose }) {
  const [players, setPlayers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editingPlayer, setEditingPlayer] = useState(null);
  const [actionError, setActionError] = useState("");

  const closeForm = () => {
    setShowForm(false);
    setEditingPlayer(null);
  };

  const savePlayer = (savedPlayer) => {
    setPlayers((current) => {
      const exists = current.some((player) => player.idJugador === savedPlayer.idJugador);
      return exists
        ? current.map((player) => player.idJugador === savedPlayer.idJugador ? savedPlayer : player)
        : [...current, savedPlayer];
    });
    closeForm();
  };

  const deletePlayer = async (player) => {
    const confirmed = window.confirm(`¿Eliminar a ${player.nombre} ${player.apellido} del plantel?`);
    if (!confirmed) return;
    setActionError("");
    try {
      const response = await apiFetch(`${API_URL}/jugadores/${player.idJugador}`, { method: "DELETE" });
      if (!response.ok) {
        const payload = await response.json().catch(() => null);
        throw new Error(payload?.message || "No se pudo eliminar el jugador");
      }
      setPlayers((current) => current.filter((item) => item.idJugador !== player.idJugador));
    } catch (requestError) {
      setActionError(requestError.message);
    }
  };

  useEffect(() => {
    fetch(`${API_URL}/jugadores/equipo/${equipo.idEquipo}`)
      .then((response) => response.json())
      .then(setPlayers)
      .finally(() => setLoading(false));
  }, [equipo.idEquipo]);

  return (
    <div className="tournament-form-backdrop" onMouseDown={onClose}>
      <section className="tournament-form-modal roster-modal" role="dialog" aria-modal="true" onMouseDown={(e) => e.stopPropagation()}>
        <button type="button" className="tournament-form-close" aria-label="Cerrar" onClick={onClose}>×</button>
        <span className="tournament-form-chip">Plantel</span>
        <h2>{equipo.nombre}</h2>
        <p>{players.length} jugadores registrados.</p>
        {canManage && !showForm && <button type="button" className="tournament-form-save" onClick={() => { setEditingPlayer(null); setShowForm(true); }}>+ Agregar jugador</button>}
        {showForm && <PlayerForm equipoId={equipo.idEquipo} player={editingPlayer} onCancel={closeForm} onSaved={savePlayer} />}
        {actionError && <p className="tournament-form-error">{actionError}</p>}
        <div className="roster-list">
          {loading ? <p>Cargando plantel...</p> : players.length === 0 ? <p className="roster-empty">Todavía no hay jugadores.</p> : players.map((player) => (
            <article className="roster-player" key={player.idJugador}>
              <strong className="roster-number">{player.numeroCamiseta}</strong>
              <div><h3>{player.nombre} {player.apellido}</h3><p>{player.posicion}{player.dni ? ` · DNI ${player.dni}` : ""}</p></div>
              <div className="roster-player-actions">
                <span className="roster-status">{player.estado}</span>
                {canManage && <><button type="button" onClick={() => { setEditingPlayer(player); setShowForm(true); }}>Editar</button><button type="button" className="danger" onClick={() => deletePlayer(player)}>Eliminar</button></>}
              </div>
            </article>
          ))}
        </div>
      </section>
    </div>
  );
}

export default TeamRosterModal;
