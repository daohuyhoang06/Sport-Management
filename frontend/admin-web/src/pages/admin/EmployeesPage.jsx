import { useEffect, useMemo, useState } from "react";
import AdminTable from "../../components/admin/AdminTable";
import ListFilters from "../../components/admin/ListFilters";
import PageHero from "../../components/admin/PageHero";
import StatusPill from "../../components/admin/StatusPill";
import TableToolbar from "../../components/admin/TableToolbar";
import useAdminEmployees from "../../hooks/useAdminEmployees";
import useAdminFields from "../../hooks/useAdminFields";
import useListFilters from "../../hooks/useListFilters";
import { adminFetch } from "../../services/adminApi";

export default function EmployeesPage() {
  const {
    employees,
    stats,
    loading,
    error,
    reload,
    createEmployee,
    deleteEmployee,
    updateEmployee,
    getEmployeeById,
  } = useAdminEmployees();
  const {
    fields,
    loading: fieldsLoading,
    error: fieldsError,
  } = useAdminFields();
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [creatingEmployee, setCreatingEmployee] = useState(false);
  const [newEmployee, setNewEmployee] = useState({
    person_name: "",
    email: "",
    username: "",
    password: "",
    phone: "",
    address: "",
    birthday: "",
    sex: "",
    status: "active",
  });
  const [selectedEmployee, setSelectedEmployee] = useState(null);
  const [selectedfield_id, setSelectedfield_id] = useState("");
  const [assigning, setAssigning] = useState(false);
  const [deactivatingEmployeeId, setDeactivatingEmployeeId] = useState(null);
  const [loadingEditEmployeeId, setLoadingEditEmployeeId] = useState(null);
  const [editingEmployeeId, setEditingEmployeeId] = useState(null);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [editForm, setEditForm] = useState({
    person_name: "",
    email: "",
    phone: "",
    address: "",
    birthday: "",
    sex: "",
    status: "active",
  });
  const [assignError, setAssignError] = useState("");
  const [assignSuccess, setAssignSuccess] = useState("");
  const [createError, setCreateError] = useState("");
  const [editError, setEditError] = useState("");

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
    if (selectedEmployee && fields.length > 0 && !selectedfield_id) {
      setSelectedfield_id(String(fields[0].id));
    }
  }, [fields, selectedEmployee, selectedfield_id]);

  const openAssignModal = (employee) => {
    setSelectedEmployee(employee);
    setAssignError("");
    setAssignSuccess("");
    setSelectedfield_id(fields[0]?.id ? String(fields[0].id) : "");
  };

  const openCreateModal = () => {
    setCreateModalOpen(true);
    setCreateError("");
    setEditError("");
    setAssignSuccess("");
  };

  const openEditModal = async (employee) => {
    try {
      setLoadingEditEmployeeId(employee.id);
      setEditError("");
      setCreateError("");
      setAssignError("");
      setAssignSuccess("");

      const details = await getEmployeeById(employee.id);

      setEditForm({
        person_name:
          details?.name || details?.person_name || employee.name || "",
        email:
          details?.email ||
          (employee.email && employee.email !== "-" ? employee.email : ""),
        phone:
          details?.phone ||
          (employee.phone && employee.phone !== "-" ? employee.phone : ""),
        address: details?.address || "",
        birthday: details?.birthday || "",
        sex: details?.sex || "",
        status: details?.status || employee.status || "active",
      });
      setEditingEmployeeId(employee.id);
      setEditModalOpen(true);
    } catch (submitError) {
      setEditError(submitError.message || "Unable to load employee details");
    } finally {
      setLoadingEditEmployeeId(null);
    }
  };

  const closeCreateModal = () => {
    if (creatingEmployee) {
      return;
    }

    setCreateModalOpen(false);
    setCreateError("");
    setNewEmployee({
      person_name: "",
      email: "",
      username: "",
      password: "",
      phone: "",
      address: "",
      birthday: "",
      sex: "",
      status: "active",
    });
  };

  const closeEditModal = () => {
    if (editingEmployeeId && deactivatingEmployeeId === editingEmployeeId) {
      return;
    }

    setEditModalOpen(false);
    setEditingEmployeeId(null);
    setEditError("");
    setEditForm({
      person_name: "",
      email: "",
      phone: "",
      address: "",
      birthday: "",
      sex: "",
      status: "active",
    });
  };

  const handleCreateEmployeeChange = (event) => {
    const { name, value } = event.target;
    setNewEmployee((current) => ({
      ...current,
      [name]: value,
    }));
  };

  const handleEditChange = (event) => {
    const { name, value } = event.target;
    setEditForm((current) => ({
      ...current,
      [name]: value,
    }));
  };

  const handleCreateEmployee = async (event) => {
    event.preventDefault();

    if (
      !newEmployee.person_name.trim() ||
      !newEmployee.username.trim() ||
      !newEmployee.password
    ) {
      setCreateError("Please fill in name, username, and password.");
      return;
    }

    if (newEmployee.phone && !/^[0-9]{10}$/.test(newEmployee.phone.trim())) {
      setCreateError("Phone number must contain exactly 10 digits.");
      return;
    }

    try {
      setCreatingEmployee(true);
      setCreateError("");
      setAssignSuccess("");

      await createEmployee({
        name: newEmployee.person_name.trim(),
        email: newEmployee.email.trim() || null,
        username: newEmployee.username.trim(),
        password: newEmployee.password,
        phone: newEmployee.phone.trim() || null,
        address: newEmployee.address.trim() || null,
        birthday: newEmployee.birthday || null,
        sex: newEmployee.sex || null,
        status: newEmployee.status,
      });

      setAssignSuccess(
        `Employee ${newEmployee.person_name.trim()} created successfully.`,
      );
      closeCreateModal();
    } catch (submitError) {
      setCreateError(submitError.message || "Unable to create employee");
    } finally {
      setCreatingEmployee(false);
    }
  };

  const closeModal = () => {
    if (assigning) {
      return;
    }

    setSelectedEmployee(null);
    setSelectedfield_id("");
    setAssignError("");
    setAssignSuccess("");
  };

  const handleAssignField = async (event) => {
    event.preventDefault();

    if (!selectedEmployee || !selectedfield_id) {
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
          field_id: Number(selectedfield_id),
        }),
      });

      setAssignSuccess("Field assigned successfully.");
      await reload();
      setSelectedEmployee(null);
      setSelectedfield_id("");
    } catch (submitError) {
      setAssignError(submitError.message || "Unable to assign field");
    } finally {
      setAssigning(false);
    }
  };

  const handleUpdateEmployee = async (event) => {
    event.preventDefault();

    if (!editingEmployeeId) {
      return;
    }

    if (!editForm.person_name.trim()) {
      setEditError("Please fill in employee name.");
      return;
    }

    if (editForm.phone && !/^[0-9]{10}$/.test(editForm.phone.trim())) {
      setEditError("Phone number must contain exactly 10 digits.");
      return;
    }

    try {
      setEditError("");
      setCreateError("");
      setAssignError("");
      setAssignSuccess("");
      setDeactivatingEmployeeId(editingEmployeeId);

      await updateEmployee(editingEmployeeId, {
        name: editForm.person_name.trim(),
        email: editForm.email.trim() || null,
        phone: editForm.phone.trim() || null,
        address: editForm.address.trim() || null,
        birthday: editForm.birthday || null,
        sex: editForm.sex || null,
        status: editForm.status,
      });

      setAssignSuccess("Employee updated successfully.");
      closeEditModal();
    } catch (submitError) {
      setEditError(submitError.message || "Unable to update employee");
    } finally {
      setDeactivatingEmployeeId(null);
    }
  };

  const handleDeactivateEmployee = async (employee) => {
    if (employee.status === "inactive") {
      return;
    }

    const confirmed = window.confirm(
      `Deactivate employee ${employee.name}? This will set status to inactive.`,
    );

    if (!confirmed) {
      return;
    }

    try {
      setDeactivatingEmployeeId(employee.id);
      setCreateError("");
      setAssignError("");
      setAssignSuccess("");

      await deleteEmployee(employee.id);

      setAssignSuccess(`Employee ${employee.name} was deactivated.`);
    } catch (submitError) {
      setAssignError(submitError.message || "Unable to deactivate employee");
    } finally {
      setDeactivatingEmployeeId(null);
    }
  };

  const employeeColumns = useMemo(
    () => [
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
        render: (row) => (
          <div className="table-actions booking-actions">
            <button
              type="button"
              className="btn-secondary"
              onClick={() => openEditModal(row)}
              disabled={
                deactivatingEmployeeId === row.id ||
                loadingEditEmployeeId === row.id
              }
            >
              {loadingEditEmployeeId === row.id ? "Loading..." : "Edit"}
            </button>
            <button
              type="button"
              className="btn-secondary"
              onClick={() => openAssignModal(row)}
              disabled={deactivatingEmployeeId === row.id}
            >
              Assign field
            </button>
            <button
              type="button"
              className="btn-primary"
              onClick={() => handleDeactivateEmployee(row)}
              disabled={
                deactivatingEmployeeId === row.id || row.status === "inactive"
              }
            >
              {deactivatingEmployeeId === row.id
                ? "Deactivating..."
                : row.status === "inactive"
                  ? "Inactive"
                  : "Deactivate"}
            </button>
          </div>
        ),
      },
    ],
    [deactivatingEmployeeId, fields, loadingEditEmployeeId],
  );

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
          onAction={openCreateModal}
        />

        {(error || fieldsError) && (
          <p className="dashboard-state error">{error || fieldsError}</p>
        )}

        {createError && <p className="dashboard-state error">{createError}</p>}

        {editError && <p className="dashboard-state error">{editError}</p>}

        {assignError && <p className="dashboard-state error">{assignError}</p>}

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

      {editModalOpen && (
        <div className="modal-backdrop" onClick={closeEditModal}>
          <div
            className="modal-card"
            onClick={(event) => event.stopPropagation()}
          >
            <div className="section-head modal-head">
              <div>
                <h3>Edit employee</h3>
                <p>Update employee profile fields through backend admin API.</p>
              </div>
              <button
                type="button"
                className="btn-secondary"
                onClick={closeEditModal}
                disabled={Boolean(deactivatingEmployeeId)}
              >
                Close
              </button>
            </div>

            <form className="modal-form" onSubmit={handleUpdateEmployee}>
              <label className="modal-field">
                <span>Name</span>
                <input
                  type="text"
                  name="person_name"
                  value={editForm.person_name}
                  onChange={handleEditChange}
                  placeholder="Full name"
                />
              </label>

              <label className="modal-field">
                <span>Email</span>
                <input
                  type="email"
                  name="email"
                  value={editForm.email}
                  onChange={handleEditChange}
                  placeholder="Email address"
                />
              </label>

              <label className="modal-field">
                <span>Phone</span>
                <input
                  type="text"
                  name="phone"
                  value={editForm.phone}
                  onChange={handleEditChange}
                  placeholder="10-digit phone"
                />
              </label>

              <label className="modal-field">
                <span>Birthday</span>
                <input
                  type="date"
                  name="birthday"
                  value={editForm.birthday}
                  onChange={handleEditChange}
                />
              </label>

              <label className="modal-field">
                <span>Sex</span>
                <select
                  name="sex"
                  value={editForm.sex}
                  onChange={handleEditChange}
                >
                  <option value="">Not set</option>
                  <option value="male">Male</option>
                  <option value="female">Female</option>
                  <option value="other">Other</option>
                </select>
              </label>

              <label className="modal-field">
                <span>Status</span>
                <select
                  name="status"
                  value={editForm.status}
                  onChange={handleEditChange}
                >
                  <option value="active">Active</option>
                  <option value="inactive">Inactive</option>
                </select>
              </label>

              <label className="modal-field modal-field-full">
                <span>Address</span>
                <textarea
                  rows="3"
                  name="address"
                  value={editForm.address}
                  onChange={handleEditChange}
                  placeholder="Address"
                />
              </label>

              <div className="modal-actions modal-actions-wide">
                <button
                  type="button"
                  className="btn-secondary"
                  onClick={closeEditModal}
                  disabled={Boolean(deactivatingEmployeeId)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn-primary"
                  disabled={Boolean(deactivatingEmployeeId)}
                >
                  {deactivatingEmployeeId ? "Saving..." : "Save changes"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {createModalOpen && (
        <div className="modal-backdrop" onClick={closeCreateModal}>
          <div
            className="modal-card"
            onClick={(event) => event.stopPropagation()}
          >
            <div className="section-head modal-head">
              <div>
                <h3>Create employee</h3>
                <p>Create a new manager account for field operations.</p>
              </div>
              <button
                type="button"
                className="btn-secondary"
                onClick={closeCreateModal}
                disabled={creatingEmployee}
              >
                Close
              </button>
            </div>

            <form className="modal-form" onSubmit={handleCreateEmployee}>
              <label className="modal-field">
                <span>Name</span>
                <input
                  type="text"
                  name="person_name"
                  value={newEmployee.person_name}
                  onChange={handleCreateEmployeeChange}
                  placeholder="Full name"
                />
              </label>

              <label className="modal-field">
                <span>Email</span>
                <input
                  type="email"
                  name="email"
                  value={newEmployee.email}
                  onChange={handleCreateEmployeeChange}
                  placeholder="Email address"
                />
              </label>

              <label className="modal-field">
                <span>Username</span>
                <input
                  type="text"
                  name="username"
                  value={newEmployee.username}
                  onChange={handleCreateEmployeeChange}
                  placeholder="Login username"
                />
              </label>

              <label className="modal-field">
                <span>Password</span>
                <input
                  type="password"
                  name="password"
                  value={newEmployee.password}
                  onChange={handleCreateEmployeeChange}
                  placeholder="Temporary password"
                />
              </label>

              <label className="modal-field">
                <span>Phone</span>
                <input
                  type="text"
                  name="phone"
                  value={newEmployee.phone}
                  onChange={handleCreateEmployeeChange}
                  placeholder="10-digit phone"
                />
              </label>

              <label className="modal-field">
                <span>Birthday</span>
                <input
                  type="date"
                  name="birthday"
                  value={newEmployee.birthday}
                  onChange={handleCreateEmployeeChange}
                />
              </label>

              <label className="modal-field">
                <span>Sex</span>
                <select
                  name="sex"
                  value={newEmployee.sex}
                  onChange={handleCreateEmployeeChange}
                >
                  <option value="">Not set</option>
                  <option value="male">Male</option>
                  <option value="female">Female</option>
                  <option value="other">Other</option>
                </select>
              </label>

              <label className="modal-field">
                <span>Status</span>
                <select
                  name="status"
                  value={newEmployee.status}
                  onChange={handleCreateEmployeeChange}
                >
                  <option value="active">Active</option>
                  <option value="inactive">Inactive</option>
                </select>
              </label>

              <label className="modal-field modal-field-full">
                <span>Address</span>
                <textarea
                  rows="3"
                  name="address"
                  value={newEmployee.address}
                  onChange={handleCreateEmployeeChange}
                  placeholder="Address"
                />
              </label>

              <div className="modal-actions modal-actions-wide">
                <button
                  type="button"
                  className="btn-secondary"
                  onClick={closeCreateModal}
                  disabled={creatingEmployee}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn-primary"
                  disabled={creatingEmployee}
                >
                  {creatingEmployee ? "Creating..." : "Create employee"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

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
                  value={selectedfield_id}
                  onChange={(event) => setSelectedfield_id(event.target.value)}
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
