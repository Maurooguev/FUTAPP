import { useEffect, useState } from "react";
import { API_URL, apiFetch } from "../../config/api";

function MatchMvpSelector({ partido, onSaved }) {
  const [players, setPlayers] = useState([]);
  const [selected, setSelected] = useState(partido.idMvp ?? "");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadRoster = async (teamId) => {
      const history = await fetch(`${API_URL}/inscripciones/equipos/${teamId}`).then((response) => response.json());
      const enrollment = history.find((item) => Number(item.idTorneo) === Number(partido.idTorneo) && item.estado === "ACTIVA");
      if (!enrollment) return [];
      return fetch(`${API_URL}/inscripciones/${enrollment.idInscripcion}/jugadores`).then((response) => response.json());
    };
    Promise.all([loadRoster(partido.idEquipoLocal), loadRoster(partido.idEquipoVisitante)])
      .then(([local, visitor]) => setPlayers([...local, ...visitor].filter((player) => player.estado === "ACTIVA")));
  }, [partido.idEquipoLocal, partido.idEquipoVisitante, partido.idTorneo]);

  const save = async () => {
    setSaving(true); setError("");
    try {
      const response = await apiFetch(`${API_URL}/partidos/${partido.idPartido}/mvp`, {
        method: "PUT", headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ idJugador: selected ? Number(selected) : null }),
      });
      const payload = await response.json().catch(() => null);
      if (!response.ok) throw new Error(payload?.message || "No se pudo guardar el MVP");
      onSaved(payload);
    } catch (requestError) { setError(requestError.message); } finally { setSaving(false); }
  };

  return <div className="match-mvp-selector"><div><strong>Jugador destacado (MVP)</strong><small>Opcional · elegido por el organizador</small></div><select value={selected} onChange={(event) => setSelected(event.target.value)}><option value="">Sin seleccionar</option>{players.map((player) => <option key={player.idInscripcionJugador} value={player.idJugador}>{player.nombreJugador} — {player.nombreEquipo}</option>)}</select><button onClick={save} disabled={saving}>{saving ? "Guardando…" : "Guardar MVP"}</button>{error && <p className="tournament-form-error">{error}</p>}</div>;
}

export default MatchMvpSelector;
