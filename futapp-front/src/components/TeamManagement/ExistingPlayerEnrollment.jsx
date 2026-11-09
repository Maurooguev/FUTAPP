import { useEffect, useMemo, useState } from "react";
import { API_URL, apiFetch } from "../../config/api";
import EntityImage from "../EntityImage/EntityImage";

function ExistingPlayerEnrollment({ equipoId, enrollmentId, currentPlayers, onSaved, onCancel }) {
  const [players, setPlayers] = useState([]);
  const [selectedIds, setSelectedIds] = useState(new Set());
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);
  const currentIds = useMemo(() => new Set(currentPlayers.map((player) => player.idJugador)), [currentPlayers]);

  useEffect(() => {
    apiFetch(`${API_URL}/jugadores/equipo/${equipoId}`).then(async (response) => {
      const data = await response.json().catch(() => []);
      if (!response.ok) throw new Error("No se pudieron cargar los jugadores del club");
      setPlayers(data.filter((player) => !currentIds.has(player.idJugador)));
    }).catch((requestError) => setError(requestError.message));
  }, [equipoId, currentIds]);

  const togglePlayer = (idJugador) => {
    setSelectedIds((current) => {
      const next = new Set(current);
      if (next.has(idJugador)) next.delete(idJugador);
      else next.add(idJugador);
      return next;
    });
    setError("");
  };

  const toggleAll = () => {
    setSelectedIds((current) => current.size === players.length
      ? new Set()
      : new Set(players.map((player) => player.idJugador)));
    setError("");
  };

  const assignNumbers = (selectedPlayers) => {
    const usedNumbers = new Set(currentPlayers
      .filter((player) => player.estado === "ACTIVA")
      .map((player) => Number(player.numeroCamiseta))
      .filter((number) => Number.isInteger(number) && number >= 1 && number <= 99));

    return selectedPlayers.map((player) => {
      const preferred = Number(player.numeroCamiseta);
      let number = Number.isInteger(preferred) && preferred >= 1 && preferred <= 99 && !usedNumbers.has(preferred)
        ? preferred
        : null;
      if (!number) number = Array.from({ length: 99 }, (_, index) => index + 1).find((candidate) => !usedNumbers.has(candidate));
      if (!number) throw new Error("No quedan dorsales disponibles entre el 1 y el 99.");
      usedNumbers.add(number);
      return { player, number };
    });
  };

  const submit = async (event) => {
    event.preventDefault();
    if (selectedIds.size === 0) return setError("Seleccioná al menos un jugador.");
    setSaving(true); setError("");
    try {
      const assignments = assignNumbers(players.filter((player) => selectedIds.has(player.idJugador)));
      const savedPlayers = [];
      for (const { player, number } of assignments) {
        const response = await apiFetch(`${API_URL}/inscripciones/${enrollmentId}/jugadores`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ idJugador: player.idJugador, numeroCamiseta: number, posicion: player.posicion || "" }),
        });
        const payload = await response.json().catch(() => null);
        if (!response.ok) throw new Error(payload?.message || `No se pudo convocar a ${player.nombre} ${player.apellido}`);
        savedPlayers.push({ ...player, ...payload, nombre: player.nombre, apellido: player.apellido });
      }
      onSaved(savedPlayers);
    } catch (requestError) { setError(requestError.message); }
    finally { setSaving(false); }
  };

  const allSelected = players.length > 0 && selectedIds.size === players.length;

  return <form className="roster-player-form" onSubmit={submit}>
    <div className="existing-player-heading"><div><h3>Convocar jugadores del club</h3><p>Seleccioná varios jugadores o convocá el plantel completo. Los dorsales libres se asignan automáticamente y después podés editarlos.</p></div>{players.length > 0 && <button type="button" className="existing-player-select-all" onClick={toggleAll}>{allSelected ? "Quitar selección" : "Convocar todos"}</button>}</div>
    <div className="existing-player-selection-summary">{selectedIds.size} de {players.length} seleccionados</div>
    <div className="existing-player-grid">{players.length === 0 ? <p>No quedan jugadores del club por convocar.</p> : players.map((player) => {
      const selected = selectedIds.has(player.idJugador);
      return <button type="button" className={selected ? "selected" : ""} key={player.idJugador} onClick={() => togglePlayer(player.idJugador)} aria-pressed={selected}>
        <span className={`existing-player-check ${selected ? "selected" : ""}`}>{selected ? "✓" : ""}</span>
        <span className="existing-player-avatar"><EntityImage type="player" value={player.foto} alt="" /></span>
        <span><strong>{player.nombre} {player.apellido}</strong><small>{player.codigoPublico} · {player.posicion || "Sin posición"}</small></span>
      </button>;
    })}</div>
    {error && <p className="tournament-form-error">{error}</p>}
    <div className="tournament-form-actions"><button type="button" className="tournament-form-cancel" onClick={onCancel}>Cancelar</button><button className="tournament-form-save" disabled={saving || selectedIds.size === 0}>{saving ? `Convocando ${selectedIds.size}…` : `Convocar ${selectedIds.size || ""} al torneo`}</button></div>
  </form>;
}
export default ExistingPlayerEnrollment;
