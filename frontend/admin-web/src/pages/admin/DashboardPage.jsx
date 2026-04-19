import EndpointPanel from "../../components/admin/EndpointPanel";
import PageHero from "../../components/admin/PageHero";
import StatsGrid from "../../components/admin/StatsGrid";
import useAdminDashboard from "../../hooks/useAdminDashboard";

const backendEndpoints = [
  { method: "GET", path: "/api/admin/dashboard" },
  { method: "GET", path: "/api/admin/revenue/date-range" },
  { method: "GET", path: "/api/admin/revenue/monthly" },
];

export default function DashboardPage() {
  const { dashboard, monthlyRevenue, stats, loading, error, formatCurrency } =
    useAdminDashboard();

  return (
    <section className="page-shell">
      <PageHero
        badges={[
          "Admin module",
          loading ? "Loading live data" : "Backend connected",
        ]}
        title="Dashboard"
        description="Dashboard admin now reads live backend stats and revenue data so the web shell reflects the same source of truth as the database."
      />

      <StatsGrid stats={stats} />

      <section className="grid-two">
        <article className="section-card">
          <div className="section-head">
            <div>
              <h3>Live summary</h3>
              <p>Aggregated from /api/admin/dashboard.</p>
            </div>
          </div>
          {loading ? (
            <p className="dashboard-state">Loading dashboard data...</p>
          ) : error ? (
            <p className="dashboard-state error">{error}</p>
          ) : (
            <div className="info-list">
              <div className="info-row">
                <strong>Total fields</strong>
                <span>{dashboard.totalFields.toLocaleString("vi-VN")}</span>
              </div>
              <div className="info-row">
                <strong>Active fields</strong>
                <span>{dashboard.activeFields.toLocaleString("vi-VN")}</span>
              </div>
              <div className="info-row">
                <strong>Today's bookings</strong>
                <span>{dashboard.todayBookings.toLocaleString("vi-VN")}</span>
              </div>
              <div className="info-row">
                <strong>Monthly revenue</strong>
                <span>{formatCurrency(dashboard.monthlyRevenue)}</span>
              </div>
            </div>
          )}
        </article>

        <article className="section-card">
          <div className="section-head">
            <div>
              <h3>Monthly revenue snapshot</h3>
              <p>From /api/admin/revenue/monthly for the current year.</p>
            </div>
          </div>
          {loading ? (
            <p className="dashboard-state">Loading revenue timeline...</p>
          ) : error ? (
            <p className="dashboard-state error">{error}</p>
          ) : (
            <div className="info-list">
              {monthlyRevenue.slice(0, 4).map((item) => (
                <div key={item.month} className="info-row">
                  <strong>Month {item.month}</strong>
                  <span>{formatCurrency(item.revenue)}</span>
                </div>
              ))}
            </div>
          )}
        </article>
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

        <EndpointPanel
          title="Backend endpoints to connect next"
          endpoints={backendEndpoints}
        />
      </section>
    </section>
  );
}
