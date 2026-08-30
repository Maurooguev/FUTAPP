import { useEffect, useState } from "react";
import { API_URL, apiFetch } from "../../config/api";
import useAuth from "../../context/useAuth";
import "./FavoriteButton.css";

function FavoriteButton({ type, id }) {
  const { usuario } = useAuth();
  const [favorite, setFavorite] = useState(false);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!usuario || !id) return;
    apiFetch(`${API_URL}/favoritos`).then((response) => response.ok ? response.json() : null)
      .then((data) => setFavorite(Boolean(data?.[type]?.includes(Number(id))))).catch(() => null);
  }, [usuario, type, id]);

  if (!usuario) return null;
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
