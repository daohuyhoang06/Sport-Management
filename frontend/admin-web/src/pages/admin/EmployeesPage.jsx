import AdminTable from "../../components/admin/AdminTable";
import EndpointPanel from "../../components/admin/EndpointPanel";
import ListFilters from "../../components/admin/ListFilters";
import PageHero from "../../components/admin/PageHero";
import StatusPill from "../../components/admin/StatusPill";
import TableToolbar from "../../components/admin/TableToolbar";
import useAdminEmployees from "../../hooks/useAdminEmployees";
import useListFilters from "../../hooks/useListFilters";

const employeeEndpoints = [
  { method: "GET", path: "/api/admin/employees" },
  { method: "GET", path: "/api/admin/employees/stats" },
  { method: "GET", path: "/api/admin/employees/:id" },
  { method: "POST", path: "/api/admin/employees/assign-field" },
];

const employeeColumns = [
  { key: "name", label: "Name" },
  { key: "email", label: "Email" },
  { key: "role", label: "Role" },
  { key: "assignedField", label: "Assigned field" },
  { key: "phone", label: "Phone" },
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
          Assign
        </button>
        <button type="button" className="btn-primary">
          Update
        </button>
      </div>
    ),
  },
];

export default function EmployeesPage() {
  const { employees, stats, loading, error } = useAdminEmployees();

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
    rows: employees,
    searchFields: ["name", "email", "role", "assignedField", "phone"],
  });

  return (
    <section className="page-shell">
      <PageHero
        badges={[
          "Admin module",
          "Employees",
          loading ? "Loading from backend" : `${stats.total} total employees`,
        ]}
        title="Employees"
        description="Employees page now reads backend manager data and keeps the assign-field oriented table flow for admin operations."
      />

      <section className="section-card table-card">
        <TableToolbar
          title="Employee list"
          subtitle="Live data from /api/admin/employees and /api/admin/employees/stats."
          actionLabel="Add employee"
        />

        {error && <p className="dashboard-state error">{error}</p>}

        <ListFilters
          searchPlaceholder="Search by name, email, role, field, or phone"
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
          columns={employeeColumns}
          rows={filteredRows}
          emptyMessage="No employees match the current filters."
        />
      </section>

      <EndpointPanel
        title="Employees endpoints"
        endpoints={employeeEndpoints}
      />
    </section>
  );
}
