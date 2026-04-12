export default function EndpointPanel({
  title = "Backend endpoints",
  endpoints = [],
}) {
  return (
    <section className="section-card">
      <div className="section-head">
        <div>
          <h3>{title}</h3>
          <p>Cac endpoint nay da co san o backend, frontend chi can noi vao.</p>
        </div>
      </div>

      <div className="info-list">
        {endpoints.map((endpoint) => (
          <div key={`${endpoint.method}-${endpoint.path}`} className="info-row">
            <strong>{endpoint.method}</strong>
            <span>{endpoint.path}</span>
          </div>
        ))}
      </div>
    </section>
  );
}
