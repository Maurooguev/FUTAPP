import { useEffect, useRef, useState } from "react";
import "./ArgentinaLocationInput.css";

const GEOREF_URL = "https://apis.datos.gob.ar/georef/api/v2.0";
const CABA_NEIGHBORHOODS = [
  "Agronomía", "Almagro", "Balvanera", "Barracas", "Belgrano", "Boedo", "Caballito", "Chacarita",
  "Coghlan", "Colegiales", "Constitución", "Flores", "Floresta", "La Boca", "La Paternal", "Liniers",
  "Mataderos", "Monserrat", "Monte Castro", "Nueva Pompeya", "Núñez", "Palermo", "Parque Avellaneda",
  "Parque Chacabuco", "Parque Chas", "Parque Patricios", "Puerto Madero", "Recoleta", "Retiro",
  "Saavedra", "San Cristóbal", "San Nicolás", "San Telmo", "Vélez Sarsfield", "Versalles", "Villa Crespo",
  "Villa del Parque", "Villa Devoto", "Villa General Mitre", "Villa Lugano", "Villa Luro", "Villa Ortúzar",
  "Villa Pueyrredón", "Villa Real", "Villa Riachuelo", "Villa Santa Rita", "Villa Soldati", "Villa Urquiza",
];

function locationLabel(item) {
  const area = item.departamento?.nombre || item.municipio?.nombre;
  const parts = [item.nombre, area, item.provincia?.nombre].filter(Boolean);
  return [...new Set(parts)].join(", ");
}

async function fetchLocations(query, signal) {
  const params = new URLSearchParams({ nombre: query, max: "6", campos: "nombre,provincia.nombre,departamento.nombre" });
  const endpoints = ["localidades", "asentamientos"];
  const responses = await Promise.allSettled(endpoints.map(async (resource) => {
    const response = await fetch(`${GEOREF_URL}/${resource}?${params}`, { signal });
    if (!response.ok) throw new Error("GeoRef no disponible");
    const data = await response.json();
    return data[resource] || [];
  }));
  const items = responses.flatMap((result) => result.status === "fulfilled" ? result.value : []);
  const normalizedQuery = query.toLocaleLowerCase("es-AR");
  const cabaMatches = CABA_NEIGHBORHOODS
    .filter((name) => name.toLocaleLowerCase("es-AR").includes(normalizedQuery))
    .map((name) => `${name}, Ciudad Autónoma de Buenos Aires`);
  if (responses.every((result) => result.status === "rejected") && cabaMatches.length === 0) throw new Error("No pudimos consultar las ubicaciones oficiales");
  return [...new Set([...cabaMatches, ...items.map(locationLabel)])].slice(0, 8);
}

function ArgentinaLocationInput({ value, onChange, onSelect, required }) {
  const [suggestions, setSuggestions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [open, setOpen] = useState(false);
  const containerRef = useRef(null);
  const selectedValueRef = useRef("");

  useEffect(() => {
    const close = (event) => {
      if (!containerRef.current?.contains(event.target)) setOpen(false);
    };
    document.addEventListener("mousedown", close);
    return () => document.removeEventListener("mousedown", close);
  }, []);

  useEffect(() => {
    const query = value.trim();
    if (query === selectedValueRef.current) return undefined;
    if (query.length < 2) {
      setSuggestions([]);
      setMessage(query ? "Escribí al menos 2 letras." : "");
      return undefined;
    }
    const controller = new AbortController();
    const timer = window.setTimeout(async () => {
      setLoading(true);
      setMessage("");
      try {
        const results = await fetchLocations(query, controller.signal);
        setSuggestions(results);
        setMessage(results.length ? "" : "No encontramos una localidad con ese nombre.");
        setOpen(true);
      } catch (error) {
        if (error.name !== "AbortError") {
          setSuggestions([]);
          setMessage(error.message);
          setOpen(true);
        }
      } finally {
        if (!controller.signal.aborted) setLoading(false);
      }
    }, 300);
    return () => { window.clearTimeout(timer); controller.abort(); };
  }, [value]);

  return <div className="location-autocomplete" ref={containerRef}>
    <input
      name="ubicacion"
      value={value}
      onChange={(event) => { selectedValueRef.current = ""; onChange(event.target.value); setOpen(true); }}
      onFocus={() => value.trim().length >= 2 && setOpen(true)}
      maxLength="120"
      placeholder="Escribí una ciudad o barrio"
      autoComplete="off"
      required={required}
      aria-autocomplete="list"
      aria-expanded={open}
    />
    {loading && <span className="location-loading">Buscando…</span>}
    {open && (suggestions.length > 0 || message) && <div className="location-suggestions" role="listbox">
      {suggestions.map((suggestion) => <button type="button" role="option" key={suggestion} onClick={() => { selectedValueRef.current = suggestion; onSelect(suggestion); setOpen(false); }}>
        <span>📍</span><strong>{suggestion}</strong>
      </button>)}
      {message && <p>{message}</p>}
    </div>}
  </div>;
}

export default ArgentinaLocationInput;
