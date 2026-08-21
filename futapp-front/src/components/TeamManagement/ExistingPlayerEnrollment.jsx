import { useEffect, useState } from "react";
import { API_URL, apiFetch, assetUrl } from "../../config/api";

function ExistingPlayerEnrollment({ enrollmentId, currentPlayers, onSaved, onCancel }) {
  const [players, setPlayers] = useState([]);
  const [playerId, setPlayerId] = useState("");
  const [number, setNumber] = useState("");
  const [position, setPosition] = useState("");
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);
  const [query, setQuery] = useState("");

  useEffect(() => {
    fetch(`${API_URL}/jugadores`).then((res) => res.json()).then((items) => {
      const currentIds = new Set(currentPlayers.map((player) => player.idJugador));
      setPlayers(Array.isArray(items) ? items.filter((player) => !currentIds.has(player.idJugador)) : []);
    }).catch(() => setError("No se pudieron cargar los jugadores"));
  }, [currentPlayers]);

  const selectPlayer = (value) => {
    setPlayerId(value);
    const selected = players.find((player) => player.idJugador === Number(value));
    setPosition(selected?.posicion || "");
  };

  const submit = async (event) => {
    event.preventDefault(); setSaving(true); setError("");
    try {
      const response = await apiFetch(`${API_URL}/inscripciones/${enrollmentId}/jugadores`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ idJugador: Number(playerId), numeroCamiseta: Number(number), posicion: position }) });
      const payload = await response.json().catch(() => null);
      if (!response.ok) throw new Error(payload?.message || "No se pudo agregar el jugador");
      const playerResponse = await fetch(`${API_URL}/jugadores/${playerId}`);
      const player = await playerResponse.json();
      onSaved({ ...player, ...payload, nombre: player.nombre, apellido: player.apellido });
    } catch (requestError) { setError(requestError.message); } finally { setSaving(false); }
  };

  const visiblePlayers = players.filter((player) => `${player.nombre} ${player.apellido} ${player.apodo || ""} ${player.codigoPublico || ""} ${player.nombreEquipo || ""}`.toLowerCase().includes(query.toLowerCase()));

  return <form className="roster-player-form" onSubmit={submit}><h3>Agregar jugador existente</h3><p>Buscá un perfil ya creado y agregalo solamente al plantel de este torneo.</p><label>Buscar jugador<input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Nombre, apodo, código o equipo..." /></label><div className="existing-player-grid">{visiblePlayers.length === 0 ? <p>No hay jugadores disponibles para esta búsqueda.</p> : visiblePlayers.slice(0, 12).map((player) => <button type="button" className={Number(playerId) === player.idJugador ? "selected" : ""} key={player.idJugador} onClick={() => selectPlayer(String(player.idJugador))}><span className="existing-player-avatar">{player.foto ? <img src={assetUrl(player.foto)} alt="" /> : player.numeroCamiseta || "—"}</span><span><strong>{player.nombre} {player.apellido}</strong><small>{player.codigoPublico} · {player.nombreEquipo}</small></span></button>)}</div>{playerId && <div className="tournament-form-grid"><label>Dorsal para este torneo<input type="number" min="1" max="99" value={number} onChange={(e) => setNumber(e.target.value)} required /></label><label>Posición para este torneo<input value={position} onChange={(e) => setPosition(e.target.value)} /></label></div>}{error && <p className="tournament-form-error">{error}</p>}<div className="tournament-form-actions"><button type="button" className="tournament-form-cancel" onClick={onCancel}>Cancelar</button><button className="tournament-form-save" disabled={saving || !playerId}>{saving ? "Agregando..." : "Agregar al plantel"}</button></div></form>;
}

export default ExistingPlayerEnrollment;
