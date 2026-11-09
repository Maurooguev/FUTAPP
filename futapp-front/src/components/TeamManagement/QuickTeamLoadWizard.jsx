import { useMemo, useState } from "react";
import { API_URL, apiFetch } from "../../config/api";
import "./QuickTeamLoadWizard.css";

const normalizeNames = (value) => value
  .split(/\r?\n/)
  .map((name) => name.trim().replace(/\s+/g, " "))
  .filter(Boolean);

function QuickTeamLoadWizard({ torneoId, capacidad, equiposActuales, onClose, onSaved }) {
  const [step, setStep] = useState(1);
  const [rawNames, setRawNames] = useState("");
  const [playersPerTeam, setPlayersPerTeam] = useState(10);
  const [reviewNames, setReviewNames] = useState([]);
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);
  const parsedNames = useMemo(() => normalizeNames(rawNames), [rawNames]);
  const duplicateNames = useMemo(() => {
    const seen = new Set();
    return parsedNames.filter((name) => {
      const normalized = name.toLocaleLowerCase("es");
      if (seen.has(normalized)) return true;
      seen.add(normalized);
      return false;
    });
  }, [parsedNames]);
  const realTeams = equiposActuales.filter((team) => !team.descripcion?.toLocaleLowerCase("es").startsWith("equipo de prueba generado"));
  const available = Math.max(0, Number(capacidad || 2) - realTeams.length);

  const close = () => {
    if ((rawNames.trim() || step > 1) && !window.confirm("¿Salir de la carga rápida? Los datos todavía no se guardaron.")) return;
    onClose();
  };

  const continueFromTeams = () => {
    if (parsedNames.length === 0) return setError("Pegá al menos un nombre de equipo.");
    if (duplicateNames.length > 0) return setError(`Hay nombres repetidos: ${[...new Set(duplicateNames)].join(", ")}.`);
    if (parsedNames.length > available) return setError(`Podés cargar hasta ${available} equipos en este torneo.`);
    setReviewNames(parsedNames);
    setError("");
    setStep(2);
  };

  const updateReviewName = (index, value) => setReviewNames((current) => current.map((name, itemIndex) => itemIndex === index ? value : name));

  const continueToReview = () => {
    if (playersPerTeam < 1 || playersPerTeam > 30) return setError("Elegí entre 1 y 30 jugadores por equipo.");
    setError("");
    setStep(3);
  };

  const submit = async () => {
    const names = reviewNames.map((name) => name.trim()).filter(Boolean);
    const unique = new Set(names.map((name) => name.toLocaleLowerCase("es")));
    if (names.length !== reviewNames.length || unique.size !== names.length) {
      setError("Todos los equipos necesitan un nombre distinto.");
      return;
    }
    setSaving(true);
    setError("");
    try {
      const response = await apiFetch(`${API_URL}/equipos/torneo/${torneoId}/carga-rapida`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ nombresEquipos: names, jugadoresPorEquipo: playersPerTeam }),
      });
      const payload = await response.json().catch(() => null);
      if (!response.ok) throw new Error(payload?.message || "No se pudo completar la carga rápida");
      onSaved(payload);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="quick-wizard-backdrop" onMouseDown={close}>
      <section className="quick-wizard" role="dialog" aria-modal="true" aria-labelledby="quick-wizard-title" onMouseDown={(event) => event.stopPropagation()}>
        <header className="quick-wizard-header">
          <div><span>Carga rápida</span><h2 id="quick-wizard-title">Creá tus equipos fácil</h2><p>Pegá los nombres. FUTAPP se ocupa del resto.</p></div>
          <button type="button" aria-label="Cerrar carga rápida" onClick={close}>×</button>
        </header>

        <ol className="quick-wizard-steps" aria-label="Progreso de la carga">
          {["Equipos", "Jugadores", "Revisar"].map((label, index) => <li key={label} className={step === index + 1 ? "active" : step > index + 1 ? "done" : ""}><i>{step > index + 1 ? "✓" : index + 1}</i><span>{label}</span></li>)}
        </ol>

        <div className="quick-wizard-content">
          {step === 1 && <div className="quick-wizard-pane">
            <div><h3>Escribí un equipo por línea</h3><p>Podés copiar la lista desde WhatsApp, Excel o cualquier documento.</p></div>
            <textarea aria-label="Nombres de los equipos" autoFocus rows="9" value={rawNames} onChange={(event) => { setRawNames(event.target.value); setError(""); }} placeholder={"Los Pibes\nNewell's\nRacing\nVélez"} />
            <div className="quick-wizard-counter"><strong>{parsedNames.length} {parsedNames.length === 1 ? "equipo" : "equipos"}</strong><span>{available} lugares disponibles</span></div>
          </div>}

          {step === 2 && <div className="quick-wizard-pane quick-player-count">
            <div><h3>¿Cuántos jugadores tendrá cada equipo?</h3><p>Los creamos sin posición para que puedas completar sus datos después.</p></div>
            <div className="quick-count-control">
              <button type="button" aria-label="Quitar un jugador" disabled={playersPerTeam <= 1} onClick={() => setPlayersPerTeam((value) => Math.max(1, value - 1))}>−</button>
              <strong>{playersPerTeam}<small>jugadores</small></strong>
              <button type="button" aria-label="Agregar un jugador" disabled={playersPerTeam >= 30} onClick={() => setPlayersPerTeam((value) => Math.min(30, value + 1))}>+</button>
            </div>
            <p className="quick-player-note">Se aplicará a los {reviewNames.length} equipos. Los jugadores quedarán como Jugador 1, Jugador 2… hasta que los edites.</p>
          </div>}

          {step === 3 && <div className="quick-wizard-pane">
            <div><h3>Revisá antes de confirmar</h3><p>Podés corregir cualquier nombre directamente desde esta lista.</p></div>
            <div className="quick-review-list">
              {reviewNames.map((name, index) => <label key={index}><span>✓</span><input aria-label={`Nombre del equipo ${index + 1}`} value={name} onChange={(event) => updateReviewName(index, event.target.value)} /><small>{playersPerTeam} jugadores</small></label>)}
            </div>
            <div className="quick-review-total"><strong>{reviewNames.length} equipos</strong><span>·</span><strong>{reviewNames.length * playersPerTeam} jugadores</strong></div>
          </div>}

          {error && <p className="quick-wizard-error" role="alert">{error}</p>}
        </div>

        <footer className="quick-wizard-actions">
          {step > 1 && <button type="button" className="secondary" disabled={saving} onClick={() => { setError(""); setStep((value) => value - 1); }}>Volver</button>}
          {step === 1 && <button type="button" className="primary" onClick={continueFromTeams}>Continuar</button>}
          {step === 2 && <button type="button" className="primary" onClick={continueToReview}>Revisar carga</button>}
          {step === 3 && <button type="button" className="primary" disabled={saving} onClick={submit}>{saving ? "Creando equipos…" : "Confirmar carga"}</button>}
        </footer>
      </section>
    </div>
  );
}

export default QuickTeamLoadWizard;
