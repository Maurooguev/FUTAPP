import { useState } from "react";
import TeamFormModal from "./TeamFormModal";
import TeamRosterModal from "./TeamRosterModal";
import "./TeamManagement.css";
import { API_URL, apiFetch } from "../../config/api";
import { Link } from "react-router-dom";
import ExistingTeamEnrollmentModal from "./ExistingTeamEnrollmentModal";
import EntityImage from "../EntityImage/EntityImage";

function TeamManagement({ torneoId, equipos, canManage, onTeamSaved, onTeamDeleted }) {
  const [editingTeam, setEditingTeam] = useState(null);
  const [showTeamForm, setShowTeamForm] = useState(false);
  const [rosterTeam, setRosterTeam] = useState(null);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [showExistingTeam, setShowExistingTeam] = useState(false);

  const openNewTeam = () => { setMessage(""); setEditingTeam(null); setShowTeamForm(true); };
  const openEditTeam = (team) => { setMessage(""); setEditingTeam(team); setShowTeamForm(true); };
  const removeTeam = async (team) => {
    if (!window.confirm(`¿Quitar a ${team.nombre} de este torneo? El perfil del club y sus otros torneos se conservarán.`)) return;
    setError(""); setMessage("");
    try {
      const response = await apiFetch(`${API_URL}/inscripciones/equipos/torneo/${torneoId}/equipo/${team.idEquipo}`, { method: "DELETE" });
      if (!response.ok) { const payload = await response.json().catch(() => null); throw new Error(payload?.message || "No se pudo quitar el equipo del torneo"); }
      onTeamDeleted(team.idEquipo);
      setMessage(`${team.nombre} se quitó correctamente del torneo.`);
    } catch (requestError) { setError(requestError.message); }
  };

  return (
    <section className="team-management" id="equipos">
      <div className="team-management-head">
        <div><span>Participantes</span><h2>Equipos y planteles</h2><p>Administrá los clubes y sus jugadores.</p></div>
        {canManage && <div className="team-head-actions"><button type="button" onClick={() => setShowExistingTeam(true)}>Inscribir club existente</button><button type="button" onClick={openNewTeam}>+ Crear club nuevo</button></div>}
      </div>
      {error && <p className="tournament-form-error">{error}</p>}
      {message && <p className="profile-action-message">{message}</p>}
      {equipos.length === 0 ? <div className="team-empty"><h3>Este torneo todavía no tiene equipos</h3><p>Agregá al menos dos para poder programar partidos.</p></div> : (
        <div className={`team-grid ${canManage ? "manageable" : ""}`}>{equipos.map((team) => (
          <article className="team-card" key={team.idEquipo}>
            <Link className="team-card-link" to={`/equipo/${team.idEquipo}`} aria-label={`Abrir perfil de ${team.nombre}`}>
              <span className="team-avatar">
                <EntityImage type="team" value={team.escudo} alt="" />
              </span>
              <span className="team-card-name">{team.nombre}</span>
              {team.verificado && <span className="team-verified" title="Equipo verificado" aria-label="Equipo verificado">✓</span>}
              <span className="team-card-arrow" aria-hidden="true">›</span>
            </Link>
            {canManage && <div className="team-card-actions" aria-label={`Acciones de ${team.nombre}`}>
              <button type="button" onClick={() => setRosterTeam(team)} title="Administrar plantel" aria-label={`Administrar plantel de ${team.nombre}`}>☷</button>
              <button type="button" className="team-edit" onClick={() => openEditTeam(team)} title="Editar equipo" aria-label={`Editar ${team.nombre}`}>✎</button>
              <button type="button" className="team-delete" onClick={() => removeTeam(team)} title="Quitar del torneo" aria-label={`Quitar a ${team.nombre} del torneo`}>×</button>
            </div>}
          </article>
        ))}</div>
      )}
      {showTeamForm && <TeamFormModal torneoId={torneoId} equipo={editingTeam} onClose={() => setShowTeamForm(false)} onSaved={(savedTeam) => { onTeamSaved(savedTeam); setMessage(editingTeam ? "Los cambios del equipo se guardaron correctamente." : "El equipo se creó correctamente."); }} onLinked={(officialTeam, duplicateId) => { onTeamDeleted(duplicateId); onTeamSaved(officialTeam); setMessage("Los equipos se vincularon correctamente."); }} />}
      {rosterTeam && <TeamRosterModal equipo={rosterTeam} torneoId={torneoId} canManage={canManage} onClose={() => setRosterTeam(null)} />}
      {showExistingTeam && <ExistingTeamEnrollmentModal torneoId={torneoId} currentTeams={equipos} onClose={() => setShowExistingTeam(false)} onSaved={(savedTeam) => { onTeamSaved(savedTeam); setMessage("El equipo se inscribió correctamente en el torneo."); }} />}
    </section>
  );
}

export default TeamManagement;
