import { useEffect, useState } from "react";
import { API_URL, apiFetch } from "../../config/api";
import ArgentinaLocationInput from "./ArgentinaLocationInput";
import "./TournamentFormModal.css";

const EMPTY_FORM = { nombre: "", descripcion: "", ubicacion: "", direccionSede: "", googleMapsUrl: "", reglasTexto: "", premiosTexto: "", estado: "BORRADOR", formato: "Liga", cantidadEquipos: 8, cantidadZonas: 2, clasificadosPorZona: 2, clasificadosPlayoff: 4, generarEquiposPrueba: false, jugadoresPorEquipoPrueba: 0, idaVuelta: false, puntosVictoria: 3, puntosEmpate: 1, puntosDerrota: 0, criterioDesempate: "DIFERENCIA_GOLES" };
const STEPS = ["Datos", "Formato", "Configuración", "Revisar"];
const DRAFT_KEY = "futapp:tournament-draft";
const FORMATS = [
  { value: "Liga", icon: "🏆", title: "Liga", detail: "Todos contra todos. El campeón se define por la tabla." },
  { value: "Liga + Playoffs", icon: "🏁", title: "Liga + Playoffs", detail: "Primero una tabla y después una llave entre los mejores." },
  { value: "Zonas + Playoffs", icon: "🌐", title: "Zonas + Playoffs", detail: "Grupos separados y una fase eliminatoria final." },
  { value: "Eliminación directa", icon: "⚡", title: "Eliminación directa", detail: "El que pierde queda eliminado desde el inicio." },
];
const COMMON_TEAM_COUNTS = [4, 6, 8, 10, 12, 16];
const BRACKET_COUNTS = [2, 4, 8, 16];
const isPowerOfTwo = (value) => value >= 2 && (value & (value - 1)) === 0;
const bracketRoundName = (teams) => teams === 2 ? "final" : teams === 4 ? "semifinales y final" : teams === 8 ? "cuartos, semifinales y final" : "octavos, cuartos, semifinales y final";

