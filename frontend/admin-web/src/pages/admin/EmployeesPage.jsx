import AdminTable from "../../components/admin/AdminTable";
import EndpointPanel from "../../components/admin/EndpointPanel";
import ListFilters from "../../components/admin/ListFilters";
import PageHero from "../../components/admin/PageHero";
import TableToolbar from "../../components/admin/TableToolbar";
import useListFilters from "../../hooks/useListFilters";

const employeeEndpoints = [
  { method: "GET", path: "/api/admin/employees" },
  { method: "GET", path: "/api/admin/employees/stats" },
  { method: "GET", path: "/api/admin/employees/:id" },
  { method: "POST", path: "/api/admin/employees/assign-field" },
];

const employeeRows = [
  {
    id: 101,
    name: "Nguyen Tuan Kiet",
    role: "Shift manager",
    assignedField: "San A1",
    phone: "0903 112 889",
    status: "active",
  },
  {
    id: 102,
    name: "Pham Bao Han",
    role: "Reception",
    assignedField: "San B2",
    phone: "0988 224 663",
    status: "pending",
  },
  {
    id: 103,
    name: "Tran Minh Quang",
    role: "Technician",
    assignedField: "San C3",
    phone: "0912 336 775",
    status: "blocked",
  },
];

const employeeColumns = [
  { key: "name", label: "Name" },
  { key: "role", label: "Role" },
  { key: "assignedField", label: "Assigned field" },
  { key: "phone", label: "Phone" },
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
  const {
    searchText,
    setSearchText,
    statusFilter,
    setStatusFilter,
    filteredRows,
  } = useListFilters({
    rows: employeeRows,
    searchFields: ["name", "role", "assignedField", "phone"],
  });

  return (
    <section className="page-shell">
      <PageHero
        badges={["Admin module", "Employees"]}
        title="Employees"
        description="Employee management scaffold with assignment-ready table. Next steps can connect to live filters and assign-field actions."
      />

      <section className="section-card table-card">
        <TableToolbar
          title="Employee list (mock data)"
          subtitle="Track role, assigned field, and status in one place."
          actionLabel="Add employee"
        />

        <ListFilters>
          <input
            type="search"
            placeholder="Search by name, role, field, or phone"
            value={searchText}
            onChange={(event) => setSearchText(event.target.value)}
          />
          <select
            value={statusFilter}
            onChange={(event) => setStatusFilter(event.target.value)}
          >
            <option value="all">All statuses</option>
            <option value="active">Active</option>
            <option value="pending">Pending</option>
            <option value="blocked">Blocked</option>
          </select>
        </ListFilters>

        <AdminTable columns={employeeColumns} rows={filteredRows} />
      </section>

      <EndpointPanel
        title="Employees endpoints"
        endpoints={employeeEndpoints}
      />
    </section>
  );
}
