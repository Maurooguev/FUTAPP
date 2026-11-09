export function formatMatchDate(value) {
  if (!value) return "Horario a confirmar";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "Horario a confirmar";
  return new Intl.DateTimeFormat("es-AR", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);
}
