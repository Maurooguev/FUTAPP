import "./HeroHome.css";

function HeroHome({ busqueda, onBusquedaChange }) {
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

        <div className="home-search-box">
          <input
            type="text"
            placeholder="Buscar por código, nombre, ubicación o estado..."
            value={busqueda}
            onChange={(e) => onBusquedaChange(e.target.value)}
          />
          <button className="home-btn-primary">Buscar</button>
        </div>

        <div className="home-hero-buttons">
          <button className="home-btn-primary">Crear torneo</button>
          <button className="home-btn-secondary">Explorar torneos</button>
        </div>
      </div>
    </section>
  );
}

export default HeroHome;
