import { useState } from "react";
import { API_URL, apiFetch } from "../../config/api";
import "../TournamentFormModal/TournamentFormModal.css";

function TeamMergeModal({ equipos, onClose, onMerged }) {
  const [officialId, setOfficialId] = useState("");
  const [duplicateId, setDuplicateId] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const official = equipos.find((team) => team.idEquipo === Number(officialId));
  const duplicate = equipos.find((team) => team.idEquipo === Number(duplicateId));

  const submit = async (event) => {
    event.preventDefault(); setError("");
    if (officialId === duplicateId) return setError("Seleccioná dos clubes diferentes.");
    if (!window.confirm(`¿Fusionar ${duplicate?.nombre} dentro de ${official?.nombre}? El perfil ${duplicate?.codigoPublico} dejará de existir.`)) return;
    setSaving(true);
    try {
      const response = await apiFetch(`${API_URL}/equipos/${officialId}/fusionar/${duplicateId}`, { method: "POST" });
      if (!response.ok) { const payload = await response.json().catch(() => null); throw new Error(payload?.message || "No se pudieron fusionar los clubes"); }
      onMerged(Number(officialId), Number(duplicateId)); onClose();
    } catch (requestError) { setError(requestError.message); } finally { setSaving(false); }
  };

  return <div className="tournament-form-backdrop" onMouseDown={onClose}><section className="tournament-form-modal team-merge-modal" role="dialog" aria-modal="true" onMouseDown={(event) => event.stopPropagation()}>
    <button type="button" className="tournament-form-close" onClick={onClose}>×</button><span className="tournament-form-chip">Unificar identidad</span><h2>Fusionar clubes duplicados</h2><p>Elegí el perfil oficial que querés conservar. El otro perfil desaparecerá, pero sus torneos, jugadores, partidos y estadísticas pasarán al oficial.</p>
    <form onSubmit={submit}><label>Perfil oficial<select value={officialId} onChange={(event) => setOfficialId(event.target.value)} required><option value="">Seleccionar club</option>{equipos.map((team) => <option key={team.idEquipo} value={team.idEquipo}>{team.nombre} · {team.codigoPublico}</option>)}</select></label>
      <label>Perfil duplicado<select value={duplicateId} onChange={(event) => setDuplicateId(event.target.value)} required><option value="">Seleccionar club</option>{equipos.filter((team) => team.idEquipo !== Number(officialId)).map((team) => <option key={team.idEquipo} value={team.idEquipo}>{team.nombre} · {team.codigoPublico}</option>)}</select></label>
      {official && duplicate && <div className="team-merge-summary"><strong>Se conserva</strong><span>{official.nombre} · {official.codigoPublico}</span><small>Se incorporará todo el historial de {duplicate.nombre}.</small></div>}
      <p className="wizard-draft-note">Por seguridad, si ambos perfiles participaron en el mismo torneo la fusión se bloqueará para revisión manual.</p>{error && <p className="tournament-form-error">{error}</p>}
      <div className="tournament-form-actions"><button type="button" className="tournament-form-cancel" onClick={onClose}>Cancelar</button><button className="tournament-form-save" disabled={saving || !officialId || !duplicateId}>{saving ? "Fusionando…" : "Fusionar clubes"}</button></div>
    </form>
  </section></div>;
}

export default TeamMergeModal;
