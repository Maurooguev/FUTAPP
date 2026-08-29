import { useMemo, useState } from "react";
import { API_URL, apiFetch } from "../../config/api";
import "../TournamentFormModal/TournamentFormModal.css";

function MatchFormModal({ torneoId, fechas, equipos, partido, onClose, onSaved }) {
  const sortedDates = useMemo(
    () => [...fechas].sort((a, b) => a.numeroFecha - b.numeroFecha),
    [fechas]
  );
  const sortedTeams = useMemo(
    () => [...equipos].sort((a, b) => a.nombre.localeCompare(b.nombre)),
    [equipos]
  );

  const isEditing = Boolean(partido?.idPartido);
  const [form, setForm] = useState({
    idFecha: partido?.idFecha ?? sortedDates[0]?.idFecha ?? "",
    idEquipoLocal: partido?.idEquipoLocal ?? "",
    idEquipoVisitante: partido?.idEquipoVisitante ?? "",
    fechaHora: partido?.fechaHora?.replace(" ", "T") ?? "",
    estado: partido?.estado ?? "PROGRAMADO",
    observaciones: partido?.observaciones ?? "",
    cancha: partido?.cancha ?? "",
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const hasRequiredData = sortedDates.length > 0 && sortedTeams.length >= 2;

  const updateField = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");

    if (form.idEquipoLocal === form.idEquipoVisitante) {
      setError("El equipo local y el visitante deben ser diferentes");
      return;
    }

    setSaving(true);

    try {
      const response = await apiFetch(isEditing ? `${API_URL}/partidos/${partido.idPartido}` : `${API_URL}/partidos`, {
        method: isEditing ? "PUT" : "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          fechaHora: form.fechaHora.replace("T", " "),
          estado: form.estado,
          golesLocal: 0,
          golesVisitante: 0,
          observaciones: form.observaciones,
          cancha: form.cancha,
          idTorneo: Number(torneoId),
          idFecha: Number(form.idFecha),
          idEquipoLocal: Number(form.idEquipoLocal),
          idEquipoVisitante: Number(form.idEquipoVisitante),
        }),
      });

      if (!response.ok) {
        const payload = await response.json().catch(() => null);
        throw new Error(payload?.message || "No se pudo crear el partido");
      }

      const savedMatch = await response.json();
      onSaved(savedMatch);
      onClose();
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="tournament-form-backdrop" onMouseDown={onClose}>
      <section
        className="tournament-form-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="match-form-title"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <button
          type="button"
          className="tournament-form-close"
          aria-label="Cerrar"
          onClick={onClose}
        >
          ×
        </button>

        <span className="tournament-form-chip">Fixture</span>
        <h2 id="match-form-title">{isEditing ? "Editar partido" : "Agregar partido"}</h2>
        <p>Programá un nuevo cruce dentro de una jornada del torneo.</p>

        {!hasRequiredData ? (
          <div className="match-form-empty">
            <strong>No se puede crear el partido todavía.</strong>
            {sortedTeams.length < 2 && <p>El torneo necesita al menos dos equipos.</p>}
            {sortedDates.length === 0 && <p>El torneo necesita al menos una jornada.</p>}
          </div>
        ) : (
          <form onSubmit={handleSubmit}>
            <div className="tournament-form-grid">
              <label>
                Jornada
                <select name="idFecha" value={form.idFecha} onChange={updateField} required>
                  {sortedDates.map((fecha) => (
                    <option key={fecha.idFecha} value={fecha.idFecha}>
                      Fecha {fecha.numeroFecha} — {fecha.descripcion}
                    </option>
                  ))}
                </select>
              </label>

              <label>
                Fecha y hora
                <input
                  type="datetime-local"
                  name="fechaHora"
                  value={form.fechaHora}
                  onChange={updateField}
                  required
                />
              </label>

              <label>
                Equipo local
                <select
                  name="idEquipoLocal"
                  value={form.idEquipoLocal}
                  onChange={updateField}
                  required
                >
                  <option value="">Seleccionar equipo</option>
                  {sortedTeams.map((equipo) => (
                    <option key={equipo.idEquipo} value={equipo.idEquipo}>
                      {equipo.nombre}
                    </option>
                  ))}
                </select>
              </label>

              <label>
                Equipo visitante
                <select
                  name="idEquipoVisitante"
                  value={form.idEquipoVisitante}
                  onChange={updateField}
                  required
                >
                  <option value="">Seleccionar equipo</option>
                  {sortedTeams.map((equipo) => (
                    <option key={equipo.idEquipo} value={equipo.idEquipo}>
                      {equipo.nombre}
                    </option>
                  ))}
                </select>
              </label>

              <label>
                Estado
                <select name="estado" value={form.estado} onChange={updateField}>
                  <option value="PROGRAMADO">Programado</option>
                  <option value="SUSPENDIDO">Suspendido</option>
                  <option value="CANCELADO">Cancelado</option><option value="FINALIZADO">Finalizado</option>
                </select>
              </label>
              <label>Cancha o sede<input name="cancha" value={form.cancha} onChange={updateField} maxLength="150" placeholder="Ej. Cancha 1" /></label>
            </div>

            <label>
              Observaciones
              <textarea
                name="observaciones"
                value={form.observaciones}
                onChange={updateField}
                maxLength="500"
                rows="3"
                placeholder="Cancha, indicaciones u otros detalles"
              />
            </label>

            {error && <p className="tournament-form-error">{error}</p>}

            <div className="tournament-form-actions">
              <button type="button" className="tournament-form-cancel" onClick={onClose}>
                Cancelar
              </button>
              <button type="submit" className="tournament-form-save" disabled={saving}>
                {saving ? "Guardando..." : isEditing ? "Guardar cambios" : "Agregar partido"}
              </button>
            </div>
          </form>
        )}
      </section>
    </div>
  );
}

export default MatchFormModal;
