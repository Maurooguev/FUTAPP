import { useMemo, useState } from "react";
import { API_URL, apiFetch } from "../../config/api";
import "../TournamentFormModal/TournamentFormModal.css";

const nextSaturday = () => {
  const date = new Date();
  const days = (6 - date.getDay() + 7) % 7;
  date.setDate(date.getDate() + days);
  return date.toISOString().slice(0, 10);
};

function FixtureGeneratorModal({ torneo, equipos, fechas, onClose, onGenerated }) {
  const [form, setForm] = useState({ fechaInicio: nextSaturday(), horaInicio: "15:00", diasEntreJornadas: 7, minutosEntrePartidos: 90 });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const summary = useMemo(() => {
    const adjustedTeams = equipos.length % 2 === 0 ? equipos.length : equipos.length + 1;
    const rounds = Math.max(0, adjustedTeams - 1) * (torneo.idaVuelta ? 2 : 1);
    return { rounds, matches: (equipos.length * (equipos.length - 1) / 2) * (torneo.idaVuelta ? 2 : 1) };
  }, [equipos.length, torneo.idaVuelta]);

  const submit = async (event) => {
    event.preventDefault(); setSaving(true); setError("");
    try {
      const response = await apiFetch(`${API_URL}/partidos/torneo/${torneo.idTorneo}/generar-fixture`, {
        method: "POST", headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ...form, diasEntreJornadas: Number(form.diasEntreJornadas), minutosEntrePartidos: Number(form.minutosEntrePartidos) }),
      });
      const payload = await response.json().catch(() => null);
      if (!response.ok) throw new Error(payload?.message || "No se pudo generar el fixture");
      onGenerated(payload); onClose();
    } catch (requestError) { setError(requestError.message); } finally { setSaving(false); }
  };

  return <div className="tournament-form-backdrop" onMouseDown={onClose}><section className="tournament-form-modal" role="dialog" aria-modal="true" onMouseDown={(event) => event.stopPropagation()}>
    <button type="button" className="tournament-form-close" onClick={onClose}>×</button>
    <span className="tournament-form-chip">Programación automática</span><h2>Generar fixture</h2><p>Definí el calendario base. Después podrás editar cada partido individualmente.</p>
    <form onSubmit={submit}><div className="tournament-form-grid">
      <label>Primera jornada<input type="date" value={form.fechaInicio} onChange={(e) => setForm({ ...form, fechaInicio: e.target.value })} required /></label>
      <label>Primer horario<input type="time" value={form.horaInicio} onChange={(e) => setForm({ ...form, horaInicio: e.target.value })} required /></label>
      <label>Días entre jornadas<input type="number" min="1" max="60" value={form.diasEntreJornadas} onChange={(e) => setForm({ ...form, diasEntreJornadas: e.target.value })} required /></label>
      <label>Minutos entre partidos<input type="number" min="0" max="720" step="5" value={form.minutosEntrePartidos} onChange={(e) => setForm({ ...form, minutosEntrePartidos: e.target.value })} required /></label>
    </div>
    <div className="wizard-summary"><div><span>Equipos</span><strong>{equipos.length}</strong></div><div><span>Modalidad</span><strong>{torneo.idaVuelta ? "Ida y vuelta" : "Una rueda"}</strong></div><div><span>Jornadas</span><strong>{summary.rounds}</strong></div><div><span>Partidos</span><strong>{summary.matches}</strong></div></div>
    {fechas.length > 0 && <p className="wizard-draft-note">Las primeras {Math.min(fechas.length, summary.rounds)} jornadas existentes se reutilizarán.</p>}
    {error && <p className="tournament-form-error">{error}</p>}
    <div className="tournament-form-actions"><button type="button" className="tournament-form-cancel" onClick={onClose}>Cancelar</button><button className="tournament-form-save" disabled={saving}>{saving ? "Generando…" : `Generar ${summary.matches} partidos`}</button></div>
    </form>
  </section></div>;
}

export default FixtureGeneratorModal;
