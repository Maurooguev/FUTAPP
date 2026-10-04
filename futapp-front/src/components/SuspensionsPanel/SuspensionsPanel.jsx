import { Link } from "react-router-dom";
import { API_URL, apiFetch } from "../../config/api";
import EntityImage from "../EntityImage/EntityImage";
import "./SuspensionsPanel.css";

export default function SuspensionsPanel({ sanciones, canManage, onUpdated, onError }) {
  const editar = async (sancion) => {
    const cantidad = window.prompt("Cantidad total de fechas de suspensión:", sancion.fechasTotal);
    if (cantidad === null) return;
    const fechasTotal = Number(cantidad);
    if (!Number.isInteger(fechasTotal) || fechasTotal < 1) return onError("La suspensión debe ser de al menos una fecha.");
    const motivo = window.prompt("Motivo o detalle de la sanción:", sancion.motivo || "") ?? sancion.motivo;
    const response = await apiFetch(`${API_URL}/sanciones/${sancion.idSancion}`, { method: "PUT", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ fechasTotal, motivo }) });
    const payload = await response.json().catch(() => null);
    if (!response.ok) return onError(payload?.message || "No se pudo actualizar la suspensión");
    onUpdated(payload);
  };

  return <section className="suspensions-card">
    <header><div><span>Disciplina</span><h2>Suspendidos</h2></div><b>{sanciones.filter((item) => item.activa).length}</b></header>
    {sanciones.length === 0 ? <div className="suspensions-empty"><strong>Sin suspendidos</strong><small>Las expulsiones aparecerán automáticamente acá.</small></div> : <div className="suspensions-list">
      {sanciones.map((sancion) => <article className={sancion.activa ? "active" : "served"} key={sancion.idSancion}>
        <span className="suspension-avatar"><EntityImage type="player" value={sancion.fotoJugador} alt="" /></span>
        <div className="suspension-player"><Link to={`/jugador/${sancion.idJugador}`}>{sancion.nombreJugador}</Link><Link to={`/equipo/${sancion.idEquipo}`}>{sancion.nombreEquipo}</Link><small>{sancion.motivo || `Expulsado en fecha ${sancion.fechaOrigen}`}</small></div>
        <div className="suspension-progress"><strong>{sancion.fechasCumplidas}/{sancion.fechasTotal}</strong><span>fechas cumplidas</span><i><b style={{ width: `${Math.min(100, (sancion.fechasCumplidas / sancion.fechasTotal) * 100)}%` }} /></i></div>
        {canManage && <button type="button" onClick={() => editar(sancion)}>Editar</button>}
        {!sancion.activa && <em>Cumplida</em>}
      </article>)}
    </div>}
  </section>;
}

