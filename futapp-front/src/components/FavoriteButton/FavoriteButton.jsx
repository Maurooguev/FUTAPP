import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { API_URL, apiFetch } from "../../config/api";
import useAuth from "../../context/useAuth";
import "./FavoriteButton.css";

function FavoriteButton({ type, id }) {
  const { usuario } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [favorite, setFavorite] = useState(false);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!usuario || !id) return;
    apiFetch(`${API_URL}/favoritos`).then((response) => response.ok ? response.json() : null)
      .then((data) => setFavorite(Boolean(data?.[type]?.includes(Number(id))))).catch(() => null);
  }, [usuario, type, id]);

  if (!usuario) {
    const returnTo = `${location.pathname}${location.search}`;
    return <button type="button" className="favorite-button guest" onClick={() => navigate(`/register?next=${encodeURIComponent(returnTo)}&intent=guardar-favoritos`)} aria-label="Crear una cuenta para guardar en favoritos" title="Creá una cuenta para guardar este perfil"><span>☆</span><small>Guardar</small></button>;
  }
  const toggle = async () => {
    setSaving(true);
    try {
      const response = await apiFetch(`${API_URL}/favoritos/${type}/${id}`, { method: favorite ? "DELETE" : "PUT" });
      if (response.ok) setFavorite((current) => !current);
    } finally { setSaving(false); }
  };

  return <button type="button" className={`favorite-button ${favorite ? "active" : ""}`} onClick={toggle} disabled={saving} aria-pressed={favorite} aria-label={favorite ? "Quitar de favoritos" : "Agregar a favoritos"}><span>{favorite ? "★" : "☆"}</span><small>{favorite ? "Favorito" : "Guardar"}</small></button>;
}

export default FavoriteButton;
