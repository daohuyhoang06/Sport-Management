import EndpointPanel from "../../components/admin/EndpointPanel";
import PageHero from "../../components/admin/PageHero";
import StatsGrid from "../../components/admin/StatsGrid";

const stats = [
  { label: "Users", value: "12,480" },
  { label: "Managers", value: "138" },
  { label: "Bookings", value: "3,912" },
  { label: "Revenue", value: "1.24B VND" },
];

const backendEndpoints = [
  { method: "GET", path: "/api/admin/dashboard" },
  { method: "GET", path: "/api/admin/revenue/date-range" },
  { method: "GET", path: "/api/admin/revenue/monthly" },
];

export default function DashboardPage() {
  return (
    <section className="page-shell">
      <PageHero
        badges={["Admin module", "Backend already exists"]}
        title="Dashboard"
        description="Ngay 1 chi dung khung hien thi va chua dung cho du lieu backend. Cac page khac se gan vao cung layout va route nay o cac ngay sau."
      />

      <StatsGrid stats={stats} />

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
