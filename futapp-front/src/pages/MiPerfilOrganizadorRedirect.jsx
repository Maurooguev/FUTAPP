import { useEffect, useState } from "react";
import { Navigate } from "react-router-dom";
import { API_URL, apiFetch } from "../config/api";

function MiPerfilOrganizadorRedirect() {
  const [destination, setDestination] = useState(null);

  useEffect(() => {
    apiFetch(`${API_URL}/perfiles-organizador/mio`)
      .then(async (response) => {
        if (response.status === 404) { setDestination("/mi-perfil-organizador/editar"); return; }
        const body = await response.json().catch(() => null);
        if (!response.ok || !body?.idUsuario) { setDestination("/mi-perfil-organizador/editar"); return; }
        setDestination(`/organizador/${body.idUsuario}`);
      })
      .catch(() => setDestination("/mi-perfil-organizador/editar"));
  }, []);

  return destination
    ? <Navigate to={destination} replace />
    : <main className="organizer-profile-page"><div className="organizer-profile-message">Abriendo tu perfil…</div></main>;
}

export default MiPerfilOrganizadorRedirect;
