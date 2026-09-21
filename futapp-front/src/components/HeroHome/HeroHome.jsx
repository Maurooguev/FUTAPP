import "./HeroHome.css";
import { assetUrl } from "../../config/api";

const grupos = [
  { key: "torneos", label: "Torneos", tipo: "torneo", icono: "🏆" },
  { key: "equipos", label: "Equipos", tipo: "equipo", icono: "🛡️" },
  { key: "jugadores", label: "Jugadores", tipo: "jugador", icono: "⚽" },
];

function HeroHome({ busqueda, onBusquedaChange, onSearch, onCreate, onExplore, resultados,
  buscando, errorBusqueda, onResultClick, primaryLabel = "Crear torneo" }) {
  const mostrarResultados = busqueda.trim().length >= 2;
  const totalResultados = grupos.reduce((total, grupo) => total + (resultados?.[grupo.key]?.length || 0), 0);
  return (
    <section className="home-hero" id="inicio">
      <div className="home-hero-overlay" />

      <div className="home-hero-content">
        <span className="home-hero-chip">Plataforma para torneos amateur</span>

        <h1>
          Organizá torneos,
          <br />
          resultados y estadísticas
          <br />
          en un solo lugar
        </h1>

        <p>
          FUTAPP te permite administrar torneos de fútbol amateur con tablas
          automáticas, goleadores, tarjetas, fixtures y resultados actualizados.
        </p>

        <div className="home-search-wrapper">
          <form className="home-search-box" onSubmit={(event) => { event.preventDefault(); onSearch(); }}>
            <input
              type="search"
              placeholder="Buscar torneos, equipos o jugadores..."
              aria-label="Buscar torneos, equipos o jugadores"
              value={busqueda}
              onChange={(e) => onBusquedaChange(e.target.value)}
            />
            <button type="submit" className="home-btn-primary">Buscar</button>
          </form>

          {mostrarResultados && (
            <div className="global-search-results" aria-live="polite">
              {buscando && <div className="global-search-status">Buscando...</div>}
              {!buscando && errorBusqueda && <div className="global-search-status error">{errorBusqueda}</div>}
              {!buscando && !errorBusqueda && totalResultados === 0 && (
                <div className="global-search-status">No encontramos resultados para “{busqueda.trim()}”.</div>
              )}
              {!buscando && !errorBusqueda && grupos.map((grupo) => {
                const items = resultados?.[grupo.key] || [];
                if (items.length === 0) return null;
                return (
                  <section className="global-search-group" key={grupo.key}>
                    <h2>{grupo.label}</h2>
                    <div>
                      {items.map((item) => (
                        <button type="button" key={`${grupo.tipo}-${item.id}`} onClick={() => onResultClick(grupo.tipo, item.id)}>
                          <span className="global-search-avatar">
                            {item.imagen ? <img src={assetUrl(item.imagen)} alt="" /> : grupo.icono}
                          </span>
                          <span className="global-search-copy">
                            <strong>{item.nombre}</strong>
                            <small>{item.subtitulo || item.codigoPublico}</small>
                          </span>
                          <span className="global-search-code">{item.codigoPublico}</span>
                          <span className="global-search-arrow" aria-hidden="true">→</span>
                        </button>
                      ))}
                    </div>
                  </section>
                );
              })}
            </div>
          )}
        </div>

        <div className="home-hero-buttons">
          <button type="button" className="home-btn-primary" onClick={onCreate}>{primaryLabel}</button>
          <button type="button" className="home-btn-secondary" onClick={onExplore}>Explorar torneos</button>
        </div>
      </div>
    </section>
  );
}

export default HeroHome;
