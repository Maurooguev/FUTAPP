import { useState } from "react";
import TeamFormModal from "./TeamFormModal";
import TeamRosterModal from "./TeamRosterModal";
import QuickTeamLoadWizard from "./QuickTeamLoadWizard";
import "./TeamManagement.css";
import { API_URL, apiFetch } from "../../config/api";
import { Link } from "react-router-dom";
import EntityImage from "../EntityImage/EntityImage";
import TeamWithdrawalModal from "./TeamWithdrawalModal";

function TeamManagement({ torneoId, capacidad, equipos, canManage, onTeamSaved, onTeamDeleted, onRosterChanged }) {
  const [editingTeam, setEditingTeam] = useState(null);
  const [showTeamForm, setShowTeamForm] = useState(false);
  const [rosterTeam, setRosterTeam] = useState(null);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [withdrawingTeam, setWithdrawingTeam] = useState(null);
  const [showQuickWizard, setShowQuickWizard] = useState(false);

  const openEditTeam = (team) => { setMessage(""); setEditingTeam(team); setShowTeamForm(true); };
  const removeTeam = async (team) => {
    if (!window.confirm(`¿Quitar a ${team.nombre} de este torneo? El perfil del club y sus otros torneos se conservarán.`)) return;
    setError(""); setMessage("");
    try {
      const response = await apiFetch(`${API_URL}/inscripciones/equipos/torneo/${torneoId}/equipo/${team.idEquipo}`, { method: "DELETE" });
      if (!response.ok) { const payload = await response.json().catch(() => null); throw new Error(payload?.message || "No se pudo quitar el equipo del torneo"); }
      onTeamDeleted(team.idEquipo);
      setMessage(`${team.nombre} se quitó correctamente del torneo.`);
    } catch (requestError) {
      if (requestError.message.toLowerCase().includes("partidos")) {
        setError("");
        setWithdrawingTeam(team);
      } else setError(requestError.message);
    }
  };

  return (
    <section className="team-management" id="equipos">
      <div className="team-management-head">
        <div><span>Participantes</span><h2>Equipos y planteles</h2><p>Administrá los clubes y sus jugadores.</p></div>
        {canManage && <div className="team-head-actions"><button type="button" className="quick" onClick={() => setShowQuickWizard(true)}>＋ Carga rápida</button></div>}
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
      {showTeamForm && <TeamFormModal torneoId={torneoId} equipo={editingTeam} onClose={() => setShowTeamForm(false)} onSaved={(savedTeam) => { onTeamSaved(savedTeam); setMessage(editingTeam ? "Los cambios del equipo se guardaron correctamente." : "El equipo se creó correctamente."); }} />}
      {rosterTeam && <TeamRosterModal equipo={rosterTeam} torneoId={torneoId} canManage={canManage} onRosterChanged={onRosterChanged} onClose={() => setRosterTeam(null)} />}
      {showQuickWizard && <QuickTeamLoadWizard torneoId={torneoId} capacidad={capacidad} equiposActuales={equipos} onClose={() => setShowQuickWizard(false)} onSaved={(payload) => { payload.equipos.forEach(onTeamSaved); setShowQuickWizard(false); setMessage(`Listo: ${payload.equipos.length} equipos y ${payload.jugadoresCreados} jugadores creados. Ahora podés personalizarlos.`); onRosterChanged?.(); }} />}
      {withdrawingTeam && <TeamWithdrawalModal torneoId={torneoId} team={withdrawingTeam} onClose={() => setWithdrawingTeam(null)} onSaved={(team) => { onTeamDeleted(team.idEquipo); setWithdrawingTeam(null); setMessage(`${team.nombre} quedó dado de baja. Su historial se conserva.`); }} />}
    </section>
  );
}

export default TeamManagement;
