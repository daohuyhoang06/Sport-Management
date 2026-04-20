import { useMemo, useState } from "react";
import AdminTable from "../../components/admin/AdminTable";
import EndpointPanel from "../../components/admin/EndpointPanel";
import ListFilters from "../../components/admin/ListFilters";
import PageHero from "../../components/admin/PageHero";
import StatusPill from "../../components/admin/StatusPill";
import useAdminFields from "../../hooks/useAdminFields";
import useListFilters from "../../hooks/useListFilters";

const fieldEndpoints = [
  { method: "GET", path: "/api/admin/fields" },
  { method: "GET", path: "/api/admin/fields/stats" },
  { method: "GET", path: "/api/admin/fields/:id" },
  { method: "PATCH", path: "/api/admin/fields/:id/status" },
  { method: "POST", path: "/api/admin/fields/:id/images" },
];

export default function FieldsPage() {
  const { fields, stats, loading, error, toggleFieldStatus, createField } =
    useAdminFields();
  const [submittingFieldId, setSubmittingFieldId] = useState(null);
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [creatingField, setCreatingField] = useState(false);
  const [newField, setNewField] = useState({
    field_name: "",
    location: "",
    rental_price: "",
    manager_id: "",
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
    rows: fields,
    searchFields: ["name", "location", "managerName"],
  });

  const handleToggleStatus = async (row) => {
    if (row.status === "maintenance") {
      return;
    }

    try {
      setSubmittingFieldId(row.id);
      setActionError("");
      setActionSuccess("");
      await toggleFieldStatus(row.id);
      setActionSuccess(
        `Field ${row.name} changed to ${row.status === "active" ? "inactive" : "active"}.`,
      );
    } catch (submitError) {
      setActionError(submitError.message || "Unable to change field status");
    } finally {
      setSubmittingFieldId(null);
    }
  };

  const openCreateModal = () => {
    setCreateModalOpen(true);
    setActionError("");
    setActionSuccess("");
  };

  const closeCreateModal = () => {
    if (creatingField) {
      return;
    }

    setCreateModalOpen(false);
    setNewField({
      field_name: "",
      location: "",
      rental_price: "",
      manager_id: "",
      status: "active",
    });
  };

  const handleCreateFieldChange = (event) => {
    const { name, value } = event.target;

    setNewField((current) => ({
      ...current,
      [name]: value,
    }));
  };

  const handleCreateField = async (event) => {
    event.preventDefault();

    if (!newField.field_name.trim() || !newField.location.trim()) {
      setActionError("Please fill in field name and location.");
      return;
    }

    if (!newField.rental_price || Number(newField.rental_price) <= 0) {
      setActionError("Please enter a valid rental price.");
      return;
    }

    try {
      setCreatingField(true);
      setActionError("");
      setActionSuccess("");

      await createField({
        field_name: newField.field_name.trim(),
        location: newField.location.trim(),
        rental_price: Number(newField.rental_price),
        manager_id: newField.manager_id ? Number(newField.manager_id) : null,
        status: newField.status,
      });

      setActionSuccess(
        `Field ${newField.field_name.trim()} created successfully.`,
      );
      closeCreateModal();
    } catch (submitError) {
      setActionError(submitError.message || "Unable to create field");
    } finally {
      setCreatingField(false);
    }
  };

  const fieldColumns = useMemo(
    () => [
      { key: "name", label: "Field" },
      { key: "location", label: "Location" },
      { key: "managerName", label: "Manager" },
      {
        key: "pricePerHour",
        label: "Price / hour",
        render: (row) =>
          new Intl.NumberFormat("vi-VN", {
            style: "currency",
            currency: "VND",
            maximumFractionDigits: 0,
          }).format(row.pricePerHour),
      },
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
              disabled={
                submittingFieldId === row.id || row.status === "maintenance"
              }
            >
              {submittingFieldId === row.id
                ? "Updating..."
                : row.status === "maintenance"
                  ? "Maintenance"
                  : row.status === "active"
                    ? "Deactivate"
                    : "Activate"}
            </button>
          </div>
        ),
      },
    ],
    [submittingFieldId],
  );

  return (
    <section className="page-shell">
      <PageHero
        badges={[
          "Admin module",
          "Fields",
          loading ? "Loading from backend" : `${stats.total} total fields`,
        ]}
        title="Fields"
        description="Fields page now reads backend list and stats so admin web reflects the same field state as the database."
      />

      <section className="section-card table-card">
        <div className="table-head">
          <h3>Field list</h3>
          <button type="button" onClick={openCreateModal}>
            Add field
          </button>
        </div>
        <p>Live data from /api/admin/fields and /api/admin/fields/stats.</p>

        {error && <p className="dashboard-state error">{error}</p>}
        {actionError && <p className="dashboard-state error">{actionError}</p>}
        {actionSuccess && (
          <p className="dashboard-state success">{actionSuccess}</p>
        )}

        <ListFilters
          searchPlaceholder="Search by field, location, or manager"
          searchText={searchText}
          onSearchChange={setSearchText}
          statusFilter={statusFilter}
          onStatusChange={setStatusFilter}
          totalCount={totalCount}
          filteredCount={filteredCount}
          hasActiveFilters={hasActiveFilters}
          onResetFilters={resetFilters}
          statusOptions={["active", "inactive", "maintenance"]}
        />

        <AdminTable
          columns={fieldColumns}
          rows={filteredRows}
          emptyMessage="No fields match the current filters."
        />
      </section>

      <EndpointPanel title="Fields endpoints" endpoints={fieldEndpoints} />

      {createModalOpen && (
        <div className="modal-backdrop" onClick={closeCreateModal}>
          <div
            className="modal-card"
            onClick={(event) => event.stopPropagation()}
          >
            <div className="section-head modal-head">
              <div>
                <h3>Create field</h3>
                <p>Add a new field through the backend admin endpoint.</p>
              </div>
              <button
                type="button"
                className="btn-secondary"
                onClick={closeCreateModal}
                disabled={creatingField}
              >
                Close
              </button>
            </div>

            <form className="modal-form" onSubmit={handleCreateField}>
              <label className="modal-field">
                <span>Field name</span>
                <input
                  type="text"
                  name="field_name"
                  value={newField.field_name}
                  onChange={handleCreateFieldChange}
                  placeholder="Field name"
                />
              </label>

              <label className="modal-field">
                <span>Location</span>
                <input
                  type="text"
                  name="location"
                  value={newField.location}
                  onChange={handleCreateFieldChange}
                  placeholder="Address or area"
                />
              </label>

              <label className="modal-field">
                <span>Rental price (VND)</span>
                <input
                  type="number"
                  min="1"
                  name="rental_price"
                  value={newField.rental_price}
                  onChange={handleCreateFieldChange}
                  placeholder="e.g. 300000"
                />
              </label>

              <label className="modal-field">
                <span>Manager ID (optional)</span>
                <input
                  type="number"
                  min="1"
                  name="manager_id"
                  value={newField.manager_id}
                  onChange={handleCreateFieldChange}
                  placeholder="Person ID"
                />
              </label>

              <label className="modal-field modal-field-full">
                <span>Status</span>
                <select
                  name="status"
                  value={newField.status}
                  onChange={handleCreateFieldChange}
                >
                  <option value="active">Active</option>
                  <option value="inactive">Inactive</option>
                  <option value="maintenance">Maintenance</option>
                </select>
              </label>

              <div className="modal-actions modal-actions-wide">
                <button
                  type="button"
                  className="btn-secondary"
                  onClick={closeCreateModal}
                  disabled={creatingField}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn-primary"
                  disabled={creatingField}
                >
                  {creatingField ? "Creating..." : "Create field"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </section>
  );
}
