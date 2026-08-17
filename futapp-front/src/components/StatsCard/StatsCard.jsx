import "./StatsCard.css";

function StatsCard({ label, value }) {
  return (
    <div className="stats-card">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

export default StatsCard;