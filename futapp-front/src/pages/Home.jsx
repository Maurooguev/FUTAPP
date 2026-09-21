import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import HeroHome from "../components/HeroHome/HeroHome";
import SectionTitle from "../components/SectionTitle/SectionTitle";
import TournamentCard from "../components/TournamentCard/TournamentCard";
import TournamentFormModal from "../components/TournamentFormModal/TournamentFormModal";
import { API_URL, apiFetch } from "../config/api";
import useAuth from "../context/useAuth";
import { tieneRol } from "../utils/roles";

function Home() {
  const [torneos, setTorneos] = useState([]);
  const [busqueda, setBusqueda] = useState("");
  const [resultadosBusqueda, setResultadosBusqueda] = useState({ torneos: [], equipos: [], jugadores: [] });
  const [buscando, setBuscando] = useState(false);
  const [errorBusqueda, setErrorBusqueda] = useState("");
  const [showTournamentForm, setShowTournamentForm] = useState(false);
  const [loadError, setLoadError] = useState("");
  const [reloadKey, setReloadKey] = useState(0);
  const navigate = useNavigate();
  const { usuario, refreshSession } = useAuth();

  useEffect(() => {
    apiFetch(`${API_URL}/torneos`)
      .then((res) => {
        if (!res.ok) throw new Error("Error al cargar torneos");
        return res.json();
      })
      .then((data) => { setTorneos(data); setLoadError(""); })
      .catch(() => setLoadError("No pudimos conectar con el backend. Verificá que FUTAPP esté iniciado."));
  }, [reloadKey]);

  useEffect(() => {
    const termino = busqueda.trim();
    if (termino.length < 2) {
      setResultadosBusqueda({ torneos: [], equipos: [], jugadores: [] });
      setErrorBusqueda("");
      setBuscando(false);
      return undefined;
    }
    let vigente = true;
    setBuscando(true);
    const timer = window.setTimeout(async () => {
      setErrorBusqueda("");
      try {
        const response = await apiFetch(`${API_URL}/busqueda?q=${encodeURIComponent(termino)}`);
        if (!response.ok) throw new Error("No se pudo buscar");
        const data = await response.json();
        if (vigente) setResultadosBusqueda(data);
      } catch {
        if (vigente) setErrorBusqueda("No pudimos completar la búsqueda.");
      } finally {
        if (vigente) setBuscando(false);
      }
    }, 250);
    return () => {
      vigente = false;
      window.clearTimeout(timer);
    };
  }, [busqueda]);

  const destacados = useMemo(() => [...torneos]
    .sort((a, b) => (b.visitas || 0) - (a.visitas || 0)
      || Number(b.estado?.toUpperCase() === "ACTIVO") - Number(a.estado?.toUpperCase() === "ACTIVO")
      || (b.idTorneo || 0) - (a.idTorneo || 0))
    .slice(0, 3), [torneos]);

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

  const buscar = () => {
    if (busqueda.trim().length < 2) irATorneos();
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
        onSearch={buscar}
        resultados={resultadosBusqueda}
        buscando={buscando}
        errorBusqueda={errorBusqueda}
        onResultClick={(tipo, id) => navigate(`/${tipo}/${id}`)}
        onCreate={crearTorneo}
        onExplore={irATorneos}
        primaryLabel={primaryLabel}
      />

      {loadError && (
        <div className="empty-state" role="alert">
          <h3>FUTAPP todavía no está disponible</h3>
          <p>{loadError}</p>
          <button type="button" className="home-btn-primary" onClick={() => setReloadKey((value) => value + 1)}>Reintentar</button>
        </div>
      )}

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

        {!loadError && torneos.length === 0 ? (
          <div className="empty-state">
            <h3>No encontramos torneos</h3>
            <p>Probá con otro nombre, estado o ubicación.</p>
          </div>
        ) : (
          <div className="cards-grid">
            {torneos.map((torneo) => (
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
          onSaved={async (savedTournament) => {
            setTorneos((current) => [savedTournament, ...current]);
            if (!tieneRol(usuario, "ORGANIZADOR")) await refreshSession();
            navigate(`/torneo/${savedTournament.idTorneo}`);
          }}
        />
      )}

    </div>
  );
}

export default Home;
