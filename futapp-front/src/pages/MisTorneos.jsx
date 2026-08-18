import { useEffect, useState } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import { API_URL, apiFetch } from "../config/api";
import useAuth from "../context/useAuth";
import TournamentCard from "../components/TournamentCard/TournamentCard";

function MisTorneos() {
  const { usuario } = useAuth();
  const navigate = useNavigate();
  const [torneos, setTorneos] = useState([]);
  const [loading, setLoading] = useState(true);
  useEffect(() => {
    if (!usuario) return;
    apiFetch(`${API_URL}/torneos/mios`).then((response) => response.json()).then(setTorneos).finally(() => setLoading(false));
  }, [usuario]);
  if (!usuario) return <Navigate to="/login" replace />;
  return <main className="my-tournaments-page"><div className="my-tournaments-shell"><span className="tournament-form-chip">Panel del organizador</span><h1>Mis torneos</h1><p>Administrá tus competencias y compartí su código público.</p>{loading ? <p>Cargando...</p> : torneos.length === 0 ? <div className="team-empty"><h3>Todavía no creaste torneos</h3><p>Creá el primero desde la página de inicio.</p></div> : <div className="cards-grid">{torneos.map((torneo) => <TournamentCard key={torneo.idTorneo} torneo={torneo} onClick={() => navigate(`/torneo/${torneo.idTorneo}`)} />)}</div>}</div></main>;
}
export default MisTorneos;
