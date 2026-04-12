import { NavLink, Outlet } from "react-router-dom";

const navItems = [
  { to: "/admin/dashboard", label: "Dashboard", icon: "📊" },
  { to: "/admin/users", label: "Users", icon: "👥" },
  { to: "/admin/fields", label: "Fields", icon: "🏟️" },
  { to: "/admin/bookings", label: "Bookings", icon: "📋" },
  { to: "/admin/employees", label: "Employees", icon: "👔" },
];

export default function AdminLayout() {
  return (
    <div className="admin-shell">
      <aside className="sidebar">
        <div className="brand-block">
          <p className="brand-kicker">Sport Management</p>
          <h1 className="brand-title">Admin Web</h1>
          <p className="brand-subtitle">
            Day 1 skeleton for the frontend that matches the backend admin
            modules.
          </p>
        </div>

        <nav className="nav-stack" aria-label="Admin navigation">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `nav-link${isActive ? " active" : ""}`
              }
            >
              <span className="nav-link-icon">{item.icon}</span>
              <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-footer">
          <strong>Backend-ready routes</strong>
          <div>Dashboard, Users, Fields, Bookings, Employees</div>
        </div>
      </aside>

      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
