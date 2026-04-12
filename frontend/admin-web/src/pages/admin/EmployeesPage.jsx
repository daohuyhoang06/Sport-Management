export default function EmployeesPage() {
  return (
    <section className="page-shell">
      <header className="hero">
        <div className="page-meta">
          <span className="badge">Admin module</span>
          <span className="badge">Employees</span>
        </div>
        <h2>Employees</h2>
        <p>
          Placeholder page for employee management and field assignment. It is
          only the scaffold for the first commit.
        </p>
      </header>

      <section className="section-card empty-state">
        <strong>Backend endpoints ready:</strong> /api/admin/employees,
        /api/admin/employees/stats, /api/admin/employees/:id,
        /api/admin/employees/assign-field
      </section>
    </section>
  );
}
