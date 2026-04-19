import AdminTable from "../../components/admin/AdminTable";
import EndpointPanel from "../../components/admin/EndpointPanel";
import ListFilters from "../../components/admin/ListFilters";
import PageHero from "../../components/admin/PageHero";
import StatusPill from "../../components/admin/StatusPill";
import useAdminUsers from "../../hooks/useAdminUsers";
import useListFilters from "../../hooks/useListFilters";

const userEndpoints = [
  { method: "GET", path: "/api/admin/users" },
  { method: "GET", path: "/api/admin/users/stats" },
  { method: "GET", path: "/api/admin/users/:id" },
  { method: "PATCH", path: "/api/admin/users/:id/status" },
];

const userColumns = [
  { key: "name", label: "Name" },
  { key: "email", label: "Email" },
  { key: "role", label: "Role" },
  {
    key: "status",
    label: "Status",
    render: (row) => <StatusPill status={row.status} />,
  },
  {
    key: "actions",
    label: "Actions",
    render: () => (
      <div className="table-actions">
        <button type="button" className="btn-secondary">
          View
        </button>
        <button type="button" className="btn-primary">
          Edit
        </button>
      </div>
    ),
  },
];

export default function UsersPage() {
  const { users, stats, loading, error } = useAdminUsers();

  const {
    searchText,
    setSearchText,
    statusFilter,
    setStatusFilter,
    filteredRows,
    filteredCount,
    totalCount,
    hasActiveFilters,
    resetFilters,
  } = useListFilters({
    rows: users,
    searchFields: ["name", "email", "role"],
  });

  return (
    <section className="page-shell">
      <PageHero
        badges={[
          "Admin module",
          "Users",
          loading ? "Loading from backend" : `${stats.total} total users`,
        ]}
        title="Users"
        description="Users page now reads data from backend admin APIs and keeps the same table/filter flow for quick operations."
      />

      <section className="section-card table-card">
        <div className="table-head">
          <h3>User list</h3>
          <button type="button">Add user</button>
        </div>

        {error && <p className="dashboard-state error">{error}</p>}

        <ListFilters
          searchPlaceholder="Search by name, email, or role"
          searchText={searchText}
          onSearchChange={setSearchText}
          statusFilter={statusFilter}
          onStatusChange={setStatusFilter}
          totalCount={totalCount}
          filteredCount={filteredCount}
          hasActiveFilters={hasActiveFilters}
          onResetFilters={resetFilters}
          statusOptions={["active", "inactive"]}
        />

        <AdminTable
          columns={userColumns}
          rows={filteredRows}
          emptyMessage="No users match the current filters."
        />
      </section>

      <EndpointPanel title="Users endpoints" endpoints={userEndpoints} />
    </section>
  );
}
