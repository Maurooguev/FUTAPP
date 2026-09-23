import { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import SectionTitle from "../components/SectionTitle/SectionTitle";
import TournamentCard from "../components/TournamentCard/TournamentCard";
import TournamentFormModal from "../components/TournamentFormModal/TournamentFormModal";
import { API_URL, apiFetch } from "../config/api";
import useAuth from "../context/useAuth";
import { tieneRol } from "../utils/roles";

const FILTERS = [
  ["TODOS", "Todos"], ["ACTIVO", "Activos"], ["PROXIMAMENTE", "Próximamente"], ["FINALIZADO", "Finalizados"],
  ["LIGA", "Liga"], ["ELIMINACION", "Eliminación directa"], ["ZONAS", "Zonas + playoffs"],
];

const formatDate = (value) => value
  ? new Intl.DateTimeFormat("es-AR", { day: "2-digit", month: "short", hour: "2-digit", minute: "2-digit" }).format(new Date(value))
  : "A confirmar";

const normalizeFilterValue = (value) => (value || "")
  .normalize("NFD")
  .replace(/[\u0300-\u036f]/g, "")
  .trim()
  .toUpperCase();

function MatchActivityCard({ partido, result, onClick }) {
  return (
    <button type="button" className="explore-match-card" onClick={onClick}>
      <span>{partido.nombreTorneo}</span>
      <div><strong>{partido.nombreEquipoLocal}</strong><b>{result ? `${partido.golesLocal} – ${partido.golesVisitante}` : "vs"}</b><strong>{partido.nombreEquipoVisitante}</strong></div>
      <small>{result ? "Finalizado" : formatDate(partido.fechaHora)}</small>
    </button>
  );
}

function Home() {
  const { usuario, refreshSession } = useAuth();
  const [exploreData, setExploreData] = useState({ torneos: [], ultimosResultados: [], proximosPartidos: [], ubicaciones: [] });
  const [favorites, setFavorites] = useState({ equipos: [], torneos: [], jugadores: [] });
  const [loading, setLoading] = useState(true);
  const [activeFilter, setActiveFilter] = useState("TODOS");
  const [sort, setSort] = useState("RELEVANTES");
  const [location, setLocation] = useState(() => window.localStorage.getItem("futapp-explore-location") || "");
  const [showTournamentForm, setShowTournamentForm] = useState(() => Boolean(usuario && new URLSearchParams(window.location.search).get("crearTorneo") === "1"));
  const [loadError, setLoadError] = useState("");
  const [reloadKey, setReloadKey] = useState(0);
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  useEffect(() => {
    if (!usuario || searchParams.get("crearTorneo") !== "1") return;
    const nextParams = new URLSearchParams(searchParams);
    nextParams.delete("crearTorneo");
    setSearchParams(nextParams, { replace: true });
  }, [searchParams, setSearchParams, usuario]);

  useEffect(() => {
    let active = true;
    Promise.all([
      apiFetch(`${API_URL}/explorar`).then((response) => {
        if (!response.ok) throw new Error("Error al cargar Explorar");
        return response.json();
      }),
      usuario
        ? apiFetch(`${API_URL}/favoritos`).then((response) => response.ok ? response.json() : { equipos: [], torneos: [], jugadores: [] })
        : Promise.resolve({ equipos: [], torneos: [], jugadores: [] }),
    ])
      .then(([data, favoriteData]) => {
        if (!active) return;
        setExploreData(data);
        setFavorites(favoriteData);
        setLoadError("");
      })
      .catch(() => { if (active) setLoadError("No pudimos conectar con el backend. Verificá que FUTAPP esté iniciado."); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [reloadKey, usuario]);

  const torneos = useMemo(() => exploreData.torneos || [], [exploreData.torneos]);
  const destacados = useMemo(() => [...torneos]
    .sort((a, b) => (b.visitas || 0) - (a.visitas || 0) || (b.idTorneo || 0) - (a.idTorneo || 0))
    .slice(0, 6), [torneos]);

  const filteredTournaments = useMemo(() => {
    const filtered = torneos.filter((tournament) => {
      const state = normalizeFilterValue(tournament.estado);
      const format = normalizeFilterValue(tournament.formato);
      const sameLocation = !location || normalizeFilterValue(tournament.ubicacion) === normalizeFilterValue(location);
      if (!sameLocation) return false;
      if (activeFilter === "ACTIVO" || activeFilter === "FINALIZADO") return state === activeFilter;
      if (activeFilter === "PROXIMAMENTE") return state !== "FINALIZADO" && Boolean(tournament.proximoPartido);
      if (activeFilter === "LIGA") return format === "LIGA";
      if (activeFilter === "ELIMINACION") return format === "ELIMINACION DIRECTA";
      if (activeFilter === "ZONAS") return format.includes("ZONA") || format.includes("PLAYOFF");
      return true;
    });
    return filtered.sort((a, b) => {
      if (sort === "RECIENTES") return (b.idTorneo || 0) - (a.idTorneo || 0);
      if (sort === "VISITADOS") return (b.visitas || 0) - (a.visitas || 0);
      if (sort === "NOMBRE") return a.nombre.localeCompare(b.nombre, "es");
      if (sort === "PROXIMOS") return (a.proximoPartido?.fechaHora || "9999").localeCompare(b.proximoPartido?.fechaHora || "9999");
      return Number(b.estado === "ACTIVO") - Number(a.estado === "ACTIVO") || (b.visitas || 0) - (a.visitas || 0);
    });
  }, [torneos, activeFilter, sort, location]);

  const hasActiveFilters = activeFilter !== "TODOS" || Boolean(location);
  const activeFilterLabel = FILTERS.find(([value]) => value === activeFilter)?.[1] || "Torneos";

  const personalized = useMemo(() => {
    if (!usuario) return [];
    const favoriteIds = new Set(favorites.torneos || []);
    return torneos.filter((tournament) => favoriteIds.has(tournament.idTorneo) || tournament.idPropietario === usuario.idUsuario).slice(0, 8);
  }, [favorites.torneos, torneos, usuario]);

  const relatedMatches = useMemo(() => {
    if (!usuario) return [];
    const tournamentIds = new Set(favorites.torneos || []);
    const teamIds = new Set(favorites.equipos || []);
    return (exploreData.proximosPartidos || []).filter((match) => tournamentIds.has(match.idTorneo)
      || teamIds.has(match.idEquipoLocal) || teamIds.has(match.idEquipoVisitante)).slice(0, 4);
  }, [exploreData.proximosPartidos, favorites, usuario]);

  const champions = useMemo(() => torneos.filter((tournament) => tournament.idCampeon).slice(0, 6), [torneos]);

  const createTournament = () => {
    if (!usuario) return navigate(`/register?next=${encodeURIComponent("/?crearTorneo=1")}&intent=crear-torneo`);
    setShowTournamentForm(true);
  };
  const retry = () => { setLoading(true); setLoadError(""); setReloadKey((value) => value + 1); };
  const selectLocation = (event) => {
    const value = event.target.value;
    setLocation(value);
    if (value) window.localStorage.setItem("futapp-explore-location", value);
    else window.localStorage.removeItem("futapp-explore-location");
  };

  return (
    <div className="home explore-home">
      <section className="explore-heading">
        <div><span>Explorar</span><h1>Descubrí el fútbol de tu zona</h1><p>Torneos, resultados y próximos partidos en un solo lugar.</p></div>
        <button type="button" className="home-btn-primary" onClick={createTournament}>+ Crear torneo</button>
      </section>

      <section className="explore-controls" aria-label="Filtros de torneos">
        <div className="explore-filter-row">{FILTERS.map(([value, label]) => <button type="button" className={activeFilter === value ? "active" : ""} key={value} onClick={() => setActiveFilter(value)}>{label}</button>)}</div>
        <div className="explore-selects">
          <label>📍 <select value={location} onChange={selectLocation}><option value="">Elegir ubicación</option>{(exploreData.ubicaciones || []).map((item) => <option key={item} value={item}>{item}</option>)}</select></label>
          <label>Ordenar <select value={sort} onChange={(event) => setSort(event.target.value)}><option value="RELEVANTES">Más relevantes</option><option value="RECIENTES">Más recientes</option><option value="VISITADOS">Más visitados</option><option value="PROXIMOS">Próximos a jugar</option><option value="NOMBRE">Por nombre</option></select></label>
        </div>
      </section>

      {loadError && <div className="empty-state explore-message" role="alert"><h3>FUTAPP todavía no está disponible</h3><p>{loadError}</p><button type="button" className="home-btn-primary" onClick={retry}>Reintentar</button></div>}
      {loading && !loadError && <div className="empty-state explore-message">Cargando torneos...</div>}

      {!loading && !loadError && hasActiveFilters && (
        <section className="section section-soft explore-section explore-filter-results" aria-live="polite">
          <SectionTitle chip="Resultados" title={location ? `${activeFilterLabel} en ${location}` : activeFilterLabel} description={`${filteredTournaments.length} ${filteredTournaments.length === 1 ? "torneo" : "torneos"}`} />
          {filteredTournaments.length === 0
            ? <div className="empty-state"><h3>No hay torneos con estos filtros</h3><p>Probá con otra categoría o ubicación.</p><button type="button" onClick={() => { setActiveFilter("TODOS"); setLocation(""); window.localStorage.removeItem("futapp-explore-location"); }}>Limpiar filtros</button></div>
            : <div className="cards-grid">{filteredTournaments.map((tournament) => <TournamentCard key={tournament.idTorneo} torneo={tournament} onClick={() => navigate(`/torneo/${tournament.idTorneo}`)} />)}</div>}
        </section>
      )}

      {!loading && !loadError && !hasActiveFilters && personalized.length > 0 && (
        <section className="section explore-section explore-personal"><SectionTitle chip="Para vos" title="Tus torneos" description="Favoritos y competencias que administrás." /><div className="cards-grid explore-rail">{personalized.map((tournament) => <TournamentCard key={tournament.idTorneo} torneo={tournament} onClick={() => navigate(`/torneo/${tournament.idTorneo}`)} />)}</div>{relatedMatches.length > 0 && <div className="related-matches"><strong>Próximos relacionados</strong>{relatedMatches.map((match) => <MatchActivityCard key={match.idPartido} partido={match} onClick={() => navigate(`/partido/${match.idPartido}`)} />)}</div>}</section>
      )}

      {!loading && !loadError && !hasActiveFilters && destacados.length > 0 && (
        <section className="section explore-section" id="destacados"><SectionTitle chip="Destacados" title="Los más visitados" description="Competencias que están siguiendo los usuarios de FUTAPP." /><div className="cards-grid explore-rail featured-grid">{destacados.map((tournament) => <TournamentCard key={tournament.idTorneo} torneo={tournament} variant="featured" onClick={() => navigate(`/torneo/${tournament.idTorneo}`)} />)}</div></section>
      )}

      {!loading && !loadError && !hasActiveFilters && (
        <section className="section explore-activity-section">
          <SectionTitle chip="Actividad" title="Qué está pasando" description="Resultados recientes y lo próximo que se juega." />
          <div className="explore-activity-grid">
            <div><h3>Últimos resultados</h3>{(exploreData.ultimosResultados || []).slice(0, 5).map((match) => <MatchActivityCard key={match.idPartido} partido={match} result onClick={() => navigate(`/partido/${match.idPartido}`)} />)}</div>
            <div><h3>Próximos partidos</h3>{(exploreData.proximosPartidos || []).slice(0, 5).map((match) => <MatchActivityCard key={match.idPartido} partido={match} onClick={() => navigate(`/partido/${match.idPartido}`)} />)}</div>
          </div>
          {champions.length > 0 && <div className="recent-champions"><h3>Campeones recientes</h3><div>{champions.map((tournament) => <button type="button" key={tournament.idTorneo} onClick={() => navigate(`/torneo/${tournament.idTorneo}`)}><span>🏆</span><strong>{tournament.nombreCampeon}</strong><small>{tournament.nombre}</small></button>)}</div></div>}
        </section>
      )}

      {!loading && !loadError && !hasActiveFilters && (
        <section className="section section-soft explore-section" id="todos-torneos">
          <SectionTitle chip="Competencias" title="Todos los torneos" description={`${filteredTournaments.length} resultados`} />
          {filteredTournaments.length === 0 ? <div className="empty-state"><h3>No hay torneos con este filtro</h3><p>Probá seleccionando otra categoría.</p></div> : <div className="cards-grid">{filteredTournaments.map((tournament) => <TournamentCard key={tournament.idTorneo} torneo={tournament} onClick={() => navigate(`/torneo/${tournament.idTorneo}`)} />)}</div>}
        </section>
      )}

      {showTournamentForm && <TournamentFormModal onClose={() => setShowTournamentForm(false)} onSaved={async (savedTournament) => { setExploreData((current) => ({ ...current, torneos: [savedTournament, ...current.torneos] })); if (!tieneRol(usuario, "ORGANIZADOR")) await refreshSession(); navigate(`/torneo/${savedTournament.idTorneo}`); }} />}
    </div>
  );
}

export default Home;
