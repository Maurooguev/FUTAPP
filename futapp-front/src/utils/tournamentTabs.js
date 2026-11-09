export const TOURNAMENT_TABS = new Set(["resumen", "info", "tabla", "equipos", "stats", "fixture"]);

export function tournamentTabFromHash(hash) {
  const requested = String(hash || "").replace(/^#/, "").toLowerCase();
  return TOURNAMENT_TABS.has(requested) ? requested : "resumen";
}
