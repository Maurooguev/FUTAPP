import { useEffect, useState } from "react";
import { API_URL, apiFetch } from "../../config/api";

function MatchIncidentForm({ partido, type, allowedTeamIds, onSaved, onCancel }) {
  const [players, setPlayers] = useState([]);
  const [form, setForm] = useState({ idJugador: "", minuto: "", tipo: type === "goal" ? "Jugada" : "AMARILLA", detail: "", fechasSuspension: 1 });
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    apiFetch(`${API_URL}/partidos/${partido.idPartido}/jugadores`).then(async (response) => {
      const payload = await response.json().catch(() => []);
      if (!response.ok) throw new Error(payload?.message || "No se pudieron cargar los jugadores");
      setPlayers(Array.isArray(payload) ? payload : []);
    }).catch((requestError) => setError(requestError.message));
  }, [partido.idPartido]);

  const eligiblePlayers = players.filter((player) => !player.suspendido);
  const availablePlayers = type === "goal" && Array.isArray(allowedTeamIds)
    ? eligiblePlayers.filter((player) => allowedTeamIds.includes(player.idEquipo))
    : eligiblePlayers;
  const localPlayers = availablePlayers.filter((player) => player.idEquipo === partido.idEquipoLocal);
  const visitorPlayers = availablePlayers.filter((player) => player.idEquipo === partido.idEquipoVisitante);

  const updateField = ({ target: { name, value } }) => {
    setForm((current) => ({ ...current, [name]: value }));
    setError("");
  };
  const handleSubmit = async (event) => {
    event.preventDefault(); setSaving(true); setError("");
    const player = players.find((item) => item.idJugador === Number(form.idJugador));
    const endpoint = type === "goal" ? "goles" : "tarjetas";
    const payload = type === "goal"
      ? { tipoGol: form.tipo, observacion: form.detail, idPartido: partido.idPartido, idJugador: player?.idJugador, idEquipo: player?.idEquipo }
      : { tipoTarjeta: form.tipo, motivo: form.detail, fechasSuspension: form.tipo === "ROJA" ? Number(form.fechasSuspension) : null, idPartido: partido.idPartido, idJugador: player?.idJugador, idEquipo: player?.idEquipo };
    if (form.minuto) payload.minuto = Number(form.minuto);
    try {
      const response = await apiFetch(`${API_URL}/${endpoint}`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(payload) });
      if (!response.ok) { const body = await response.json().catch(() => null); throw new Error(body?.message || "No se pudo guardar la incidencia"); }
      onSaved(await response.json());
      setForm((current) => ({ ...current, idJugador: "", minuto: "", detail: "" }));
    } catch (requestError) { setError(requestError.message); } finally { setSaving(false); }
  };

  return <form className="match-incident-form" onSubmit={handleSubmit}>
    <label>Jugador<select name="idJugador" value={form.idJugador} onChange={updateField} required><option value="">Seleccionar jugador</option>{localPlayers.length > 0 && <optgroup label={partido.nombreEquipoLocal}>{localPlayers.map((player) => <option key={player.idJugador} value={player.idJugador}>{player.nombre} {player.apellido}</option>)}</optgroup>}{visitorPlayers.length > 0 && <optgroup label={partido.nombreEquipoVisitante}>{visitorPlayers.map((player) => <option key={player.idJugador} value={player.idJugador}>{player.nombre} {player.apellido}</option>)}</optgroup>}</select></label>
    <div className="match-incident-row"><label>Minuto <small>Opcional</small><input type="number" name="minuto" min="1" max="130" placeholder="Ej. 34" value={form.minuto} onChange={updateField} /></label>
      <label>{type === "goal" ? "Tipo de gol" : "Tarjeta"}<select name="tipo" value={form.tipo} onChange={updateField}>{type === "goal" ? <><option>Jugada</option><option>Penal</option><option>Tiro libre</option><option>En contra</option></> : <><option value="AMARILLA">Amarilla</option><option value="ROJA">Roja</option></>}</select></label></div>
    <label>{type === "goal" ? "Observación" : "Motivo"} <small>Opcional</small><input name="detail" value={form.detail} onChange={updateField} placeholder={type === "goal" ? "Ej. Asistencia de Juan Pérez" : "Ej. Juego brusco"} /></label>
    {type === "card" && form.tipo === "ROJA" && <div className="match-red-suspension"><label>Fechas de suspensión<input type="number" name="fechasSuspension" min="1" max="99" value={form.fechasSuspension} onChange={updateField} required /></label><p className="match-red-card-notice">El jugador no podrá participar en las próximas {form.fechasSuspension || 1} {Number(form.fechasSuspension) === 1 ? "fecha" : "fechas"} que dispute su equipo. Una fecha libre no cuenta.</p></div>}
    {players.some((player) => player.suspendido) && <small className="match-suspended-help">Los jugadores suspendidos no aparecen entre las opciones habilitadas.</small>}
    {error && <p className="tournament-form-error">{error}</p>}
    <div className="match-incident-actions"><button type="button" onClick={onCancel}>Cancelar</button><button type="submit" disabled={saving}>{saving ? "Guardando..." : "Guardar"}</button></div>
  </form>;
}
export default MatchIncidentForm;
