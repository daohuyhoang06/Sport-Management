import { useEffect, useState } from "react";
import AdminTable from "../../components/admin/AdminTable";
import EndpointPanel from "../../components/admin/EndpointPanel";
import ListFilters from "../../components/admin/ListFilters";
import PageHero from "../../components/admin/PageHero";
import StatusPill from "../../components/admin/StatusPill";
import TableToolbar from "../../components/admin/TableToolbar";
import useAdminEmployees from "../../hooks/useAdminEmployees";
import useAdminFields from "../../hooks/useAdminFields";
import useListFilters from "../../hooks/useListFilters";
import { adminFetch } from "../../services/adminApi";

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
    render: (row) => <EmployeeActions row={row} />,
  },
];

function EmployeeActions({ row }) {
  const handleClick = () => {
    window.dispatchEvent(
      new CustomEvent("admin-assign-field-request", {
        detail: row,
      }),
    );
  };

  return (
    <div className="table-actions">
      <button type="button" className="btn-secondary" onClick={handleClick}>
        Assign field
      </button>
    </div>
  );
}

export default function EmployeesPage() {
  const { employees, stats, loading, error, reload } = useAdminEmployees();
  const {
    fields,
    loading: fieldsLoading,
    error: fieldsError,
  } = useAdminFields();
  const [selectedEmployee, setSelectedEmployee] = useState(null);
  const [selectedFieldId, setSelectedFieldId] = useState("");
  const [assigning, setAssigning] = useState(false);
  const [assignError, setAssignError] = useState("");
  const [assignSuccess, setAssignSuccess] = useState("");

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

  useEffect(() => {
    const onRequestAssignField = (event) => {
      const employee = event.detail;
      setSelectedEmployee(employee);
      setAssignError("");
      setAssignSuccess("");
      setSelectedFieldId(fields[0]?.id ? String(fields[0].id) : "");
    };

    window.addEventListener("admin-assign-field-request", onRequestAssignField);
    return () => {
      window.removeEventListener(
        "admin-assign-field-request",
        onRequestAssignField,
      );
    };
  }, [fields]);

  useEffect(() => {
    if (selectedEmployee && fields.length > 0 && !selectedFieldId) {
      setSelectedFieldId(String(fields[0].id));
    }
  }, [fields, selectedEmployee, selectedFieldId]);

  const closeModal = () => {
    if (assigning) {
      return;
    }

    setSelectedEmployee(null);
    setSelectedFieldId("");
    setAssignError("");
    setAssignSuccess("");
  };

  const handleAssignField = async (event) => {
    event.preventDefault();

    if (!selectedEmployee || !selectedFieldId) {
      setAssignError("Please choose a field first.");
      return;
    }

    try {
      setAssigning(true);
      setAssignError("");
      setAssignSuccess("");

      await adminFetch("/api/admin/employees/assign-field", {
        method: "POST",
        body: JSON.stringify({
          employeeId: selectedEmployee.id,
          fieldId: Number(selectedFieldId),
        }),
      });

      setAssignSuccess("Field assigned successfully.");
      await reload();
      setSelectedEmployee(null);
      setSelectedFieldId("");
    } catch (submitError) {
      setAssignError(submitError.message || "Unable to assign field");
    } finally {
      setAssigning(false);
    }
  };

  return (
    <section className="page-shell">
      <PageHero
        badges={[
          "Admin module",
          "Employees",
          loading ? "Loading from backend" : `${stats.total} total employees`,
        ]}
        title="Employees"
        description="Employees page now reads backend manager data and supports assigning a field directly from the admin table."
      />

      <section className="section-card table-card">
        <TableToolbar
          title="Employee list"
          subtitle="Live data from /api/admin/employees and /api/admin/employees/stats."
          actionLabel="Add employee"
        />

        {(error || fieldsError) && (
          <p className="dashboard-state error">{error || fieldsError}</p>
        )}

        {assignSuccess && (
          <p className="dashboard-state success">{assignSuccess}</p>
        )}

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

      {selectedEmployee && (
        <div className="modal-backdrop" onClick={closeModal}>
          <div
            className="modal-card"
            onClick={(event) => event.stopPropagation()}
          >
            <div className="section-head modal-head">
              <div>
                <h3>Assign field</h3>
                <p>
                  {selectedEmployee.name} - choose a field to attach from the
                  backend list.
                </p>
              </div>
              <button
                type="button"
                className="btn-secondary"
                onClick={closeModal}
                disabled={assigning}
              >
                Close
              </button>
            </div>

            <form className="modal-form" onSubmit={handleAssignField}>
              <label className="modal-field">
                <span>Employee</span>
                <input type="text" value={selectedEmployee.name} readOnly />
              </label>

              <label className="modal-field">
                <span>Field</span>
                <select
                  value={selectedFieldId}
                  onChange={(event) => setSelectedFieldId(event.target.value)}
                  disabled={fieldsLoading}
                >
                  <option value="">
                    {fieldsLoading ? "Loading fields..." : "Choose a field"}
                  </option>
                  {fields.map((field) => (
                    <option key={field.id} value={field.id}>
                      {field.name} - {field.location}
                    </option>
                  ))}
                </select>
              </label>

              {assignError && (
                <p className="dashboard-state error">{assignError}</p>
              )}

              <div className="modal-actions">
                <button
                  type="button"
                  className="btn-secondary"
                  onClick={closeModal}
                  disabled={assigning}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn-primary"
                  disabled={assigning}
                >
                  {assigning ? "Assigning..." : "Assign field"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </section>
  );
}
