const configuredApiUrl = import.meta.env.VITE_API_URL?.trim();

export const API_URL = (configuredApiUrl || "http://localhost:8080/api/v1").replace(
  /\/$/,
  ""
);

const RETRYABLE_STATUS = new Set([502, 503, 504]);
const inFlightGets = new Map();

const wait = (milliseconds) => new Promise((resolve) => setTimeout(resolve, milliseconds));

export async function apiFetch(url, options = {}) {
  const token = localStorage.getItem("authToken");
  const headers = new Headers(options.headers || {});
  if (token) headers.set("Authorization", `Bearer ${token}`);
  const method = (options.method || "GET").toUpperCase();
  const attempts = method === "GET" ? 3 : 1;

  const execute = async () => {
    for (let attempt = 1; attempt <= attempts; attempt += 1) {
      try {
        const response = await fetch(url, { ...options, headers });
        if (!RETRYABLE_STATUS.has(response.status) || attempt === attempts) return response;
      } catch (error) {
        if (attempt === attempts || error?.name === "AbortError") throw error;
      }
      await wait(attempt * 500);
    }
    throw new Error("No se pudo conectar con FUTAPP");
  };

  if (method !== "GET") return execute();

  const requestKey = `${token || "public"}:${url}`;
  if (!inFlightGets.has(requestKey)) {
    const pending = execute();
    inFlightGets.set(requestKey, pending);
    pending.finally(() => inFlightGets.delete(requestKey));
  }
  return (await inFlightGets.get(requestKey)).clone();
}

export function assetUrl(url) {
  if (!url) return "";
  return url.startsWith("/uploads/") ? `${API_URL}${url}` : url;
}
