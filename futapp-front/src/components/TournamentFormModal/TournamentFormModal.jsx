import { useEffect, useState } from "react";
import { API_URL, apiFetch } from "../../config/api";
import "./TournamentFormModal.css";

const EMPTY_FORM = {
  nombre: "",
  descripcion: "",
  ubicacion: "",
  estado: "ACTIVO",
  formato: "Liga",
  cantidadEquipos: 2,
};

function TournamentFormModal({ torneo, onClose, onSaved }) {
  const [form, setForm] = useState(EMPTY_FORM);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const isEditing = Boolean(torneo?.idTorneo);

  useEffect(() => {
    if (torneo) {
      setForm({
        nombre: torneo.nombre ?? "",
        descripcion: torneo.descripcion ?? "",
        ubicacion: torneo.ubicacion ?? "",
        estado: torneo.estado ?? "ACTIVO",
        formato: torneo.formato ?? "Liga",
        cantidadEquipos: torneo.cantidadEquipos ?? 2,
      });
    } else {
      setForm(EMPTY_FORM);
    }
  }, [torneo]);

  const updateField = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError("");

    const url = isEditing
      ? `${API_URL}/torneos/${torneo.idTorneo}`
      : `${API_URL}/torneos`;

    try {
      const response = await apiFetch(url, {
        method: isEditing ? "PUT" : "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          ...form,
          cantidadEquipos: Number(form.cantidadEquipos),
        }),
      });

      if (!response.ok) {
        const payload = await response.json().catch(() => null);
        throw new Error(payload?.message || "No se pudo guardar el torneo");
      }

      const savedTournament = await response.json();
      onSaved(savedTournament);
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
        aria-labelledby="tournament-form-title"
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

        <span className="tournament-form-chip">
          {isEditing ? "Administración" : "Nuevo torneo"}
        </span>
        <h2 id="tournament-form-title">
          {isEditing ? "Editar torneo" : "Crear torneo"}
        </h2>
        <p>Completá los datos generales de la competencia.</p>

        <form onSubmit={handleSubmit}>
          <label>
            Nombre
            <input
              name="nombre"
              value={form.nombre}
              onChange={updateField}
              maxLength="120"
              required
            />
          </label>

          <label>
            Descripción
            <textarea
              name="descripcion"
              value={form.descripcion}
              onChange={updateField}
              maxLength="500"
              rows="3"
              required
            />
          </label>

          <div className="tournament-form-grid">
            <label>
              Ubicación
              <input
                name="ubicacion"
                value={form.ubicacion}
                onChange={updateField}
                maxLength="120"
                required
              />
            </label>

            <label>
              Cantidad de equipos
              <input
                type="number"
                name="cantidadEquipos"
                value={form.cantidadEquipos}
                onChange={updateField}
                min="2"
                max="100"
                required
              />
            </label>

            <label>
              Formato
              <select name="formato" value={form.formato} onChange={updateField}>
                <option value="Liga">Liga</option>
                <option value="Eliminación directa">Eliminación directa</option>
                <option value="Grupos y eliminación">Grupos y eliminación</option>
              </select>
            </label>

            <label>
              Estado
              <select name="estado" value={form.estado} onChange={updateField}>
                <option value="ACTIVO">Activo</option>
                <option value="BORRADOR">Borrador</option>
                <option value="FINALIZADO">Finalizado</option>
              </select>
            </label>
          </div>

          {error && <p className="tournament-form-error">{error}</p>}

          <div className="tournament-form-actions">
            <button type="button" className="tournament-form-cancel" onClick={onClose}>
              Cancelar
            </button>
            <button type="submit" className="tournament-form-save" disabled={saving}>
              {saving ? "Guardando..." : isEditing ? "Guardar cambios" : "Crear torneo"}
            </button>
          </div>
        </form>
      </section>
    </div>
  );
}

export default TournamentFormModal;