function TournamentFormModal({ torneo, onClose, onSaved }) {
  const [form, setForm] = useState(EMPTY_FORM);
  const [step, setStep] = useState(1);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [locationConfirmed, setLocationConfirmed] = useState(false);
  const [draftRestored, setDraftRestored] = useState(false);
  const [showAdvancedRules, setShowAdvancedRules] = useState(false);
  const [showPublicInfo, setShowPublicInfo] = useState(false);
  const isEditing = Boolean(torneo?.idTorneo);
  const isDirectElimination = form.formato === "Eliminación directa";
  const isLeaguePlayoff = form.formato === "Liga + Playoffs";
  const isZonesPlayoff = form.formato === "Zonas + Playoffs";
  const teamCount = Math.max(2, Number(form.cantidadEquipos) || 2);
  const multiplier = form.idaVuelta ? 2 : 1;
  const estimatedRounds = (teamCount % 2 === 0 ? teamCount - 1 : teamCount) * multiplier;
  const leagueMatches = (teamCount * (teamCount - 1) / 2) * multiplier;
  const playoffOptions = BRACKET_COUNTS.filter((value) => value <= teamCount);
  const zoneOptions = BRACKET_COUNTS.filter((value) => value <= Math.floor(teamCount / 2));
  const zones = Math.max(2, Number(form.cantidadZonas) || 2);
  const zoneQualifierOptions = Array.from({ length: Math.max(1, Math.floor(teamCount / zones)) }, (_, index) => index + 1)
    .filter((value) => isPowerOfTwo(zones * value) && zones * value <= 16);
  const qualifiedTeams = zones * Math.max(1, Number(form.clasificadosPorZona) || 1);
  const largestZone = Math.ceil(teamCount / zones);
  const zoneRounds = (largestZone % 2 === 0 ? largestZone - 1 : largestZone) * multiplier;
  const zoneSizes = Array.from({ length: zones }, (_, index) => Math.floor(teamCount / zones) + (index < teamCount % zones ? 1 : 0));
  const groupMatches = zoneSizes.reduce((total, size) => total + size * (size - 1) / 2, 0) * multiplier;
  const setTeamCount = (value) => setForm((current) => {
    const count = Math.max(2, Math.min(100, Number(value) || 2));
    const next = { ...current, cantidadEquipos: count };
    if (current.formato === "Liga + Playoffs") next.clasificadosPlayoff = BRACKET_COUNTS.filter((item) => item <= count).at(-1) || 2;
    if (current.formato === "Zonas + Playoffs") {
      const nextZones = BRACKET_COUNTS.filter((item) => item <= Math.floor(count / 2)).at(-1) || 2;
      next.cantidadZonas = Math.min(Number(current.cantidadZonas) || 2, nextZones);
      const maxPerZone = Math.max(1, Math.floor(count / next.cantidadZonas));
      const options = Array.from({ length: maxPerZone }, (_, index) => index + 1).filter((item) => isPowerOfTwo(next.cantidadZonas * item));
      next.clasificadosPorZona = options.includes(Number(current.clasificadosPorZona)) ? Number(current.clasificadosPorZona) : (options[0] || 1);
    }
    return next;
  });
  const selectFormat = (format) => setForm((current) => {
    const next = { ...current, formato: format, idaVuelta: format === "Eliminación directa" ? false : current.idaVuelta };
    if (format === "Eliminación directa" && !isPowerOfTwo(Number(current.cantidadEquipos))) next.cantidadEquipos = 8;
    if (format === "Liga + Playoffs") next.clasificadosPlayoff = BRACKET_COUNTS.filter((value) => value <= Number(next.cantidadEquipos)).at(-1) || 2;
    if (format === "Zonas + Playoffs") {
      const validZones = BRACKET_COUNTS.filter((value) => value <= Math.floor(Number(next.cantidadEquipos) / 2));
      next.cantidadZonas = validZones.includes(Number(current.cantidadZonas)) ? Number(current.cantidadZonas) : (validZones[0] || 2);
      const maxPerZone = Math.max(1, Math.floor(Number(next.cantidadEquipos) / next.cantidadZonas));
      const validQualifiers = Array.from({ length: maxPerZone }, (_, index) => index + 1).filter((value) => isPowerOfTwo(next.cantidadZonas * value));
      next.clasificadosPorZona = validQualifiers.includes(Number(current.clasificadosPorZona)) ? Number(current.clasificadosPorZona) : (validQualifiers[0] || 1);
    }
    return next;
  });
  useEffect(() => {
    if (torneo) {
      setForm({ ...EMPTY_FORM, ...torneo, idaVuelta: Boolean(torneo.idaVuelta) });
      setLocationConfirmed(Boolean(torneo.ubicacion));
      setDraftRestored(false);
    } else {
      try {
        const saved = JSON.parse(window.localStorage.getItem(DRAFT_KEY));
        if (saved?.form) {
          setForm({ ...EMPTY_FORM, ...saved.form });
          setLocationConfirmed(Boolean(saved.locationConfirmed));
          setDraftRestored(true);
        } else {
          setForm(EMPTY_FORM); setLocationConfirmed(false); setDraftRestored(false);
        }
      } catch { setForm(EMPTY_FORM); setLocationConfirmed(false); setDraftRestored(false); }
    }
    setStep(1);
    setShowAdvancedRules(false);
    setShowPublicInfo(Boolean(torneo?.reglasTexto || torneo?.premiosTexto));
  }, [torneo]);
  useEffect(() => {
    if (!isEditing && (form.nombre || form.descripcion || form.ubicacion)) {
      window.localStorage.setItem(DRAFT_KEY, JSON.stringify({ form, locationConfirmed }));
    }
  }, [form, isEditing, locationConfirmed]);
  const updateField = ({ target: { name, value, type, checked } }) => setForm((current) => ({ ...current, [name]: type === "checkbox" ? checked : value }));
  const discardDraft = () => {
    window.localStorage.removeItem(DRAFT_KEY);
    setForm(EMPTY_FORM);
    setStep(1);
    setError("");
    setLocationConfirmed(false);
    setDraftRestored(false);
    setShowAdvancedRules(false);
    setShowPublicInfo(false);
  };
  const next = () => {
    setError("");
    if (step === 1 && (!form.nombre.trim() || !form.ubicacion.trim())) return setError("Completá el nombre y la ciudad o barrio.");
    if (step === 1 && !locationConfirmed) return setError("Elegí una ciudad o barrio de las sugerencias oficiales.");
    if (step === 2 && Number(form.cantidadEquipos) < 2) return setError("El torneo necesita al menos 2 equipos.");
    if (step === 2 && isDirectElimination && !isPowerOfTwo(Number(form.cantidadEquipos))) return setError("Para eliminación directa elegí 2, 4, 8 o 16 equipos.");
    if (step === 2 && isLeaguePlayoff && (!isPowerOfTwo(Number(form.clasificadosPlayoff)) || Number(form.clasificadosPlayoff) > Number(form.cantidadEquipos))) return setError("Elegí una cantidad válida de clasificados a playoffs.");
    if (step === 2 && form.formato === "Zonas + Playoffs" && Number(form.cantidadZonas) > Number(form.cantidadEquipos)) return setError("No puede haber más zonas que equipos.");
    if (step === 2 && form.formato === "Zonas + Playoffs" && Number(form.cantidadZonas) * Number(form.clasificadosPorZona) > Number(form.cantidadEquipos)) return setError("Los clasificados no pueden superar la cantidad de equipos.");
    if (step === 2 && isZonesPlayoff && !isPowerOfTwo(Number(form.cantidadZonas) * Number(form.clasificadosPorZona))) return setError("La clasificación debe formar una llave de 2, 4, 8 o 16 equipos.");
    setStep((current) => Math.min(4, current + 1));
  };
  const handleSubmit = async (event) => {
    event.preventDefault();
    if (step < 4) return next();
    setSaving(true); setError("");
    try {
      const response = await apiFetch(isEditing ? `${API_URL}/torneos/${torneo.idTorneo}` : `${API_URL}/torneos`, {
        method: isEditing ? "PUT" : "POST", headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ...form, cantidadEquipos: Number(form.cantidadEquipos), cantidadZonas: Number(form.cantidadZonas), clasificadosPorZona: Number(form.clasificadosPorZona), clasificadosPlayoff: Number(form.clasificadosPlayoff), generarEquiposPrueba: false, jugadoresPorEquipoPrueba: 0, puntosVictoria: isDirectElimination ? 0 : Number(form.puntosVictoria), puntosEmpate: isDirectElimination ? 0 : Number(form.puntosEmpate), puntosDerrota: isDirectElimination ? 0 : Number(form.puntosDerrota), criterioDesempate: isDirectElimination ? "PENALES" : form.criterioDesempate }),
      });
      const saved = await response.json().catch(() => null);
      if (!response.ok) throw new Error(saved?.message || "No se pudo guardar el torneo");
      if (!isEditing) window.localStorage.removeItem(DRAFT_KEY);
      onSaved(saved); onClose();
    } catch (requestError) { setError(requestError.message); }
    finally { setSaving(false); }
  };
  return <div className="tournament-form-backdrop" onMouseDown={onClose}><section className="tournament-form-modal tournament-wizard" role="dialog" aria-modal="true" onMouseDown={(event) => event.stopPropagation()}>
    <button type="button" className="tournament-form-close" aria-label="Cerrar" onClick={onClose}>×</button>
    <span className="tournament-form-chip">{isEditing ? "Configuración del torneo" : "Nuevo torneo"}</span>
    <h2>{isEditing ? "Editar torneo" : "Creá tu torneo"}</h2>
    {!isEditing && draftRestored && <div className="wizard-restored"><span>Continuamos desde el último dato que habías cargado.</span><button type="button" onClick={discardDraft}>Descartar y empezar de cero</button></div>}
    <div className="wizard-progress">{STEPS.map((label, index) => <button type="button" key={label} className={step === index + 1 ? "active" : step > index + 1 ? "done" : ""} onClick={() => index + 1 < step && setStep(index + 1)}><span>{step > index + 1 ? "✓" : index + 1}</span>{label}</button>)}</div>
    <form onSubmit={handleSubmit}>
      {step === 1 && <div className="wizard-step">
        <div><h3>Información básica</h3><p>Con el nombre y la ubicación alcanza para comenzar. El resto lo podés completar ahora o más adelante.</p></div>
        <label>Nombre del torneo<input autoFocus name="nombre" value={form.nombre} onChange={updateField} maxLength="120" placeholder="Ej. Liga Apertura 2026" required /></label>
        <div className="tournament-form-grid"><label>Ciudad o barrio<ArgentinaLocationInput value={form.ubicacion} confirmed={locationConfirmed} onChange={(value) => { setForm((current) => ({ ...current, ubicacion: value })); setLocationConfirmed(false); }} onSelect={(value) => { setForm((current) => ({ ...current, ubicacion: value })); setLocationConfirmed(true); setError(""); }} required /></label>{isEditing && <label>Estado actual<input value={form.estado} disabled /></label>}</div>
        <label>Descripción <small className="optional-field">Opcional</small><textarea name="descripcion" value={form.descripcion} onChange={updateField} maxLength="500" rows="3" placeholder="Ej. Torneo amateur de fútbol 7, categoría libre, que se juega los domingos." /><small className="field-example"><strong>Ejemplo de qué poner:</strong> modalidad, categoría, días de juego y a quién está dirigido el torneo.</small></label>
        <details className="optional-location" defaultOpen={Boolean(form.direccionSede || form.googleMapsUrl)}>
          <summary>Agregar ubicación detallada <span>Opcional</span></summary>
          <div className="tournament-form-grid"><label>Complejo o predio (cancha)<input name="direccionSede" value={form.direccionSede} onChange={updateField} maxLength="200" placeholder="Ej. Complejo Deportivo Paso del Rey" /></label><label>Enlace de Google Maps<input type="url" name="googleMapsUrl" value={form.googleMapsUrl} onChange={updateField} maxLength="1000" placeholder="https://maps.app.goo.gl/..." /></label></div>
          <small className="maps-field-help">Abrí el predio en Google Maps, tocá Compartir y pegá acá el enlace.</small>
        </details>
      </div>}
      {step === 2 && <div className="wizard-step">
        <div><h3>¿Cómo se va a jugar?</h3><p>Elegí el formato y FUTAPP te mostrará cómo quedará antes de crear el torneo.</p></div>
        <div className="format-options">{FORMATS.map((option) => <button key={option.value} type="button" className={`format-card ${form.formato === option.value ? "selected" : ""}`} onClick={() => selectFormat(option.value)}><span>{option.icon}</span><strong>{option.title}</strong><small>{option.detail}</small><em>{form.formato === option.value ? "Seleccionado" : "Elegir"}</em></button>)}</div>
        <div className="team-count-control">
          <div><strong>Cantidad de equipos</strong><small>Después vas a completar estos lugares con equipos reales.</small></div>
          <div className="team-count-stepper"><button type="button" onClick={() => setTeamCount(teamCount - 1)} aria-label="Quitar un equipo">−</button><input type="number" value={form.cantidadEquipos} onChange={(event) => setTeamCount(event.target.value)} min="2" max="100" aria-label="Cantidad de equipos" /><button type="button" onClick={() => setTeamCount(teamCount + 1)} aria-label="Agregar un equipo">+</button></div>
          <div className="team-count-presets">{(isDirectElimination ? [4, 8, 16] : COMMON_TEAM_COUNTS).map((count) => <button type="button" key={count} className={teamCount === count ? "selected" : ""} onClick={() => setTeamCount(count)}>{count}</button>)}</div>
        </div>
        {!isDirectElimination && <label className="wizard-toggle"><input type="checkbox" name="idaVuelta" checked={form.idaVuelta} onChange={updateField} /><span><strong>Partidos de ida y vuelta</strong><small>{form.idaVuelta ? "Cada equipo enfrentará dos veces a cada rival." : "Cada equipo enfrentará una vez a cada rival."}</small></span></label>}
        {isLeaguePlayoff && <label>Equipos que clasifican a playoffs<select name="clasificadosPlayoff" value={form.clasificadosPlayoff} onChange={updateField}>{playoffOptions.map((count) => <option value={count} key={count}>{count} equipos</option>)}</select></label>}
        {isZonesPlayoff && <div className="tournament-form-grid"><label>Cantidad de zonas<select name="cantidadZonas" value={form.cantidadZonas} onChange={(event) => setForm((current) => ({ ...current, cantidadZonas: Number(event.target.value), clasificadosPorZona: 1 }))}>{zoneOptions.map((count) => <option value={count} key={count}>{count} zonas</option>)}</select></label><label>Clasificados por zona<select name="clasificadosPorZona" value={form.clasificadosPorZona} onChange={updateField}>{zoneQualifierOptions.map((count) => <option value={count} key={count}>{count} por zona</option>)}</select></label></div>}
        <div className="format-projection">
          <span>Así quedará</span>
          {form.formato === "Liga" && <><strong>{teamCount} equipos · {form.idaVuelta ? "ida y vuelta" : "una rueda"}</strong><p>{estimatedRounds} fechas · {Math.floor(teamCount / 2)} partidos por fecha{teamCount % 2 ? " y un equipo libre" : ""} · {leagueMatches} partidos estimados.</p></>}
          {isLeaguePlayoff && <><strong>{teamCount} equipos · clasifican {form.clasificadosPlayoff}</strong><p>{estimatedRounds} fechas de liga · después {bracketRoundName(Number(form.clasificadosPlayoff))} · {leagueMatches + Number(form.clasificadosPlayoff) - 1} partidos estimados.</p></>}
          {isZonesPlayoff && <><strong>{teamCount} equipos · {zones} zonas · clasifican {form.clasificadosPorZona} por zona</strong><p>Zonas de {zoneSizes.join(" / ")} equipos · {zoneRounds} fechas de grupos · llave final de {qualifiedTeams} · {groupMatches + qualifiedTeams - 1} partidos estimados.</p></>}
          {isDirectElimination && <><strong>{teamCount} equipos · {bracketRoundName(teamCount)}</strong><p>{Math.log2(teamCount)} rondas · {teamCount - 1} partidos · cada cruce debe tener un ganador.</p></>}
          <small>Se crearán {teamCount} cupos vacíos. Al terminar vas directamente a cargar los equipos.</small>
        </div>
      </div>}
      {step === 3 && <div className="wizard-step">
        <div><h3>Configuración deportiva</h3><p>FUTAPP ya eligió la configuración más usada. Solo cambiala si tu reglamento funciona distinto.</p></div>
        {isDirectElimination ? <div className="knockout-rule"><span>⚽</span><div><strong>Empate: definición por penales</strong><small>Cada llave debe tener un ganador y no se asignan puntos.</small></div></div> : <>
          <div className="sports-default-card"><div><span>Configuración actual</span><strong>{form.puntosVictoria} puntos por victoria · {form.puntosEmpate} por empate · {form.puntosDerrota} por derrota</strong><small>Desempate: {form.criterioDesempate === "GOLES_FAVOR" ? "goles a favor" : "diferencia de goles"}.</small></div><span className="recommended-badge">Recomendada</span></div>
          <button type="button" className="configuration-toggle" onClick={() => setShowAdvancedRules((current) => !current)}>{showAdvancedRules ? "Ocultar configuración avanzada" : "Cambiar configuración"}<span>{showAdvancedRules ? "−" : "+"}</span></button>
          {showAdvancedRules && <div className="advanced-rules"><div className="points-grid"><label>Victoria<input type="number" min="0" max="10" name="puntosVictoria" value={form.puntosVictoria} onChange={updateField} /></label><label>Empate<input type="number" min="0" max="10" name="puntosEmpate" value={form.puntosEmpate} onChange={updateField} /></label><label>Derrota<input type="number" min="0" max="10" name="puntosDerrota" value={form.puntosDerrota} onChange={updateField} /></label></div><label>Primer criterio de desempate<select name="criterioDesempate" value={form.criterioDesempate} onChange={updateField}><option value="DIFERENCIA_GOLES">Diferencia de goles</option><option value="GOLES_FAVOR">Goles a favor</option></select></label></div>}
        </>}
        <div className="optional-public-info"><button type="button" onClick={() => setShowPublicInfo((current) => !current)}><span><strong>Agregar reglamento y premios ahora</strong><small>Opcional. También podés completarlos después.</small></span><b>{showPublicInfo ? "−" : "+"}</b></button>{showPublicInfo && <div><label>Reglamento completo<textarea name="reglasTexto" value={form.reglasTexto} onChange={updateField} maxLength="5000" rows="5" placeholder="Detallá reglas de inscripción, conducta, suspensiones, horarios, indumentaria y cualquier condición del torneo." /></label><label>Premios<textarea name="premiosTexto" value={form.premiosTexto} onChange={updateField} maxLength="3000" rows="3" placeholder="Ej. Trofeo y medallas para el campeón, premio al goleador y valla menos vencida." /></label></div>}</div>
      </div>}
      {step === 4 && <div className="wizard-step">
        <div><h3>Revisá antes de crear</h3><p>Confirmá que los datos principales estén bien. Podés volver a cualquier sección para corregirla.</p></div>
        <div className="review-grid">
          <article className="review-card"><div><span>Datos</span><button type="button" onClick={() => setStep(1)}>Editar</button></div><strong>{form.nombre || "Sin nombre"}</strong><p>{form.ubicacion}{form.direccionSede ? ` · ${form.direccionSede}` : ""}</p>{form.descripcion && <small>{form.descripcion}</small>}</article>
          <article className="review-card"><div><span>Formato</span><button type="button" onClick={() => setStep(2)}>Editar</button></div><strong>{form.formato}</strong><p>{teamCount} equipos{isLeaguePlayoff ? ` · clasifican ${form.clasificadosPlayoff}` : isZonesPlayoff ? ` · ${zones} zonas · ${form.clasificadosPorZona} por zona` : !isDirectElimination ? ` · ${form.idaVuelta ? "ida y vuelta" : "una rueda"}` : ` · ${bracketRoundName(teamCount)}`}</p><small>{isZonesPlayoff ? `${zoneRounds} fechas de grupos y llave de ${qualifiedTeams}` : isDirectElimination ? `${Math.log2(teamCount)} rondas y ${teamCount - 1} partidos` : `${estimatedRounds} fechas estimadas`}</small></article>
          <article className="review-card"><div><span>Configuración</span><button type="button" onClick={() => setStep(3)}>Editar</button></div><strong>{isDirectElimination ? "Penales en caso de empate" : `${form.puntosVictoria} / ${form.puntosEmpate} / ${form.puntosDerrota} puntos`}</strong><p>{isDirectElimination ? "Cada llave tendrá un ganador" : `Desempate por ${form.criterioDesempate === "GOLES_FAVOR" ? "goles a favor" : "diferencia de goles"}`}</p><small>{form.reglasTexto || form.premiosTexto ? `${form.reglasTexto ? "Reglamento cargado" : "Sin reglamento"} · ${form.premiosTexto ? "Premios cargados" : "Sin premios"}` : "Reglamento y premios pendientes"}</small></article>
        </div>
        <div className="review-next-step"><span>✓</span><div><strong>{isEditing ? "Se guardarán los cambios del torneo" : "Se creará como borrador"}</strong><p>{isEditing ? "Podrás seguir administrándolo desde su perfil." : `Quedarán ${teamCount} cupos vacíos y te llevaremos directamente a cargar los equipos.`}</p></div></div>
      </div>}
      {error && <p className="tournament-form-error">{error}</p>}
      <div className="tournament-form-actions"><button type="button" className="tournament-form-cancel" onClick={step === 1 ? onClose : () => { setError(""); setStep((current) => current - 1); }}>{step === 1 ? "Cerrar" : "Atrás"}</button><button type="submit" className="tournament-form-save" disabled={saving}>{saving ? "Guardando…" : step < 4 ? "Continuar" : isEditing ? "Guardar cambios" : "Crear torneo y cargar equipos"}</button></div>
    </form>
  </section></div>;
}
export default TournamentFormModal;
