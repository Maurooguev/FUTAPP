const configuredApiUrl = import.meta.env.VITE_API_URL?.trim();

export const API_URL = (configuredApiUrl || "http://localhost:8080/api/v1").replace(
  /\/$/,
  ""
);

export function apiFetch(url, options = {}) {
  const token = localStorage.getItem("authToken");
  const headers = new Headers(options.headers || {});
  if (token) headers.set("Authorization", `Bearer ${token}`);
  return fetch(url, { ...options, headers });
}

export function assetUrl(url) {
  if (!url) return "";
  return url.startsWith("/uploads/") ? `${API_URL}${url}` : url;
}
