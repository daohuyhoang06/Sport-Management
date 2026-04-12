const stats = [
  { label: "Users", value: "12,480" },
  { label: "Managers", value: "138" },
  { label: "Bookings", value: "3,912" },
  { label: "Revenue", value: "1.24B VND" },
];

const backendMap = [
  ["GET", "/api/admin/dashboard"],
  ["GET", "/api/admin/revenue/date-range"],
  ["GET", "/api/admin/revenue/monthly"],
];

export default function DashboardPage() {
  return (
    <section className="page-shell">
      <header className="hero">
        <div className="page-meta">
          <span className="badge">Admin module</span>
          <span className="badge">Backend already exists</span>
        </div>
        <h2>Dashboard</h2>
        <p>
          Ngày 1 chỉ dựng khung hiển thị và chừa đúng chỗ cho dữ liệu từ
          backend. Các page khác sẽ gắn vào cùng layout và route này ở các ngày
          sau.
        </p>
      </header>

      <section className="stats-grid" aria-label="Summary stats">
        {stats.map((item) => (
          <article key={item.label} className="stat-card">
            <p>{item.label}</p>
            <h3>{item.value}</h3>
          </article>
        ))}
      </section>

      <section className="grid-two">
        <article className="section-card">
          <div className="section-head">
            <div>
              <h3>What this shell prepares</h3>
              <p>Frontend structure aligned with the backend admin modules.</p>
            </div>
          </div>
          <div className="info-list">
            <div className="info-row">
              <strong>Routing</strong>
              <span>
                /admin/dashboard, /admin/users, /admin/fields, /admin/bookings,
                /admin/employees
              </span>
            </div>
            <div className="info-row">
              <strong>Auth</strong>
              <span>
                Bearer token guard can be added later without changing the
                layout
              </span>
            </div>
            <div className="info-row">
              <strong>Data shape</strong>
              <span>Ready for {"{ success, data }"} backend responses</span>
            </div>
          </div>
        </article>

        <article className="section-card">
          <div className="section-head">
            <div>
              <h3>Backend endpoints to connect next</h3>
              <p>These are already available in your backend.</p>
            </div>
          </div>
          <div className="info-list">
            {backendMap.map(([method, path]) => (
              <div key={path} className="info-row">
                <strong>{method}</strong>
                <span>{path}</span>
              </div>
            ))}
          </div>
        </article>
      </section>
    </section>
  );
}
