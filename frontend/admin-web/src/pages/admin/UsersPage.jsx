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
  const { users, stats, loading, error, toggleUserStatus, createUser } =
    useAdminUsers();
  const [submittingUserId, setSubmittingUserId] = useState(null);
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [creatingUser, setCreatingUser] = useState(false);
  const [newUser, setNewUser] = useState({
    person_name: "",
    email: "",
    username: "",
    password: "",
    phone: "",
    address: "",
    birthday: "",
    sex: "",
    role: "user",
    status: "active",
  });
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

  const openCreateModal = () => {
    setCreateModalOpen(true);
    setActionError("");
    setActionSuccess("");
  };

  const closeCreateModal = () => {
    if (creatingUser) {
      return;
    }

    setCreateModalOpen(false);
    setNewUser({
      person_name: "",
      email: "",
      username: "",
      password: "",
      phone: "",
      address: "",
      birthday: "",
      sex: "",
      role: "user",
      status: "active",
    });
  };

  const handleCreateUserChange = (event) => {
    const { name, value } = event.target;

    setNewUser((current) => ({
      ...current,
      [name]: value,
    }));
  };

  const handleCreateUser = async (event) => {
    event.preventDefault();

    if (
      !newUser.person_name.trim() ||
      !newUser.email.trim() ||
      !newUser.username.trim() ||
      !newUser.password
    ) {
      setActionError("Please fill in name, email, username, and password.");
      return;
    }

    try {
      setCreatingUser(true);
      setActionError("");
      setActionSuccess("");

      await createUser({
        ...newUser,
        person_name: newUser.person_name.trim(),
        email: newUser.email.trim(),
        username: newUser.username.trim(),
        password: newUser.password,
        phone: newUser.phone.trim() || null,
        address: newUser.address.trim() || null,
        birthday: newUser.birthday || null,
        sex: newUser.sex || null,
      });

      setActionSuccess(
        `User ${newUser.person_name.trim()} created successfully.`,
      );
      closeCreateModal();
    } catch (submitError) {
      setActionError(submitError.message || "Unable to create user");
    } finally {
      setCreatingUser(false);
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
          <button type="button" onClick={openCreateModal}>
            Add user
          </button>
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

      {createModalOpen && (
        <div className="modal-backdrop" onClick={closeCreateModal}>
          <div
            className="modal-card"
            onClick={(event) => event.stopPropagation()}
          >
            <div className="section-head modal-head">
              <div>
                <h3>Create user</h3>
                <p>
                  Add a new admin-managed person record through the backend.
                </p>
              </div>
              <button
                type="button"
                className="btn-secondary"
                onClick={closeCreateModal}
                disabled={creatingUser}
              >
                Close
              </button>
            </div>

            <form className="modal-form" onSubmit={handleCreateUser}>
              <label className="modal-field">
                <span>Name</span>
                <input
                  type="text"
                  name="person_name"
                  value={newUser.person_name}
                  onChange={handleCreateUserChange}
                  placeholder="Full name"
                />
              </label>

              <label className="modal-field">
                <span>Email</span>
                <input
                  type="email"
                  name="email"
                  value={newUser.email}
                  onChange={handleCreateUserChange}
                  placeholder="Email address"
                />
              </label>

              <label className="modal-field">
                <span>Username</span>
                <input
                  type="text"
                  name="username"
                  value={newUser.username}
                  onChange={handleCreateUserChange}
                  placeholder="Login username"
                />
              </label>

              <label className="modal-field">
                <span>Password</span>
                <input
                  type="password"
                  name="password"
                  value={newUser.password}
                  onChange={handleCreateUserChange}
                  placeholder="Temporary password"
                />
              </label>

              <label className="modal-field">
                <span>Role</span>
                <select
                  name="role"
                  value={newUser.role}
                  onChange={handleCreateUserChange}
                >
                  <option value="user">User</option>
                  <option value="manager">Manager</option>
                  <option value="admin">Admin</option>
                </select>
              </label>

              <label className="modal-field">
                <span>Status</span>
                <select
                  name="status"
                  value={newUser.status}
                  onChange={handleCreateUserChange}
                >
                  <option value="active">Active</option>
                  <option value="inactive">Inactive</option>
                </select>
              </label>

              <label className="modal-field">
                <span>Phone</span>
                <input
                  type="text"
                  name="phone"
                  value={newUser.phone}
                  onChange={handleCreateUserChange}
                  placeholder="Phone number"
                />
              </label>

              <label className="modal-field">
                <span>Birthday</span>
                <input
                  type="date"
                  name="birthday"
                  value={newUser.birthday}
                  onChange={handleCreateUserChange}
                />
              </label>

              <label className="modal-field">
                <span>Sex</span>
                <select
                  name="sex"
                  value={newUser.sex}
                  onChange={handleCreateUserChange}
                >
                  <option value="">Not set</option>
                  <option value="male">Male</option>
                  <option value="female">Female</option>
                  <option value="other">Other</option>
                </select>
              </label>

              <label className="modal-field modal-field-full">
                <span>Address</span>
                <textarea
                  rows="3"
                  name="address"
                  value={newUser.address}
                  onChange={handleCreateUserChange}
                  placeholder="Home or contact address"
                />
              </label>

              <div className="modal-actions modal-actions-wide">
                <button
                  type="button"
                  className="btn-secondary"
                  onClick={closeCreateModal}
                  disabled={creatingUser}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn-primary"
                  disabled={creatingUser}
                >
                  {creatingUser ? "Creating..." : "Create user"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </section>
  );
}
