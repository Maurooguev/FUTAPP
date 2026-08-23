import "./HeroHome.css";

function HeroHome({ busqueda, onBusquedaChange, onSearch, onCreate, onExplore, primaryLabel = "Crear torneo" }) {
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

        <form className="home-search-box" onSubmit={(event) => { event.preventDefault(); onSearch(); }}>
          <input
            type="text"
            placeholder="Buscar por código, nombre, ubicación o estado..."
            value={busqueda}
            onChange={(e) => onBusquedaChange(e.target.value)}
          />
          <button type="submit" className="home-btn-primary">Buscar</button>
        </form>

        <div className="home-hero-buttons">
          <button type="button" className="home-btn-primary" onClick={onCreate}>{primaryLabel}</button>
          <button type="button" className="home-btn-secondary" onClick={onExplore}>Explorar torneos</button>
        </div>
      </div>
    </section>
  );
}

export default HeroHome;
