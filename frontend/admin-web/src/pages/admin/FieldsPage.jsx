export default function FieldsPage() {
  return (
    <section className="page-shell">
      <header className="hero">
        <div className="page-meta">
          <span className="badge">Admin module</span>
          <span className="badge">Fields</span>
        </div>
        <h2>Fields</h2>
        <p>
          Placeholder page for field management. This keeps the route structure
          in sync with the backend before CRUD is added.
        </p>
      </header>

      <section className="section-card empty-state">
        <strong>Backend endpoints ready:</strong> /api/admin/fields,
        /api/admin/fields/stats, /api/admin/fields/:id,
        /api/admin/fields/:id/status, /api/admin/fields/:id/images
      </section>
    </section>
  );
}
