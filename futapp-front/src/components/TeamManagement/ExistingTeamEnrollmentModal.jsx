import { useEffect, useState } from "react";
import { API_URL, apiFetch } from "../../config/api";
import "../TournamentFormModal/TournamentFormModal.css";

function ExistingTeamEnrollmentModal({ torneoId, currentTeams, onClose, onSaved }) {
  const [teams, setTeams] = useState([]);
  const [teamId, setTeamId] = useState("");
  const [category, setCategory] = useState("");
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    fetch(`${API_URL}/equipos`).then((res) => res.json()).then((items) => {
      const currentIds = new Set(currentTeams.map((team) => team.idEquipo));
      setTeams(Array.isArray(items) ? items.filter((team) => !currentIds.has(team.idEquipo)) : []);
    }).catch(() => setError("No se pudieron cargar los clubes"));
  }, [currentTeams]);

  const submit = async (event) => {
    event.preventDefault(); setSaving(true); setError("");
    try {
      const response = await apiFetch(`${API_URL}/inscripciones/equipos`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ idEquipo: Number(teamId), idTorneo: Number(torneoId), categoria: category }) });
      const payload = await response.json().catch(() => null);
      if (!response.ok) throw new Error(payload?.message || "No se pudo inscribir el club");
      const teamResponse = await fetch(`${API_URL}/equipos/${teamId}`);
      onSaved(await teamResponse.json()); onClose();
    } catch (requestError) { setError(requestError.message); } finally { setSaving(false); }
  };

  return <div className="tournament-form-backdrop" onMouseDown={onClose}><section className="tournament-form-modal" onMouseDown={(e) => e.stopPropagation()}><button type="button" className="tournament-form-close" onClick={onClose}>×</button><span className="tournament-form-chip">Inscripción</span><h2>Agregar club existente</h2><p>El club conservará su perfil y tendrá un plantel propio para este torneo.</p><form onSubmit={submit}><label>Club<select value={teamId} onChange={(e) => setTeamId(e.target.value)} required><option value="">Seleccionar club</option>{teams.map((team) => <option key={team.idEquipo} value={team.idEquipo}>{team.nombre} · {team.barrio}</option>)}</select></label><label>Categoría (opcional)<input value={category} onChange={(e) => setCategory(e.target.value)} placeholder="Primera, Senior, Sub 18..." /></label>{teams.length === 0 && <p className="tournament-form-error">No hay otros clubes disponibles.</p>}{error && <p className="tournament-form-error">{error}</p>}<div className="tournament-form-actions"><button type="button" className="tournament-form-cancel" onClick={onClose}>Cancelar</button><button className="tournament-form-save" disabled={saving || !teamId}>{saving ? "Inscribiendo..." : "Inscribir club"}</button></div></form></section></div>;
}

export default ExistingTeamEnrollmentModal;
