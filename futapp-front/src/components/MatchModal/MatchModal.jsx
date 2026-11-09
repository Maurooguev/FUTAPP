import { useEffect, useState } from "react";
import "./MatchModal.css";
import { API_URL, apiFetch } from "../../config/api";
import MatchIncidentForm from "./MatchIncidentForm";
import { Link } from "react-router-dom";
import MatchMvpSelector from "./MatchMvpSelector";
import EntityImage from "../EntityImage/EntityImage";
import { formatMatchDate } from "../../utils/matchDate";

function MatchModal({ partido, onClose, canManage, onChanged }) {
  const [goles, setGoles] = useState([]);
  const [tarjetas, setTarjetas] = useState([]);
  const [formType, setFormType] = useState(null);
  const [error, setError] = useState("");
  const [currentMatch, setCurrentMatch] = useState(partido);
  const [activeSection, setActiveSection] = useState("goals");

  const deleteIncident = async (type, id) => {
    if (!window.confirm("¿Eliminar esta incidencia?")) return;
    setError("");
    try {
      const response = await apiFetch(`${API_URL}/${type}/${id}`, { method: "DELETE" });
      if (!response.ok) throw new Error("No se pudo eliminar la incidencia");
      if (type === "goles") setGoles((current) => current.filter((item) => item.idGol !== id));
      else setTarjetas((current) => current.filter((item) => item.idTarjeta !== id));
      onChanged?.();
    } catch (requestError) { setError(requestError.message); }
  };

  useEffect(() => {
    if (!partido) return;
    setCurrentMatch(partido);
    setActiveSection("goals");
    setFormType(null);

    apiFetch(`${API_URL}/goles/partido/${partido.idPartido}`)
      .then((res) => res.json())
      .then((data) => setGoles(data))
      .catch((err) => console.error("Goles partido:", err));

    apiFetch(`${API_URL}/tarjetas/partido/${partido.idPartido}`)
      .then((res) => res.json())
      .then((data) => setTarjetas(data))
      .catch((err) => console.error("Tarjetas partido:", err));
  }, [partido]);

  if (!partido) return null;
  const isFinalized = partido.estado?.toUpperCase() === "FINALIZADO";
  const golesLocalAsignados = goles.filter((gol) => gol.idEquipo === partido.idEquipoLocal).length;
  const golesVisitanteAsignados = goles.filter((gol) => gol.idEquipo === partido.idEquipoVisitante).length;
  const golesTotales = Number(partido.golesLocal || 0) + Number(partido.golesVisitante || 0);
  const golesAsignados = golesLocalAsignados + golesVisitanteAsignados;
  const golesCompletos = isFinalized && golesAsignados === golesTotales;
  const golesLocalPendientes = Math.max(0, Number(partido.golesLocal || 0) - golesLocalAsignados);
  const golesVisitantePendientes = Math.max(0, Number(partido.golesVisitante || 0) - golesVisitanteAsignados);
  const allowedGoalTeamIds = [
    ...(golesLocalPendientes > 0 ? [partido.idEquipoLocal] : []),
    ...(golesVisitantePendientes > 0 ? [partido.idEquipoVisitante] : []),
  ];

  return (
    <div className="match-modal-backdrop" onClick={onClose}>
      <div className="match-modal" onClick={(e) => e.stopPropagation()}>
        <button type="button" className="match-modal-close" aria-label="Cerrar detalle del partido" onClick={onClose}>
          ×
        </button>

        <div className="match-modal-header">
          <span>Paso 2 de 3 · Fecha {partido.numeroFecha}</span>
          <h2>
            <Link to={`/equipo/${partido.idEquipoLocal}`} onClick={onClose}>{partido.nombreEquipoLocal}</Link> vs <Link to={`/equipo/${partido.idEquipoVisitante}`} onClick={onClose}>{partido.nombreEquipoVisitante}</Link>
          </h2>
          <p>Completá goles, tarjetas y MVP · {formatMatchDate(partido.fechaHora)}{partido.cancha ? ` · ${partido.cancha}` : ""}</p>
        </div>

        <div className="match-score">
          <div>
            <EntityImage className="match-score-shield" type="team" value={partido.escudoEquipoLocal} alt="" />
            <h3><Link to={`/equipo/${partido.idEquipoLocal}`} onClick={onClose}>{partido.nombreEquipoLocal}</Link></h3>
            <strong>{partido.golesLocal}</strong>
            {isFinalized && <small>{golesLocalAsignados} asignados</small>}
          </div>

          <span>VS</span>

          <div>
            <EntityImage className="match-score-shield" type="team" value={partido.escudoEquipoVisitante} alt="" />
            <h3><Link to={`/equipo/${partido.idEquipoVisitante}`} onClick={onClose}>{partido.nombreEquipoVisitante}</Link></h3>
            <strong>{partido.golesVisitante}</strong>
            {isFinalized && <small>{golesVisitanteAsignados} asignados</small>}
          </div>
        </div>

        {isFinalized && <nav className="match-detail-tabs" aria-label="Detalle del partido">
          <button type="button" className={activeSection === "goals" ? "active" : ""} onClick={() => { setActiveSection("goals"); setFormType(null); }}><span>{golesCompletos ? "✓" : "1"}</span>Goles<small>{golesAsignados}/{golesTotales}</small></button>
          <button type="button" className={activeSection === "cards" ? "active" : ""} onClick={() => { setActiveSection("cards"); setFormType(null); }}><span>2</span>Tarjetas<small>{tarjetas.length} cargadas</small></button>
          <button type="button" className={activeSection === "mvp" ? "active" : ""} onClick={() => { setActiveSection("mvp"); setFormType(null); }}><span>{currentMatch?.idMvp ? "✓" : "3"}</span>MVP<small>{currentMatch?.idMvp ? "Elegido" : "Opcional"}</small></button>
          <button type="button" className={activeSection === "summary" ? "active" : ""} onClick={() => { setActiveSection("summary"); setFormType(null); }}><span>✓</span>Resumen<small>Finalizar</small></button>
        </nav>}

        {isFinalized && <div className="match-detail-content">
          {activeSection === "goals" && <section>
            <div className="match-section-title"><h3>Goles</h3>{isFinalized && <span className={golesCompletos ? "complete" : "pending"}>{golesAsignados}/{golesTotales} asignados</span>}</div>
            {!golesCompletos && <p className="match-section-help">Falta asignar {golesLocalPendientes} a {partido.nombreEquipoLocal} y {golesVisitantePendientes} a {partido.nombreEquipoVisitante}.</p>}
            {canManage && isFinalized && !formType && !golesCompletos && <button className="match-add-event" onClick={() => setFormType("goal")}>+ Asignar gol a un jugador</button>}
            {golesCompletos && <p className="match-assignment-ok">✓ El marcador coincide con los goles individuales.</p>}
            {formType === "goal" && <MatchIncidentForm partido={partido} type="goal" allowedTeamIds={allowedGoalTeamIds} onCancel={() => setFormType(null)} onSaved={(goal) => { setGoles((current) => { const updated = [...current, goal]; if (updated.length >= golesTotales) setFormType(null); return updated; }); onChanged?.(); }} />}

            {goles.length === 0 ? (
              <p className="match-empty">No hay goles cargados.</p>
            ) : (
              goles.map((gol) => (
                <div className="match-event" key={gol.idGol}>
                  <strong>{gol.minuto ? `${gol.minuto}'` : "–"}</strong>
                  <div>
                    <h4><Link to={`/jugador/${gol.idJugador}`} onClick={onClose}>{gol.nombreJugador}</Link></h4>
                    <p><Link to={`/equipo/${gol.idEquipo}`} onClick={onClose}>{gol.nombreEquipo}</Link> · {gol.tipoGol}</p>
                  </div>
                  {canManage && <button className="match-delete-event" onClick={() => deleteIncident("goles", gol.idGol)}>×</button>}
                </div>
              ))
            )}
            <div className="match-section-footer"><button type="button" onClick={() => { setFormType(null); setActiveSection("cards"); }}>{golesCompletos ? "Continuar a tarjetas" : "Omitir por ahora"} →</button></div>
          </section>}

          {activeSection === "cards" && <section>
            <div className="match-section-title"><h3>Tarjetas</h3><span className={tarjetas.length ? "complete" : "neutral"}>{tarjetas.length} cargadas</span></div>
            <p className="match-section-help">Son opcionales. Una roja genera automáticamente una fecha de suspensión, que después puede editarse.</p>
            {canManage && isFinalized && !formType && <button className="match-add-event" onClick={() => setFormType("card")}>+ Agregar tarjeta</button>}
            {formType === "card" && <MatchIncidentForm partido={partido} type="card" onCancel={() => setFormType(null)} onSaved={(card) => { setTarjetas((current) => [...current, card]); onChanged?.(); }} />}

            {tarjetas.length === 0 ? (
              <p className="match-empty">No hay tarjetas cargadas.</p>
            ) : (
              tarjetas.map((tarjeta) => (
                <div className="match-event" key={tarjeta.idTarjeta}>
                  <strong>{tarjeta.minuto ? `${tarjeta.minuto}'` : "-"}</strong>
                  <div>
                    <h4><Link to={`/jugador/${tarjeta.idJugador}`} onClick={onClose}>{tarjeta.nombreJugador}</Link></h4>
                    <p>
                      <Link to={`/equipo/${tarjeta.idEquipo}`} onClick={onClose}>{tarjeta.nombreEquipo}</Link> · {tarjeta.tipoTarjeta}
                      {tarjeta.motivo ? ` · ${tarjeta.motivo}` : ""}
                    </p>
                  </div>
                  {canManage && <button className="match-delete-event" onClick={() => deleteIncident("tarjetas", tarjeta.idTarjeta)}>×</button>}
                </div>
              ))
            )}
            <div className="match-section-footer"><button type="button" onClick={() => { setFormType(null); setActiveSection("mvp"); }}>Continuar a MVP →</button></div>
          </section>}

          {activeSection === "mvp" && <section><div className="match-section-title"><h3>Jugador destacado</h3><span className={currentMatch?.idMvp ? "complete" : "neutral"}>{currentMatch?.idMvp ? "Elegido" : "Opcional"}</span></div><p className="match-section-help">Elegí la figura del partido entre los jugadores inscriptos de ambos equipos.</p><div className="match-mvp-block">{currentMatch?.idMvp && <p>⭐ MVP actual: <Link to={`/jugador/${currentMatch.idMvp}`} onClick={onClose}>{currentMatch.nombreMvp}</Link></p>}{canManage && <MatchMvpSelector partido={currentMatch || partido} onSaved={(updated) => { setCurrentMatch(updated); onChanged?.(); }} />}</div><div className="match-section-footer"><button type="button" onClick={() => setActiveSection("summary")}>{currentMatch?.idMvp ? "Revisar resumen" : "Omitir y revisar resumen"} →</button></div></section>}

          {activeSection === "summary" && <section className="match-detail-summary"><div className="match-section-title"><h3>Resumen del partido</h3><span className="complete">Resultado guardado</span></div><div className="match-summary-score"><strong>{partido.nombreEquipoLocal}</strong><b>{partido.golesLocal} – {partido.golesVisitante}</b><strong>{partido.nombreEquipoVisitante}</strong></div><div className="match-summary-items"><p><span>Goles individuales</span><strong>{golesCompletos ? `${golesAsignados}/${golesTotales} completos` : `${golesAsignados}/${golesTotales} · se puede completar después`}</strong></p><p><span>Tarjetas</span><strong>{tarjetas.length ? `${tarjetas.length} cargadas` : "Sin tarjetas cargadas"}</strong></p><p><span>MVP</span><strong>{currentMatch?.nombreMvp || "Sin seleccionar"}</strong></p></div><button type="button" className="match-finish-button" onClick={onClose}>Terminar carga</button></section>}
        </div>}
        {canManage && !isFinalized && <p className="match-result-hint">{partido.estado?.toUpperCase() === "SUSPENDIDO" ? "Reprogramá el partido antes de cargar el resultado." : "Primero cargá el resultado del partido para habilitar goles, tarjetas y MVP."}</p>}
        {error && <p className="tournament-form-error">{error}</p>}
        <Link className="match-full-link" to={`/partido/${partido.idPartido}`} onClick={onClose}>Ver página completa del partido →</Link>
      </div>
    </div>
  );
}

export default MatchModal;
