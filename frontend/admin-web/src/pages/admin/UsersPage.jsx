import AdminTable from "../../components/admin/AdminTable";
import EndpointPanel from "../../components/admin/EndpointPanel";
import ListFilters from "../../components/admin/ListFilters";
import PageHero from "../../components/admin/PageHero";
import useListFilters from "../../hooks/useListFilters";

const userEndpoints = [
  { method: "GET", path: "/api/admin/users" },
  { method: "GET", path: "/api/admin/users/stats" },
  { method: "GET", path: "/api/admin/users/:id" },
  { method: "PATCH", path: "/api/admin/users/:id/status" },
];

const userRows = [
  {
    id: 1,
    name: "Tran Minh Khang",
    email: "khang.tm@example.com",
    role: "User",
    status: "active",
  },
  {
    id: 2,
    name: "Le Hoang Nhi",
    email: "nhi.lh@example.com",
    role: "Manager",
    status: "pending",
  },
  {
    id: 3,
    name: "Nguyen Phuoc An",
    email: "an.np@example.com",
    role: "User",
    status: "blocked",
  },
];

const userColumns = [
  { key: "name", label: "Name" },
  { key: "email", label: "Email" },
  { key: "role", label: "Role" },
  {
    key: "status",
    label: "Status",
    render: (row) => (
      <span className={`status-pill ${row.status}`}>{row.status}</span>
    ),
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
    rows: userRows,
    searchFields: ["name", "email", "role"],
  });

  return (
    <section className="page-shell">
      <PageHero
        badges={["Admin module", "Users"]}
        title="Users"
        description="Initial users table scaffold. Next steps can attach real API data, filters, and pagination without changing the page structure."
      />

      <section className="section-card table-card">
        <div className="table-head">
          <h3>User list (mock data)</h3>
          <button type="button">Add user</button>
        </div>

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
