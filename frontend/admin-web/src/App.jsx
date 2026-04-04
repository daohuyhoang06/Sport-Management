const stats = [
  { label: "Users", value: "12,480" },
  { label: "Managers", value: "138" },
  { label: "Bookings", value: "3,912" },
  { label: "Revenue", value: "1.24B VND" },
];

const pending = [
  {
    id: "MNG-102",
    type: "Manager verify",
    owner: "Arena Alpha",
    status: "Waiting",
  },
  {
    id: "REV-772",
    type: "Review report",
    owner: "User #9321",
    status: "Need review",
  },
  {
    id: "PAY-222",
    type: "Payment dispute",
    owner: "Booking #2190",
    status: "Critical",
  },
];

export default function App() {
  return (
    <div className="admin-shell">
      <aside className="sidebar">
        <h1>Sport Admin</h1>
        <nav>
          <a href="#">Overview</a>
          <a href="#">Users</a>
          <a href="#">Managers</a>
          <a href="#">Fields</a>
          <a href="#">Bookings</a>
          <a href="#">Moderation</a>
        </nav>
      </aside>

      <main className="content">
        <header className="hero">
          <h2>System Control Center</h2>
          <p>
            Monitor user growth, manager operation and moderation queue in one
            place.
          </p>
        </header>

        <section className="stats-grid">
          {stats.map((item) => (
            <article key={item.label} className="card stat-card">
              <p>{item.label}</p>
              <h3>{item.value}</h3>
            </article>
          ))}
        </section>

        <section className="card table-card">
          <div className="table-head">
            <h3>Pending Actions</h3>
            <button type="button">Open moderation</button>
          </div>
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Type</th>
                <th>Owner</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {pending.map((row) => (
                <tr key={row.id}>
                  <td>{row.id}</td>
                  <td>{row.type}</td>
                  <td>{row.owner}</td>
                  <td>{row.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      </main>
    </div>
  );
}
