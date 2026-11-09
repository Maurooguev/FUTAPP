const enabled = (value) => String(value).toLowerCase() === "true";

export const FEATURES = Object.freeze({
  playerAccounts: enabled(import.meta.env.VITE_FEATURE_PLAYER_ACCOUNTS),
  teamDirectorAccounts: enabled(import.meta.env.VITE_FEATURE_TEAM_DIRECTOR_ACCOUNTS),
  claims: enabled(import.meta.env.VITE_FEATURE_CLAIMS),
  notifications: enabled(import.meta.env.VITE_FEATURE_NOTIFICATIONS),
  favorites: enabled(import.meta.env.VITE_FEATURE_FAVORITES),
});
