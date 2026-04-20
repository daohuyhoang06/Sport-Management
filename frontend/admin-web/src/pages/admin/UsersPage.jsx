import { useMemo, useState } from "react";
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

export default function UsersPage() {
  const { users, stats, loading, error, toggleUserStatus } = useAdminUsers();
  const [submittingUserId, setSubmittingUserId] = useState(null);
  const [actionError, setActionError] = useState("");
  const [actionSuccess, setActionSuccess] = useState("");

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

  const handleToggleStatus = async (row) => {
    try {
      setSubmittingUserId(row.id);
      setActionError("");
      setActionSuccess("");
      await toggleUserStatus(row.id);
      setActionSuccess(
        `User ${row.name} changed to ${row.status === "active" ? "inactive" : "active"}.`,
      );
    } catch (submitError) {
      setActionError(submitError.message || "Unable to change user status");
    } finally {
      setSubmittingUserId(null);
    }
  };

  const userColumns = useMemo(
    () => [
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
        render: (row) => (
          <div className="table-actions">
            <button
              type="button"
              className="btn-primary"
              onClick={() => handleToggleStatus(row)}
              disabled={submittingUserId === row.id}
            >
              {submittingUserId === row.id
                ? "Updating..."
                : row.status === "active"
                  ? "Deactivate"
                  : "Activate"}
            </button>
          </div>
        ),
      },
    ],
    [submittingUserId],
  );

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
        {actionError && <p className="dashboard-state error">{actionError}</p>}
        {actionSuccess && (
          <p className="dashboard-state success">{actionSuccess}</p>
        )}

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
