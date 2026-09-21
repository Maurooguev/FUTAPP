import { useEffect, useMemo, useState } from "react";
import { API_URL, apiFetch, assetUrl } from "../../config/api";
import "../TournamentFormModal/TournamentFormModal.css";

function ExistingTeamEnrollmentModal({ torneoId, currentTeams, onClose, onSaved }) {
  const [mode, setMode] = useState("MINE");
  const [teams, setTeams] = useState([]);
  const [selected, setSelected] = useState(null);
  const [code, setCode] = useState("");
  const [category, setCategory] = useState("");
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [saving, setSaving] = useState(false);
  const currentIds = useMemo(() => new Set(currentTeams.map((team) => team.idEquipo)), [currentTeams]);

  useEffect(() => {
    apiFetch(`${API_URL}/equipos/creados`).then(async (response) => {
      const data = await response.json().catch(() => []);
      if (!response.ok) throw new Error(data?.message || "No se pudieron cargar tus equipos");
      setTeams(data.filter((team) => !currentIds.has(team.idEquipo)));
    }).catch((requestError) => setError(requestError.message));
  }, [currentIds]);

  const searchByCode = async () => {
    setError(""); setMessage(""); setSelected(null);
    if (!code.trim()) return setError("Ingresá el código del equipo.");
    const response = await fetch(`${API_URL}/equipos/codigo/${encodeURIComponent(code.trim())}`);
    const data = await response.json().catch(() => null);
    if (!response.ok) return setError(data?.message || "No encontramos ese equipo.");
    if (currentIds.has(data.idEquipo)) return setError("Ese equipo ya está inscripto en este torneo.");
    setSelected(data);
  };

  const submit = async (event) => {
    event.preventDefault();
    if (!selected) return setError("Seleccioná un equipo para continuar.");
    setSaving(true); setError(""); setMessage("");
    try {
      const response = await apiFetch(`${API_URL}/inscripciones/equipos`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ idEquipo: selected.idEquipo, idTorneo: Number(torneoId), categoria: category }) });
      const payload = await response.json().catch(() => null);
      if (response.status === 403) {
        const invitationResponse = await apiFetch(`${API_URL}/reclamos/equipos/${selected.idEquipo}/invitar`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            idTorneo: Number(torneoId),
            categoria: category,
            mensaje: `Invitación para participar en el torneo como ${category.trim() || "equipo participante"}.`,
          }),
        });
        const invitation = await invitationResponse.json().catch(() => null);
        if (!invitationResponse.ok) throw new Error(invitation?.message || "No se pudo enviar la invitación");
        setMessage(`Invitación enviada a los responsables de ${selected.nombre}. El equipo aparecerá en el torneo cuando la acepten.`);
        setSelected(null); setCode("");
        window.dispatchEvent(new Event("requests-updated"));
        return;
      }
      if (!response.ok) throw new Error(payload?.message || "No se pudo inscribir el equipo");
      onSaved(selected); onClose();
    } catch (requestError) { setError(requestError.message); }
    finally { setSaving(false); }
  };

  return <div className="tournament-form-backdrop" onMouseDown={onClose}><section className="tournament-form-modal existing-team-modal" onMouseDown={(event) => event.stopPropagation()}><button type="button" className="tournament-form-close" onClick={onClose}>×</button><span className="tournament-form-chip">Inscripción</span><h2>Agregar equipo existente</h2><p>Reutilizá un equipo que ya creaste o ingresá el código de otro club.</p>
    <div className="existing-team-tabs"><button type="button" className={mode === "MINE" ? "active" : ""} onClick={() => { setMode("MINE"); setSelected(null); setError(""); }}>Mis equipos creados</button><button type="button" className={mode === "CODE" ? "active" : ""} onClick={() => { setMode("CODE"); setSelected(null); setError(""); }}>Agregar por código</button></div>
    <form onSubmit={submit}>
      {mode === "MINE" && <div className="existing-team-list">{teams.length === 0 ? <div className="team-empty"><strong>No tenés otros equipos creados</strong><p>Podés buscarlos por código o crear uno nuevo.</p></div> : teams.map((team) => <button type="button" key={team.idEquipo} className={selected?.idEquipo === team.idEquipo ? "selected" : ""} onClick={() => setSelected(team)}><span className="existing-team-logo">{team.escudo ? <img src={assetUrl(team.escudo)} alt="" /> : team.nombre.slice(0, 2).toUpperCase()}</span><span><strong>{team.nombre}</strong><small>{team.barrio || "Sin barrio"} · {team.codigoPublico}</small></span><em>{selected?.idEquipo === team.idEquipo ? "Seleccionado" : "Elegir"}</em></button>)}</div>}
      {mode === "CODE" && <><label>Código público del equipo<div className="team-code-search"><input value={code} onChange={(event) => setCode(event.target.value.toUpperCase())} placeholder="EQ-XXXXXXX" /><button type="button" onClick={searchByCode}>Buscar</button></div></label>{selected && <div className="team-code-result"><span className="existing-team-logo">{selected.escudo ? <img src={assetUrl(selected.escudo)} alt="" /> : selected.nombre.slice(0, 2).toUpperCase()}</span><div><strong>{selected.nombre}</strong><small>{selected.barrio || "Sin barrio"} · {selected.codigoPublico}</small></div><span>✓ Encontrado</span></div>}</>}
      {selected && <label>Categoría (opcional)<input value={category} onChange={(event) => setCategory(event.target.value)} placeholder="Primera, Senior, Sub 18..." /></label>}
      {message && <p className="profile-action-message">{message}</p>}
      {error && <p className="tournament-form-error">{error}</p>}
      <div className="tournament-form-actions"><button type="button" className="tournament-form-cancel" onClick={onClose}>Cancelar</button><button className="tournament-form-save" disabled={saving || !selected}>{saving ? "Inscribiendo…" : "Inscribir equipo"}</button></div>
    </form>
  </section></div>;
}
export default ExistingTeamEnrollmentModal;
