import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import HeroHome from "../components/HeroHome/HeroHome";
import SectionTitle from "../components/SectionTitle/SectionTitle";
import TournamentCard from "../components/TournamentCard/TournamentCard";
import TournamentFormModal from "../components/TournamentFormModal/TournamentFormModal";
import { API_URL } from "../config/api";
import useAuth from "../context/useAuth";

function Home() {
  const [torneos, setTorneos] = useState([]);
  const [busqueda, setBusqueda] = useState("");
  const [showTournamentForm, setShowTournamentForm] = useState(false);
  const navigate = useNavigate();
  const { usuario } = useAuth();

  useEffect(() => {
    fetch(`${API_URL}/torneos`)
      .then((res) => {
        if (!res.ok) throw new Error("Error al cargar torneos");
        return res.json();
      })
      .then((data) => setTorneos(data))
      .catch((error) => console.error("Torneos:", error));
  }, []);

  const torneosFiltrados = useMemo(() => {
    return torneos.filter((torneo) => {
      const texto = `${torneo.nombre} ${torneo.codigoPublico ?? ""} ${torneo.ubicacion} ${torneo.estado}`.toLowerCase();
      return texto.includes(busqueda.toLowerCase());
    });
  }, [torneos, busqueda]);

  const destacados = torneos.slice(0, 3);

  const torneosActivos = torneos.filter(
    (torneo) => torneo.estado?.toUpperCase() === "ACTIVO"
  ).length;

  const totalEquipos = torneos.reduce(
    (acc, torneo) => acc + (torneo.cantidadEquipos || 0),
    0
  );

  const irATorneo = (idTorneo) => {
    navigate(`/torneo/${idTorneo}`);
  };

  const irATorneos = () => {
    document.getElementById("todos-torneos")?.scrollIntoView({ behavior: "smooth", block: "start" });
  };

  const crearTorneo = () => {
    if (!usuario) return navigate("/register");
    setShowTournamentForm(true);
  };

  const primaryLabel = "Crear torneo";

  return (
    <div className="home">

      <HeroHome
        busqueda={busqueda}
        onBusquedaChange={setBusqueda}
        onSearch={irATorneos}
        onCreate={crearTorneo}
        onExplore={irATorneos}
        primaryLabel={primaryLabel}
      />

      <section className="stats-section">
        <div className="stat-card">
          <span className="stat-number">{torneos.length}</span>
          <span className="stat-label">Torneos cargados</span>
        </div>

        <div className="stat-card">
          <span className="stat-number">{torneosActivos}</span>
          <span className="stat-label">Torneos activos</span>
        </div>

        <div className="stat-card">
          <span className="stat-number">{totalEquipos}</span>
          <span className="stat-label">Equipos registrados</span>
        </div>
      </section>

      <section className="section" id="destacados">
        <SectionTitle
          chip="Destacados"
          title="Torneos destacados"
          description="Explorá los torneos más importantes y entrá directo a sus estadísticas."
        />

        <div className="cards-grid featured-grid">
          {destacados.map((torneo) => (
            <TournamentCard
              key={torneo.idTorneo}
              torneo={torneo}
              variant="featured"
              onClick={() => irATorneo(torneo.idTorneo)}
            />
          ))}
        </div>
      </section>

      <section className="section section-soft" id="todos-torneos">
        <SectionTitle
          chip="Explorar"
          title="Todos los torneos"
          description="Filtrá rápidamente y encontrá el torneo que estás buscando."
        />

        {torneosFiltrados.length === 0 ? (
          <div className="empty-state">
            <h3>No encontramos torneos</h3>
            <p>Probá con otro nombre, estado o ubicación.</p>
          </div>
        ) : (
          <div className="cards-grid">
            {torneosFiltrados.map((torneo) => (
              <TournamentCard
                key={torneo.idTorneo}
                torneo={torneo}
                onClick={() => irATorneo(torneo.idTorneo)}
              />
            ))}
          </div>
        )}
      </section>

      <section className="section" id="como-funciona">
        <SectionTitle
          chip="Proceso"
          title="Cómo funciona FUTAPP"
          description="Una experiencia simple para organizadores, equipos y torneos."
          center
        />

        <div className="steps-grid">
          <div className="step-card">
            <div className="step-number">1</div>
            <h3>Creá el torneo</h3>
            <p>Definí formato, cantidad de equipos, fechas y estado inicial del torneo.</p>
          </div>

          <div className="step-card">
            <div className="step-number">2</div>
            <h3>Cargá equipos y jugadores</h3>
            <p>Registrá planteles, responsables, camisetas y armá la estructura del campeonato.</p>
          </div>

          <div className="step-card">
            <div className="step-number">3</div>
            <h3>SeguÍ todo en vivo</h3>
            <p>Resultados, tabla de posiciones, goleadores y tarjetas actualizadas automáticamente.</p>
          </div>
        </div>
      </section>

      <section className="section section-soft" id="beneficios">
        <SectionTitle
          chip="Beneficios"
          title="Todo lo que necesitás para organizar mejor"
          description="Diseñado para torneos amateur, ligas barriales y competencias locales."
          center
        />

        <div className="cards-grid">
          <div className="card feature-card">
            <h3>📊 Tabla automática</h3>
            <p>La tabla de posiciones se actualiza a partir de los partidos cargados.</p>
          </div>

          <div className="card feature-card">
            <h3>🥅 Goleadores y estadísticas</h3>
            <p>Mostrá top 5, rendimiento por torneo y seguimiento de jugadores.</p>
          </div>

          <div className="card feature-card">
            <h3>🟨 Tarjetas y disciplina</h3>
            <p>Controlá amarillas, rojas y comportamiento dentro de cada partido.</p>
          </div>

          <div className="card feature-card">
            <h3>📅 Fixtures y fechas</h3>
            <p>Organizá partidos por jornada y mostrale a todos qué se juega después.</p>
          </div>
        </div>
      </section>

      <section className="cta-section">
        <div className="cta-box">
          <h2>¿Querés organizar tu torneo con FUTAPP?</h2>
          <p>Empezá a centralizar resultados, estadísticas y equipos en una sola plataforma.</p>

          <div className="home-hero-buttons">
            <button type="button" className="home-btn-primary" onClick={crearTorneo}>{primaryLabel}</button>
            <button type="button" className="home-btn-secondary" onClick={irATorneos}>Ver torneos</button>
          </div>
        </div>
      </section>

      {showTournamentForm && (
        <TournamentFormModal
          onClose={() => setShowTournamentForm(false)}
          onSaved={(savedTournament) => {
            setTorneos((current) => [savedTournament, ...current]);
          }}
        />
      )}

    </div>
  );
}

export default Home;
