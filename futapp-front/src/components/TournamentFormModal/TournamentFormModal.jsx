import { useEffect, useState } from "react";
import { API_URL, apiFetch } from "../../config/api";
import ArgentinaLocationInput from "./ArgentinaLocationInput";
import "./TournamentFormModal.css";

const EMPTY_FORM = { nombre: "", descripcion: "", ubicacion: "", direccionSede: "", googleMapsUrl: "", reglasTexto: "", premiosTexto: "", estado: "BORRADOR", formato: "Liga", cantidadEquipos: 8, cantidadZonas: 2, clasificadosPorZona: 2, generarEquiposPrueba: true, jugadoresPorEquipoPrueba: 5, idaVuelta: false, puntosVictoria: 3, puntosEmpate: 1, puntosDerrota: 0, criterioDesempate: "DIFERENCIA_GOLES" };
const STEPS = ["Datos", "Formato", "Reglas"];
const FORMATS = [
  { value: "Liga", icon: "🏆", title: "Liga", detail: "Todos contra todos" },
  { value: "Eliminación directa", icon: "⚡", title: "Eliminación directa", detail: "Llaves desde el inicio" },
  { value: "Liga + Playoffs", icon: "🏁", title: "Liga + Playoffs", detail: "Tabla y fase final" },
  { value: "Zonas + Playoffs", icon: "🌐", title: "Zonas + Playoffs", detail: "Zonas y fase final" },
];

