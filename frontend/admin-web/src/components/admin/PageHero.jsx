export default function PageHero({ badges = [], title, description }) {
  return (
    <header className="hero">
      {badges.length > 0 && (
        <div className="page-meta">
          {badges.map((badge) => (
            <span key={badge} className="badge">
              {badge}
            </span>
          ))}
        </div>
      )}
      <h2>{title}</h2>
      <p>{description}</p>
    </header>
  );
}
