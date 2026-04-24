import { NavLink, Outlet } from "react-router-dom";

const navItems = [
  { to: "/admin/dashboard", label: "Dashboard", icon: "📊" },
  { to: "/admin/users", label: "Quản lý người dùng", icon: "👥" },
  { to: "/admin/fields", label: "Quản lý sân", icon: "🏟️" },
  { to: "/admin/sport-types", label: "Quản lý loại sân", icon: "🧩" },
  { to: "/admin/bookings", label: "Quản lý đặt sân", icon: "📋" },
  { to: "/admin/employees", label: "Quản lý nhân viên", icon: "👔" },
];

export default function AdminLayout() {
  return (
    <div className="admin-shell">
      <aside className="sidebar">
        <div className="brand-block">
          <p className="brand-kicker">Admin</p>
          <h1 className="brand-title">Panel</h1>
          <p className="brand-subtitle">
            Giao diện quản trị hệ thống sân bóng.
          </p>
          <div className="sidebar-profile">
            <strong>Admin</strong>
            <span>Admin</span>
          </div>
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
          <strong>Điều hướng nhanh</strong>
          <div>Dashboard, người dùng, sân, loại sân, đặt sân, nhân viên</div>
        </div>
      </aside>

      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