function TournamentFormModal({ torneo, onClose, onSaved }) {
  const [form, setForm] = useState(EMPTY_FORM);
  const [step, setStep] = useState(1);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [locationConfirmed, setLocationConfirmed] = useState(false);
  const isEditing = Boolean(torneo?.idTorneo);
  const isDirectElimination = form.formato === "Eliminación directa";
  const teamCount = Math.max(2, Number(form.cantidadEquipos) || 2);
  const estimatedRounds = (teamCount % 2 === 0 ? teamCount - 1 : teamCount) * (form.idaVuelta ? 2 : 1);
  useEffect(() => {
    setForm(torneo ? { ...EMPTY_FORM, ...torneo, idaVuelta: Boolean(torneo.idaVuelta) } : EMPTY_FORM);
    setLocationConfirmed(Boolean(torneo?.ubicacion));
    setStep(1);
  }, [torneo]);
  const updateField = ({ target: { name, value, type, checked } }) => setForm((current) => ({ ...current, [name]: type === "checkbox" ? checked : value }));
  const next = () => {
    setError("");
    if (step === 1 && (!form.nombre.trim() || !form.ubicacion.trim() || !form.descripcion.trim())) return setError("Completá el nombre, la ubicación y la descripción.");
    if (step === 1 && !locationConfirmed) return setError("Elegí una ciudad o barrio de las sugerencias oficiales.");
    if (step === 2 && Number(form.cantidadEquipos) < 2) return setError("El torneo necesita al menos 2 equipos.");
    if (step === 2 && form.formato === "Zonas + Playoffs" && Number(form.cantidadZonas) > Number(form.cantidadEquipos)) return setError("No puede haber más zonas que equipos.");
    if (step === 2 && form.formato === "Zonas + Playoffs" && Number(form.cantidadZonas) * Number(form.clasificadosPorZona) > Number(form.cantidadEquipos)) return setError("Los clasificados no pueden superar la cantidad de equipos.");
    setStep((current) => Math.min(3, current + 1));
  };
  const handleSubmit = async (event) => {
    event.preventDefault();
    if (step < 3) return next();
    setSaving(true); setError("");
    try {
      const response = await apiFetch(isEditing ? `${API_URL}/torneos/${torneo.idTorneo}` : `${API_URL}/torneos`, {
        method: isEditing ? "PUT" : "POST", headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ...form, cantidadEquipos: Number(form.cantidadEquipos), cantidadZonas: Number(form.cantidadZonas), clasificadosPorZona: Number(form.clasificadosPorZona), puntosVictoria: isDirectElimination ? 0 : Number(form.puntosVictoria), puntosEmpate: isDirectElimination ? 0 : Number(form.puntosEmpate), puntosDerrota: isDirectElimination ? 0 : Number(form.puntosDerrota), criterioDesempate: isDirectElimination ? "PENALES" : form.criterioDesempate }),
      });
      const saved = await response.json().catch(() => null);
      if (!response.ok) throw new Error(saved?.message || "No se pudo guardar el torneo");
      onSaved(saved); onClose();
    } catch (requestError) { setError(requestError.message); }
    finally { setSaving(false); }
  };
  return <div className="tournament-form-backdrop" onMouseDown={onClose}><section className="tournament-form-modal tournament-wizard" role="dialog" aria-modal="true" onMouseDown={(event) => event.stopPropagation()}>
    <button type="button" className="tournament-form-close" aria-label="Cerrar" onClick={onClose}>×</button>
    <span className="tournament-form-chip">{isEditing ? "Configuración del torneo" : "Nuevo torneo"}</span>
    <h2>{isEditing ? "Editar torneo" : "Creá tu torneo"}</h2>
    <div className="wizard-progress">{STEPS.map((label, index) => <button type="button" key={label} className={step === index + 1 ? "active" : step > index + 1 ? "done" : ""} onClick={() => index + 1 < step && setStep(index + 1)}><span>{step > index + 1 ? "✓" : index + 1}</span>{label}</button>)}</div>
    <form onSubmit={handleSubmit}>
      {step === 1 && <div className="wizard-step"><div><h3>Información básica</h3><p>Lo que verán los equipos cuando encuentren tu torneo.</p></div><label>Nombre del torneo<input autoFocus name="nombre" value={form.nombre} onChange={updateField} maxLength="120" placeholder="Ej. Liga Apertura 2026" required /></label><div className="tournament-form-grid"><label>Ciudad o barrio<ArgentinaLocationInput value={form.ubicacion} onChange={(value) => { setForm((current) => ({ ...current, ubicacion: value })); setLocationConfirmed(false); }} onSelect={(value) => { setForm((current) => ({ ...current, ubicacion: value })); setLocationConfirmed(true); setError(""); }} required /></label>{isEditing && <label>Estado actual<input value={form.estado} disabled /></label>}</div><div className="tournament-form-grid"><label>Complejo o predio (cancha)<input name="direccionSede" value={form.direccionSede} onChange={updateField} maxLength="200" placeholder="Ej. Complejo Deportivo Paso del Rey" /></label><label>Enlace de Google Maps<input type="url" name="googleMapsUrl" value={form.googleMapsUrl} onChange={updateField} maxLength="1000" placeholder="https://maps.app.goo.gl/..." /></label></div><small className="maps-field-help">Abrí el predio en Google Maps, tocá Compartir y pegá acá el enlace.</small><label>Descripción<textarea name="descripcion" value={form.descripcion} onChange={updateField} maxLength="500" rows="3" placeholder="Contá brevemente de qué se trata la competencia" required /></label></div>}
      {step === 2 && <div className="wizard-step"><div><h3>¿Cómo se va a jugar?</h3><p>Elegí una estructura inicial. FUTAPP prepara las fases y los lugares para los equipos.</p></div><div className="format-options">{FORMATS.map((option) => <button key={option.value} type="button" className={`format-card ${form.formato === option.value ? "selected" : ""}`} onClick={() => setForm((current) => ({ ...current, formato: option.value, idaVuelta: option.value === "Eliminación directa" ? false : current.idaVuelta }))}><span>{option.icon}</span><strong>{option.title}</strong><small>{option.detail}</small><em>Elegir</em></button>)}</div><div className="tournament-form-grid"><label>Cantidad de equipos<input type="number" name="cantidadEquipos" value={form.cantidadEquipos} onChange={updateField} min="2" max="100" required /></label>{form.formato !== "Eliminación directa" && <label className="wizard-toggle"><input type="checkbox" name="idaVuelta" checked={form.idaVuelta} onChange={updateField} /><span><strong>Partidos de ida y vuelta</strong><small>Dos partidos contra cada rival en la fase de liga.</small></span></label>}</div>{form.formato === "Zonas + Playoffs" && <div className="tournament-form-grid"><label>Cantidad de zonas<input type="number" name="cantidadZonas" value={form.cantidadZonas} onChange={updateField} min="2" max="16" /></label><label>Clasificados por zona<input type="number" name="clasificadosPorZona" value={form.clasificadosPorZona} onChange={updateField} min="1" max="16" /></label></div>}<label className="wizard-toggle test-team-toggle"><input type="checkbox" name="generarEquiposPrueba" checked={form.generarEquiposPrueba} onChange={updateField} /><span><strong>Generar equipos de prueba</strong><small>Crea automáticamente Equipo 1, Equipo 2 y los demás para poder probar el torneo enseguida.</small></span></label>{form.generarEquiposPrueba && <label>Jugadores de prueba por equipo<input type="number" name="jugadoresPorEquipoPrueba" value={form.jugadoresPorEquipoPrueba} onChange={updateField} min="0" max="30" /></label>}<div className="fixture-estimate"><strong>{form.formato === "Liga" ? `${estimatedRounds} jornadas estimadas` : form.formato}</strong><span>{form.generarEquiposPrueba ? `Se crearán automáticamente ${teamCount} equipos editables con sus planteles.` : `Quedarán preparados ${teamCount} cupos vacíos.`}</span></div></div>}
      {step === 3 && <div className="wizard-step"><div><h3>{isDirectElimination ? "Reglas de los partidos" : "Reglas de la tabla"}</h3><p>{isDirectElimination ? "Cada llave debe tener un ganador. Si terminan empatados, se define por penales." : "Usamos la configuración más común. Cambiala solo si tu torneo lo necesita."}</p></div>{isDirectElimination ? <div className="knockout-rule"><span>⚽</span><div><strong>Empate: definición por penales</strong><small>No se asignan puntos por victoria, empate o derrota.</small></div></div> : <><div className="points-grid"><label>Victoria<input type="number" min="0" max="10" name="puntosVictoria" value={form.puntosVictoria} onChange={updateField} /></label><label>Empate<input type="number" min="0" max="10" name="puntosEmpate" value={form.puntosEmpate} onChange={updateField} /></label><label>Derrota<input type="number" min="0" max="10" name="puntosDerrota" value={form.puntosDerrota} onChange={updateField} /></label></div><label>Primer criterio de desempate<select name="criterioDesempate" value={form.criterioDesempate} onChange={updateField}><option value="DIFERENCIA_GOLES">Diferencia de goles</option><option value="GOLES_FAVOR">Goles a favor</option></select></label></>}<label>Reglamento completo<textarea name="reglasTexto" value={form.reglasTexto} onChange={updateField} maxLength="5000" rows="5" placeholder="Detallá reglas de inscripción, conducta, suspensiones, horarios, indumentaria y cualquier condición del torneo." /></label><label>Premios<textarea name="premiosTexto" value={form.premiosTexto} onChange={updateField} maxLength="3000" rows="3" placeholder="Ej. Trofeo y medallas para el campeón, premio al goleador y valla menos vencida." /></label><div className="wizard-summary"><div><span>Torneo</span><strong>{form.nombre || "Sin nombre"}</strong></div><div><span>Formato</span><strong>{form.formato}{!isDirectElimination ? ` · ${form.idaVuelta ? "ida y vuelta" : "una rueda"}` : ""}</strong></div><div><span>Equipos</span><strong>{form.cantidadEquipos}</strong></div><div><span>{isDirectElimination ? "Definición" : "Puntos"}</span><strong>{isDirectElimination ? "Penales en caso de empate" : `${form.puntosVictoria} / ${form.puntosEmpate} / ${form.puntosDerrota}`}</strong></div></div>{!isEditing && <p className="wizard-draft-note">Se creará como borrador con sus fases y cupos preparados. Luego reemplazás cada lugar con un equipo real.</p>}</div>}
      {error && <p className="tournament-form-error">{error}</p>}
      <div className="tournament-form-actions"><button type="button" className="tournament-form-cancel" onClick={step === 1 ? onClose : () => { setError(""); setStep((current) => current - 1); }}>{step === 1 ? "Cancelar" : "Atrás"}</button><button type="submit" className="tournament-form-save" disabled={saving}>{saving ? "Guardando…" : step < 3 ? "Continuar" : isEditing ? "Guardar cambios" : "Crear torneo"}</button></div>
    </form>
  </section></div>;
}
export default TournamentFormModal;
