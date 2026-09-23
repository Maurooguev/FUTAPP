import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import SectionTitle from "../components/SectionTitle/SectionTitle";
import TournamentCard from "../components/TournamentCard/TournamentCard";
import TournamentFormModal from "../components/TournamentFormModal/TournamentFormModal";
import { API_URL, apiFetch } from "../config/api";
import useAuth from "../context/useAuth";
import { tieneRol } from "../utils/roles";

function Home() {
  const [torneos, setTorneos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showTournamentForm, setShowTournamentForm] = useState(false);
  const [loadError, setLoadError] = useState("");
  const [reloadKey, setReloadKey] = useState(0);
  const navigate = useNavigate();
  const { usuario, refreshSession } = useAuth();

  useEffect(() => {
    apiFetch(`${API_URL}/torneos`)
      .then((response) => {
        if (!response.ok) throw new Error("Error al cargar torneos");
        return response.json();
      })
      .then((data) => {
        setTorneos(data);
        setLoadError("");
      })
      .catch(() => setLoadError("No pudimos conectar con el backend. Verificá que FUTAPP esté iniciado."))
      .finally(() => setLoading(false));
  }, [reloadKey]);

  const destacados = useMemo(() => [...torneos]
    .sort((a, b) => (b.visitas || 0) - (a.visitas || 0)
      || Number(b.estado?.toUpperCase() === "ACTIVO") - Number(a.estado?.toUpperCase() === "ACTIVO")
      || (b.idTorneo || 0) - (a.idTorneo || 0))
    .slice(0, 3), [torneos]);

  const torneosOrdenados = useMemo(() => [...torneos]
    .sort((a, b) => Number(b.estado?.toUpperCase() === "ACTIVO") - Number(a.estado?.toUpperCase() === "ACTIVO")
      || (b.idTorneo || 0) - (a.idTorneo || 0)), [torneos]);

  const crearTorneo = () => {
    if (!usuario) return navigate("/register");
    setShowTournamentForm(true);
  };

  const reintentar = () => {
    setLoading(true);
    setLoadError("");
    setReloadKey((value) => value + 1);
  };

  return (
    <div className="home explore-home">
      <section className="explore-heading">
        <div>
          <span>Explorar</span>
          <h1>Todos los torneos</h1>
          <p>Encontrá competencias, equipos y jugadores desde el buscador superior.</p>
        </div>
        <button type="button" className="home-btn-primary" onClick={crearTorneo}>+ Crear torneo</button>
      </section>

      {loadError && (
        <div className="empty-state explore-message" role="alert">
          <h3>FUTAPP todavía no está disponible</h3>
          <p>{loadError}</p>
          <button type="button" className="home-btn-primary" onClick={reintentar}>Reintentar</button>
        </div>
      )}

      {loading && !loadError && <div className="empty-state explore-message">Cargando torneos...</div>}

      {!loading && !loadError && destacados.length > 0 && (
        <section className="section explore-section" id="destacados">
          <SectionTitle chip="Destacados" title="Torneos destacados" description="Los torneos más visitados de FUTAPP." />
          <div className="cards-grid featured-grid">
            {destacados.map((torneo) => (
              <TournamentCard key={torneo.idTorneo} torneo={torneo} variant="featured" onClick={() => navigate(`/torneo/${torneo.idTorneo}`)} />
            ))}
          </div>
        </section>
      )}

      {!loading && !loadError && (
        <section className="section section-soft explore-section" id="todos-torneos">
          <SectionTitle chip="Competencias" title="Todos los torneos" description={`${torneosOrdenados.length} torneos disponibles`} />
          {torneosOrdenados.length === 0 ? (
            <div className="empty-state"><h3>Todavía no hay torneos</h3><p>Podés crear el primero desde esta pantalla.</p></div>
          ) : (
            <div className="cards-grid">
              {torneosOrdenados.map((torneo) => (
                <TournamentCard key={torneo.idTorneo} torneo={torneo} onClick={() => navigate(`/torneo/${torneo.idTorneo}`)} />
              ))}
            </div>
          )}
        </section>
      )}

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
