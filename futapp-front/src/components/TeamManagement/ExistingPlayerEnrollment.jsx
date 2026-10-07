import { useEffect, useMemo, useState } from "react";
import { API_URL, apiFetch } from "../../config/api";
import EntityImage from "../EntityImage/EntityImage";

function ExistingPlayerEnrollment({ equipoId, enrollmentId, currentPlayers, onSaved, onCancel }) {
  const [mode, setMode] = useState("CLUB");
  const [players, setPlayers] = useState([]);
  const [selected, setSelected] = useState(null);
  const [code, setCode] = useState("");
  const [number, setNumber] = useState("");
  const [position, setPosition] = useState("");
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

  const choose = (player) => {
    setSelected(player);
    setNumber(player.numeroCamiseta || "");
    setPosition(player.posicion || "");
    setError("");
  };

  const searchByCode = async () => {
    setError(""); setSelected(null);
    if (!code.trim()) return setError("Ingresá el código del jugador.");
    const response = await apiFetch(`${API_URL}/jugadores/codigo/${encodeURIComponent(code.trim())}`);
    const data = await response.json().catch(() => null);
    if (!response.ok) return setError(data?.message || "No encontramos ese jugador.");
    if (currentIds.has(data.idJugador)) return setError("Ese jugador ya está en el plantel de este torneo.");
    choose(data);
  };

  const submit = async (event) => {
    event.preventDefault();
    if (!selected) return setError("Seleccioná un jugador.");
    setSaving(true); setError("");
    try {
      const response = await apiFetch(`${API_URL}/inscripciones/${enrollmentId}/jugadores`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ idJugador: selected.idJugador, numeroCamiseta: Number(number), posicion: position }) });
      const payload = await response.json().catch(() => null);
      if (!response.ok) throw new Error(payload?.message || "No se pudo agregar el jugador");
      onSaved({ ...selected, ...payload, nombre: selected.nombre, apellido: selected.apellido });
    } catch (requestError) { setError(requestError.message); }
    finally { setSaving(false); }
  };

  return <form className="roster-player-form" onSubmit={submit}><h3>Agregar jugador existente</h3><p>Elegí un jugador del club o buscá otro perfil mediante su código.</p>
    <div className="existing-team-tabs"><button type="button" className={mode === "CLUB" ? "active" : ""} onClick={() => { setMode("CLUB"); setSelected(null); setError(""); }}>Jugadores del club</button><button type="button" className={mode === "CODE" ? "active" : ""} onClick={() => { setMode("CODE"); setSelected(null); setError(""); }}>Agregar por código</button></div>
    {mode === "CLUB" && <div className="existing-player-grid">{players.length === 0 ? <p>No quedan jugadores del club por agregar.</p> : players.map((player) => <button type="button" className={selected?.idJugador === player.idJugador ? "selected" : ""} key={player.idJugador} onClick={() => choose(player)}><span className="existing-player-avatar"><EntityImage type="player" value={player.foto} alt="" /></span><span><strong>{player.nombre} {player.apellido}</strong><small>{player.codigoPublico} · {player.posicion || "Sin posición"}</small></span></button>)}</div>}
    {mode === "CODE" && <><label>Código público del jugador<div className="team-code-search"><input value={code} onChange={(event) => setCode(event.target.value.toUpperCase())} placeholder="JUG-XXXXXXX" /><button type="button" onClick={searchByCode}>Buscar</button></div></label>{selected && <div className="team-code-result"><span className="existing-player-avatar"><EntityImage type="player" value={selected.foto} alt="" /></span><div><strong>{selected.nombre} {selected.apellido}</strong><small>{selected.nombreEquipo} · {selected.codigoPublico}</small></div><span>✓ Encontrado</span></div>}</>}
    {selected && <div className="tournament-form-grid"><label>Número para este torneo<input type="number" min="1" max="99" value={number} onChange={(event) => setNumber(event.target.value)} required /></label><label>Posición para este torneo<input value={position} onChange={(event) => setPosition(event.target.value)} placeholder="Arquero, defensor..." /></label></div>}
    {error && <p className="tournament-form-error">{error}</p>}<div className="tournament-form-actions"><button type="button" className="tournament-form-cancel" onClick={onCancel}>Cancelar</button><button className="tournament-form-save" disabled={saving || !selected || !number}>{saving ? "Agregando…" : "Agregar al plantel"}</button></div>
  </form>;
}
export default ExistingPlayerEnrollment;
