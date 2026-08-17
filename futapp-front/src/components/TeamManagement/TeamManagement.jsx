import { useState } from "react";
import TeamFormModal from "./TeamFormModal";
import TeamRosterModal from "./TeamRosterModal";
import "./TeamManagement.css";
import { API_URL, apiFetch } from "../../config/api";

function TeamManagement({ torneoId, equipos, canManage, onTeamSaved, onTeamDeleted }) {
  const [editingTeam, setEditingTeam] = useState(null);
  const [showTeamForm, setShowTeamForm] = useState(false);
  const [rosterTeam, setRosterTeam] = useState(null);
  const [error, setError] = useState("");

  const openNewTeam = () => { setEditingTeam(null); setShowTeamForm(true); };
  const openEditTeam = (team) => { setEditingTeam(team); setShowTeamForm(true); };
  const deleteTeam = async (team) => {
    if (!window.confirm(`¿Eliminar el equipo ${team.nombre}?`)) return;
    setError("");
    try {
      const response = await apiFetch(`${API_URL}/equipos/${team.idEquipo}`, { method: "DELETE" });
      if (!response.ok) { const payload = await response.json().catch(() => null); throw new Error(payload?.message || "No se pudo eliminar el equipo"); }
      onTeamDeleted(team.idEquipo);
    } catch (requestError) { setError(requestError.message); }
  };

  return (
    <section className="team-management">
      <div className="team-management-head">
        <div><span>Participantes</span><h2>Equipos y planteles</h2><p>Administrá los clubes y sus jugadores.</p></div>
        {canManage && <button type="button" onClick={openNewTeam}>+ Agregar equipo</button>}
      </div>
      {error && <p className="tournament-form-error">{error}</p>}
      {equipos.length === 0 ? <div className="team-empty"><h3>Este torneo todavía no tiene equipos</h3><p>Agregá al menos dos para poder programar partidos.</p></div> : (
        <div className="team-grid">{equipos.map((team) => (
          <article className="team-card" key={team.idEquipo}>
            <div className="team-avatar">{team.nombre.slice(0, 2).toUpperCase()}</div>
            <div className="team-card-copy"><h3>{team.nombre}</h3><p>{team.barrio} · {team.colorCamiseta}</p><small>Responsable: {team.responsable}</small></div>
            <div className="team-card-actions"><button type="button" onClick={() => setRosterTeam(team)}>Ver plantel</button>{canManage && <><button type="button" className="team-edit" onClick={() => openEditTeam(team)}>Editar</button><button type="button" className="team-delete" onClick={() => deleteTeam(team)}>Eliminar</button></>}</div>
          </article>
        ))}</div>
      )}
      {showTeamForm && <TeamFormModal torneoId={torneoId} equipo={editingTeam} onClose={() => setShowTeamForm(false)} onSaved={onTeamSaved} />}
      {rosterTeam && <TeamRosterModal equipo={rosterTeam} canManage={canManage} onClose={() => setRosterTeam(null)} />}
    </section>
  );
}

export default TeamManagement;
