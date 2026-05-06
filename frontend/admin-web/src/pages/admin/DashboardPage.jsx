import { useMemo, useState } from "react";
import useAdminDashboard from "../../hooks/useAdminDashboard";

function formatDateInput(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function buildConicGradient(items) {
  const total = items.reduce((sum, item) => sum + item.value, 0) || 1;
  let current = 0;

  const segments = items.map((item) => {
    const start = current;
    const end = current + (item.value / total) * 100;
    current = end;
    return `${item.color} ${start}% ${end}%`;
  });

  return `conic-gradient(${segments.join(", ")})`;
}

function StatCard({ accent, icon, label, value, hint }) {
  return (
    <article className="admin-stat-card" style={{ ["--accent-color"]: accent }}>
      <div className="admin-stat-copy">
        <p>{label}</p>
        <h3>{value}</h3>
        <span>{hint}</span>
      </div>
      <div className="admin-stat-icon">{icon}</div>
    </article>
  );
}

function ChartCard({ title, subtitle, totalLabel, items, centerLabel }) {
  const gradient = buildConicGradient(items);

  return (
    <article className="dashboard-panel">
      <div className="dashboard-panel-head">
        <div>
          <h3>{title}</h3>
          <p>{subtitle}</p>
        </div>
      </div>

      <div className="chart-wrap">
        <div className="donut-chart" style={{ background: gradient }}>
          <div className="donut-hole">
            <strong>{centerLabel}</strong>
            <span>{totalLabel}</span>
          </div>
        </div>

        <div className="chart-legend">
          {items.map((item) => (
            <div key={item.label} className="legend-row">
              <span className="legend-dot" style={{ background: item.color }} />
              <div>
                <strong>{item.label}</strong>
                <span>
                  {item.value.toLocaleString("vi-VN")} {item.unit}
                </span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </article>
  );
}

export default function DashboardPage() {
  const { dashboard, monthlyRevenue, loading, error, formatCurrency } =
    useAdminDashboard();

  const today = new Date();
  const [fromDate, setFromDate] = useState(() => {
    const start = new Date(today.getFullYear(), today.getMonth(), 1);
    return formatDateInput(start);
  });
  const [toDate, setToDate] = useState(() => formatDateInput(today));

  const summaryCards = useMemo(
    () => [
      {
        label: "Tổng người dùng",
        value: dashboard.totalUsers.toLocaleString("vi-VN"),
        hint: `${dashboard.activeUsers.toLocaleString("vi-VN")} đang hoạt động`,
        icon: "👥",
        accent: "#1a8f5a",
      },
      {
        label: "Tổng sân bóng",
        value: dashboard.totalFields.toLocaleString("vi-VN"),
        hint: `${dashboard.activeFields.toLocaleString("vi-VN")} sân đang hoạt động`,
        icon: "🏟️",
        accent: "#0f766e",
      },
      {
        label: "Tổng đặt sân",
        value: dashboard.totalBookings.toLocaleString("vi-VN"),
        hint: `${dashboard.todayBookings.toLocaleString("vi-VN")} chờ hôm nay`,
        icon: "📋",
        accent: "#ff8c42",
      },
      {
        label: "Doanh thu",
        value: dashboard.totalRevenue.toLocaleString("vi-VN"),
        hint: "VND (confirmed + completed)",
        icon: "💰",
        accent: "#dff4e9",
      },
    ],
    [dashboard],
  );

  const bookingBreakdown = useMemo(
    () => [
      {
        label: "Đã xác nhận",
        value: dashboard.confirmedBookings,
        color: "#1a8f5a",
        unit: "đặt sân",
      },
      {
        label: "Chờ xác nhận",
        value: dashboard.pendingBookings,
        color: "#ff8c42",
        unit: "đặt sân",
      },
      {
        label: "Hoàn thành",
        value: dashboard.completedBookings,
        color: "#0f766e",
        unit: "đặt sân",
      },
      {
        label: "Đã hủy",
        value: dashboard.cancelledBookings,
        color: "#d8e8df",
        unit: "đặt sân",
      },
    ],
    [dashboard],
  );

  const fieldBreakdown = useMemo(
    () => [
      {
        label: "Hoạt động",
        value: dashboard.activeFields,
        color: "#1a8f5a",
        unit: "sân",
      },
      {
        label: "Không hoạt động",
        value: dashboard.fieldInactive,
        color: "#d8e8df",
        unit: "sân",
      },
      {
        label: "Bảo trì",
        value: dashboard.fieldMaintenance,
        color: "#ff8c42",
        unit: "sân",
      },
    ],
    [dashboard],
  );

  const revenueBars = useMemo(() => {
    const source = monthlyRevenue.slice(-6);
    const maxValue = Math.max(
      ...source.map((item) => Number(item.revenue || 0)),
      1,
    );

    return source.map((item) => ({
      month: `T${item.month}`,
      amount: Number(item.revenue || 0),
      height: Math.max((Number(item.revenue || 0) / maxValue) * 100, 8),
    }));
  }, [monthlyRevenue]);

  return (
    <section className="dashboard-page">
      <header className="dashboard-hero">
        <div className="dashboard-hero-left">
          <div className="dashboard-hero-icon">📊</div>
          <div>
            <p className="dashboard-hero-kicker">Dashboard</p>
            <h2>Tổng Quan Hệ Thống</h2>
          </div>
        </div>

        <div className="dashboard-hero-right">
          <div className="dashboard-role-switcher" aria-label="Vai trò">
            <span className="is-active">👷 Quản trị viên</span>
            <span>📘 Quản lý</span>
            <span>👤 Người dùng</span>
          </div>
          <div className="dashboard-user-chip">
            <span className="dashboard-user-badge">ADMIN</span>
            <strong>Admin</strong>
          </div>
        </div>
      </header>

      <section className="dashboard-toolbar card-surface">
        <label className="date-range-field">
          <span>Khoảng thời gian:</span>
          <input
            type="date"
            value={fromDate}
            onChange={(event) => setFromDate(event.target.value)}
          />
        </label>
        <span className="date-range-separator">đến</span>
        <label className="date-range-field date-range-field-end">
          <input
            type="date"
            value={toDate}
            onChange={(event) => setToDate(event.target.value)}
          />
        </label>
      </section>

      <section className="dashboard-section-title">
        <h3>📊 Tổng Quan Hệ Thống</h3>
      </section>

      <section className="dashboard-stats-grid">
        {summaryCards.map((card) => (
          <StatCard key={card.label} {...card} />
        ))}
      </section>

      {error && <p className="dashboard-state error">{error}</p>}
      {loading && (
        <p className="dashboard-state">Đang tải dữ liệu hệ thống...</p>
      )}

      <section className="dashboard-chart-grid">
        <ChartCard
          title="Trạng Thái Đặt Sân"
          subtitle="Phân bổ theo dữ liệu đặt sân từ backend"
          totalLabel={`${dashboard.totalBookings.toLocaleString("vi-VN")} lượt`}
          centerLabel={`${Math.round((dashboard.confirmedBookings / Math.max(dashboard.totalBookings, 1)) * 100)}%`}
          items={bookingBreakdown}
        />

        <ChartCard
          title="Trạng Thái Sân Bóng"
          subtitle="Phân bổ theo trạng thái sân"
          totalLabel={`${dashboard.totalFields.toLocaleString("vi-VN")} sân`}
          centerLabel={`${Math.round((dashboard.activeFields / Math.max(dashboard.totalFields, 1)) * 100)}%`}
          items={fieldBreakdown}
        />
      </section>

      <section className="dashboard-chart-grid dashboard-chart-grid-bottom">
        <article className="dashboard-panel">
          <div className="dashboard-panel-head">
            <div>
              <h3>Phân Loại Người Dùng</h3>
              <p>Nhóm tài khoản đang hoạt động trong hệ thống</p>
            </div>
          </div>

          <div className="user-bars">
            <div className="bar-group">
              <span className="bar-value">
                {dashboard.totalUsers.toLocaleString("vi-VN")}
              </span>
              <div className="bar-track">
                <div
                  className="bar-fill bar-fill-users"
                  style={{
                    height: `${Math.max(dashboard.totalUsers ? 100 : 12, 12)}%`,
                  }}
                />
              </div>
              <strong>Users</strong>
            </div>
            <div className="bar-group">
              <span className="bar-value">
                {dashboard.totalManagers.toLocaleString("vi-VN")}
              </span>
              <div className="bar-track">
                <div
                  className="bar-fill bar-fill-managers"
                  style={{
                    height: `${Math.max((dashboard.totalManagers / Math.max(dashboard.totalUsers, 1)) * 100, 12)}%`,
                  }}
                />
              </div>
              <strong>Managers</strong>
            </div>
            <div className="bar-group">
              <span className="bar-value">
                {Math.max(
                  dashboard.totalUsers - dashboard.totalManagers,
                  0,
                ).toLocaleString("vi-VN")}
              </span>
              <div className="bar-track">
                <div
                  className="bar-fill bar-fill-admins"
                  style={{
                    height: `${Math.max(((dashboard.totalUsers - dashboard.totalManagers) / Math.max(dashboard.totalUsers, 1)) * 100, 12)}%`,
                  }}
                />
              </div>
              <strong>Admins</strong>
            </div>
          </div>
        </article>

        <article className="dashboard-panel">
          <div className="dashboard-panel-head">
            <div>
              <h3>Doanh Thu Theo Thời Gian</h3>
              <p>Thống kê theo tháng của năm hiện tại</p>
            </div>
          </div>

          <div className="revenue-summary-grid">
            <div className="revenue-stat revenue-stat-primary">
              <span>Tổng doanh thu</span>
              <strong>{dashboard.totalRevenue.toLocaleString("vi-VN")}</strong>
              <small>VND</small>
            </div>
            <div className="revenue-stat revenue-stat-secondary">
              <span>Số lượt đặt</span>
              <strong>{dashboard.totalBookings.toLocaleString("vi-VN")}</strong>
              <small>Bookings</small>
            </div>
            <div className="revenue-stat revenue-stat-light">
              <span>Trung bình/đặt</span>
              <strong>
                {dashboard.totalBookings > 0
                  ? Math.round(
                      dashboard.totalRevenue / dashboard.totalBookings,
                    ).toLocaleString("vi-VN")
                  : 0}
              </strong>
              <small>VND</small>
            </div>
          </div>

          <div className="revenue-bars">
            {revenueBars.length > 0 ? (
              revenueBars.map((item) => (
                <div key={item.month} className="revenue-bar-item">
                  <div className="revenue-bar-track">
                    <div
                      className="revenue-bar-fill"
                      style={{ height: `${item.height}%` }}
                    />
                  </div>
                  <span>{item.month}</span>
                </div>
              ))
            ) : (
              <div className="dashboard-empty-note">
                Chưa có dữ liệu doanh thu theo tháng.
              </div>
            )}
          </div>
        </article>
      </section>

      <section className="dashboard-footer-grid">
        <article className="dashboard-panel dashboard-note-panel">
          <div className="dashboard-panel-head">
            <div>
              <h3>Thời Gian Lọc</h3>
              <p>Khoảng lọc đang hiển thị trên dashboard</p>
            </div>
          </div>
          <div className="note-list">
            <div className="note-row">
              <strong>Từ ngày</strong>
              <span>{fromDate.split("-").reverse().join("/")}</span>
            </div>
            <div className="note-row">
              <strong>Đến ngày</strong>
              <span>{toDate.split("-").reverse().join("/")}</span>
            </div>
            <div className="note-row">
              <strong>Trạng thái</strong>
              <span>Đang đồng bộ với backend</span>
            </div>
          </div>
        </article>

        <article className="dashboard-panel dashboard-note-panel">
          <div className="dashboard-panel-head">
            <div>
              <h3>Ghi Chú Hệ Thống</h3>
              <p>Thông tin tóm tắt từ backend admin</p>
            </div>
          </div>
          <div className="note-list">
            <div className="note-row">
              <strong>Người dùng hoạt động</strong>
              <span>{dashboard.activeUsers.toLocaleString("vi-VN")}</span>
            </div>
            <div className="note-row">
              <strong>Sân đang hoạt động</strong>
              <span>{dashboard.activeFields.toLocaleString("vi-VN")}</span>
            </div>
            <div className="note-row">
              <strong>Đặt sân hôm nay</strong>
              <span>{dashboard.todayBookings.toLocaleString("vi-VN")}</span>
            </div>
          </div>
        </article>
      </section>
    </section>
  );
}
