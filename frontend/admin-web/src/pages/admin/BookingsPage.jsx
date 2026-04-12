export default function BookingsPage() {
  return (
    <section className="page-shell">
      <header className="hero">
        <div className="page-meta">
          <span className="badge">Admin module</span>
          <span className="badge">Bookings</span>
        </div>
        <h2>Bookings</h2>
        <p>
          Placeholder page for booking control. The future UI will connect to
          the existing backend endpoints without changing this shell.
        </p>
      </header>

      <section className="section-card empty-state">
        <strong>Backend endpoints ready:</strong> /api/admin/bookings,
        /api/admin/bookings/stats, /api/admin/bookings/date-range,
        /api/admin/bookings/:id, /api/admin/bookings/:id/status,
        /api/admin/bookings/:id/cancel
      </section>
    </section>
  );
}
