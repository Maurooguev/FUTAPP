import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { API_URL } from "../config/api";

function PartidoDetalle() {
  const { id } = useParams();
  const [partido, setPartido] = useState(null);
  const [goles, setGoles] = useState([]);
  const [tarjetas, setTarjetas] = useState([]);

  useEffect(() => {
    fetch(`${API_URL}/partidos/${id}`)
      .then((res) => res.json())
      .then((data) => setPartido(data))
      .catch((err) => console.error("Partido:", err));

    fetch(`${API_URL}/goles/partido/${id}`)
      .then((res) => res.json())
      .then((data) => setGoles(data))
      .catch((err) => console.error("Goles:", err));

    fetch(`${API_URL}/tarjetas/partido/${id}`)
      .then((res) => res.json())
      .then((data) => setTarjetas(data))
      .catch((err) => console.error("Tarjetas:", err));
  }, [id]);

  if (!partido) {
    return <p style={{ padding: "20px" }}>Cargando partido...</p>;
  }

  return (
    <div className="partido-page">

      <section className="partido-hero">
        <div className="partido-shell">
          <Link to={`/torneo/${partido.idTorneo}`} className="volver-link">
            ← Volver al torneo
          </Link>

          <span className="partido-chip">Fecha {partido.numeroFecha}</span>

          <div className="scoreboard">
            <div className="team-box">
              <h2>{partido.nombreEquipoLocal}</h2>
              <strong>{partido.golesLocal}</strong>
            </div>

            <div className="versus">VS</div>

            <div className="team-box">
              <h2>{partido.nombreEquipoVisitante}</h2>
              <strong>{partido.golesVisitante}</strong>
            </div>
          </div>

          <p className="partido-info">
            {partido.fechaHora} · {partido.estado}
          </p>
        </div>
      </section>

      <main className="partido-content">
        <div className="partido-shell partido-grid">
          <section className="partido-panel">
            <h2>Goles</h2>

            {goles.length === 0 ? (
              <p className="empty-text">No hay goles cargados.</p>
            ) : (
              goles.map((gol) => (
                <div className="incidencia-card" key={gol.idGol}>
                  <strong>{gol.minuto}'</strong>
                  <div>
                    <h3>{gol.nombreJugador}</h3>
                    <p>{gol.nombreEquipo} · {gol.tipoGol}</p>
                  </div>
                </div>
              ))
            )}
          </section>

          <section className="partido-panel">
            <h2>Tarjetas</h2>

            {tarjetas.length === 0 ? (
              <p className="empty-text">No hay tarjetas cargadas.</p>
            ) : (
              tarjetas.map((tarjeta) => (
                <div className="incidencia-card" key={tarjeta.idTarjeta}>
                  <strong>{tarjeta.minuto ? `${tarjeta.minuto}'` : "-"}</strong>
                  <div>
                    <h3>{tarjeta.nombreJugador}</h3>
                    <p>
                      {tarjeta.nombreEquipo} · {tarjeta.tipoTarjeta}
                      {tarjeta.motivo ? ` · ${tarjeta.motivo}` : ""}
                    </p>
                  </div>
                </div>
              ))
            )}
          </section>
        </div>
      </main>
    </div>
  );
}

export default PartidoDetalle;
