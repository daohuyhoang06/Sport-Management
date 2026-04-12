export default function UsersPage() {
  return (
    <section className="page-shell">
      <header className="hero">
        <div className="page-meta">
          <span className="badge">Admin module</span>
          <span className="badge">Users</span>
        </div>
        <h2>Users</h2>
        <p>
          Placeholder page for the user management flow. Day 2 or later will
          plug in table, filters, and API hooks.
        </p>
      </header>

      <section className="section-card empty-state">
        <strong>Backend endpoints ready:</strong> /api/admin/users,
        /api/admin/users/stats, /api/admin/users/:id,
        /api/admin/users/:id/status
      </section>
    </section>
  );
}
