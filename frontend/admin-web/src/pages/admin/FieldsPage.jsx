import { useMemo, useState } from "react";
import AdminTable from "../../components/admin/AdminTable";
import useAdminFields from "../../hooks/useAdminFields";
import useAdminSportTypes from "../../hooks/useAdminSportTypes";

const initialFieldForm = {
  field_name: "",
  location: "",
  phone: "",
  slot_price: "",
  manager_id: "",
  sport_id: "",
  status: "active",
};

const statusOptions = [
  { value: "all", label: "Tất cả trạng thái" },
  { value: "active", label: "Hoạt động" },
  { value: "inactive", label: "Không hoạt động" },
  { value: "maintenance", label: "Bảo trì" },
];

const isRentalPriceRequired = (status) => status === "active";

function renderFieldStatus(status) {
  const labels = {
    active: "HOẠT ĐỘNG",
    inactive: "KHÔNG HOẠT ĐỘNG",
    maintenance: "BẢO TRÌ",
  };

  return (
    <span className={`field-status-pill ${status || "inactive"}`}>
      {labels[status] || labels.inactive}
    </span>
  );
}

function FieldFormModal({
  mode,
  open,
  formState,
  sportTypes,
  loading,
  error,
  onClose,
  onChange,
  onSubmit,
}) {
  if (!open) {
    return null;
  }

  const title = mode === "edit" ? "Sửa sân" : "Thêm sân";
  const submitLabel = mode === "edit" ? "Lưu thay đổi" : "Tạo sân";

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div
        className="modal-card modal-card-fields"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="section-head modal-head">
          <div>
            <h3>{title}</h3>
            <p>
              Đồng bộ theo phong cách dashboard và cập nhật trực tiếp từ
              backend.
            </p>
          </div>
          <button
            type="button"
            className="btn-secondary"
            onClick={onClose}
            disabled={loading}
          >
            Đóng
          </button>
        </div>

        {error && <p className="dashboard-state error">{error}</p>}

        <form className="modal-form" onSubmit={onSubmit}>
          <label className="modal-field">
            <span>Tên sân</span>
            <input
              type="text"
              name="field_name"
              value={formState.field_name}
              onChange={onChange}
              placeholder="Nhập tên sân"
            />
          </label>

          <label className="modal-field">
            <span>Địa chỉ</span>
            <input
              type="text"
              name="location"
              value={formState.location}
              onChange={onChange}
              placeholder="Nhập khu vực hoặc địa chỉ"
            />
          </label>

          <label className="modal-field">
            <span>Số điện thoại sân</span>
            <input
              type="tel"
              name="phone"
              value={formState.phone}
              onChange={onChange}
              placeholder="Nhập số điện thoại sân"
            />
          </label>

          <label className="modal-field">
            <span>Giá thuê / giờ</span>
            <input
              type="number"
              min={isRentalPriceRequired(formState.status) ? "1" : "0"}
              name="slot_price"
              value={formState.slot_price}
              onChange={onChange}
              placeholder="Ví dụ: 300000"
            />
            <small className="modal-field-hint">
              {isRentalPriceRequired(formState.status)
                ? "Bắt buộc khi sân ở trạng thái Hoạt động."
                : "Không bắt buộc khi sân ở trạng thái Không hoạt động hoặc Bảo trì."}
            </small>
          </label>

          <label className="modal-field modal-field-full">
            <span>Loại sân</span>
            <select
              name="sport_id"
              value={formState.sport_id}
              onChange={onChange}
            >
              <option value="">Chọn loại sân</option>
              {sportTypes.map((sportType) => (
                <option key={sportType.id} value={sportType.id}>
                  {sportType.name}
                </option>
              ))}
            </select>
          </label>

          <label className="modal-field">
            <span>Người quản lý (ID)</span>
            <input
              type="number"
              min="1"
              name="manager_id"
              value={formState.manager_id}
              onChange={onChange}
              placeholder="Nếu chưa có, để trống"
            />
          </label>

          <label className="modal-field modal-field-full">
            <span>Trạng thái</span>
            <select name="status" value={formState.status} onChange={onChange}>
              <option value="active">Hoạt động</option>
              <option value="inactive">Không hoạt động</option>
              <option value="maintenance">Bảo trì</option>
            </select>
          </label>

          <div className="modal-actions modal-actions-wide">
            <button
              type="button"
              className="btn-secondary"
              onClick={onClose}
              disabled={loading}
            >
              Hủy
            </button>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? "Đang xử lý..." : submitLabel}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default function FieldsPage() {
  const {
    fields,
    stats,
    loading,
    error,
    toggleFieldStatus,
    deleteField,
    createField,
    updateField,
  } = useAdminFields();
  const { types: sportTypes } = useAdminSportTypes();

  const [searchText, setSearchText] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");
  const [submittingFieldId, setSubmittingFieldId] = useState(null);
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [creatingField, setCreatingField] = useState(false);
  const [editingField, setEditingField] = useState(false);
  const [actionError, setActionError] = useState("");
  const [actionSuccess, setActionSuccess] = useState("");
  const [selectedField, setSelectedField] = useState(null);
  const [createForm, setCreateForm] = useState(initialFieldForm);
  const [editForm, setEditForm] = useState(initialFieldForm);

  const filteredRows = useMemo(() => {
    const keyword = searchText.trim().toLowerCase();

    return fields.filter((field) => {
      const matchesKeyword =
        !keyword ||
        [field.name, field.location, field.phone, field.managerName]
          .join(" ")
          .toLowerCase()
          .includes(keyword);

      const matchesStatus =
        statusFilter === "all" || field.status === statusFilter;

      return matchesKeyword && matchesStatus;
    });
  }, [fields, searchText, statusFilter]);

  const handleFieldChange = (setter) => (event) => {
    const { name, value } = event.target;

    setter((current) => ({
      ...current,
      [name]: value,
    }));
  };

  const openCreateModal = () => {
    setActionError("");
    setActionSuccess("");
    setCreateForm(initialFieldForm);
    setCreateModalOpen(true);
  };

  const openEditModal = (field) => {
    setActionError("");
    setActionSuccess("");
    setSelectedField(field);
    setEditForm({
      field_name: field.name || "",
      location: field.location || "",
      phone: field.phone && field.phone !== "-" ? field.phone : "",
      sport_id: field.sportId ? String(field.sportId) : "",
      slot_price:
        field.pricePerHour !== null &&
        field.pricePerHour !== undefined &&
        Number(field.pricePerHour) > 0
          ? String(field.pricePerHour)
          : "",
      manager_id: field.managerId ? String(field.managerId) : "",
      status: field.status || "active",
    });
    setEditModalOpen(true);
  };

  const closeModals = () => {
    if (creatingField || editingField) {
      return;
    }

    setCreateModalOpen(false);
    setEditModalOpen(false);
    setSelectedField(null);
    setCreateForm(initialFieldForm);
    setEditForm(initialFieldForm);
  };

  const handleCreateSubmit = async (event) => {
    event.preventDefault();

    if (!createForm.field_name.trim() || !createForm.location.trim()) {
      setActionError("Vui lòng nhập tên sân và địa chỉ.");
      return;
    }

    if (!createForm.sport_id) {
      setActionError("Vui lòng chọn loại sân.");
      return;
    }

    if (
      isRentalPriceRequired(createForm.status) &&
      (!createForm.slot_price || Number(createForm.slot_price) <= 0)
    ) {
      setActionError("Vui lòng nhập giá thuê hợp lệ.");
      return;
    }

    try {
      setCreatingField(true);
      setActionError("");
      setActionSuccess("");

      await createField({
        field_name: createForm.field_name.trim(),
        location: createForm.location.trim(),
        phone: createForm.phone.trim() || null,
        sport_id: Number(createForm.sport_id),
        slot_price:
          createForm.slot_price && Number(createForm.slot_price) > 0
            ? Number(createForm.slot_price)
            : null,
        manager_id: createForm.manager_id
          ? Number(createForm.manager_id)
          : null,
        status: createForm.status,
      });

      setActionSuccess(
        `Đã tạo sân ${createForm.field_name.trim()} thành công.`,
      );
      closeModals();
    } catch (submitError) {
      setActionError(submitError.message || "Không thể tạo sân.");
    } finally {
      setCreatingField(false);
    }
  };

  const handleEditSubmit = async (event) => {
    event.preventDefault();

    if (!selectedField) {
      return;
    }

    if (!editForm.field_name.trim() || !editForm.location.trim()) {
      setActionError("Vui lòng nhập tên sân và địa chỉ.");
      return;
    }

    if (!editForm.sport_id) {
      setActionError("Vui lòng chọn loại sân.");
      return;
    }

    if (
      isRentalPriceRequired(editForm.status) &&
      (!editForm.slot_price || Number(editForm.slot_price) <= 0)
    ) {
      setActionError("Vui lòng nhập giá thuê hợp lệ.");
      return;
    }

    try {
      setEditingField(true);
      setActionError("");
      setActionSuccess("");

      await updateField(selectedField.id, {
        field_name: editForm.field_name.trim(),
        location: editForm.location.trim(),
        phone: editForm.phone.trim() || null,
        sport_id: Number(editForm.sport_id),
        slot_price:
          editForm.slot_price && Number(editForm.slot_price) > 0
            ? Number(editForm.slot_price)
            : null,
        manager_id: editForm.manager_id ? Number(editForm.manager_id) : null,
        status: editForm.status,
      });

      setActionSuccess(
        `Đã cập nhật sân ${editForm.field_name.trim()} thành công.`,
      );
      closeModals();
    } catch (submitError) {
      setActionError(submitError.message || "Không thể cập nhật sân.");
    } finally {
      setEditingField(false);
    }
  };

  const handleToggleStatus = async (field) => {
    if (field.status === "maintenance") {
      return;
    }

    try {
      setSubmittingFieldId(field.id);
      setActionError("");
      setActionSuccess("");
      await toggleFieldStatus(field.id);
      setActionSuccess(
        `Đã chuyển sân ${field.name} sang trạng thái ${field.status === "active" ? "không hoạt động" : "hoạt động"}.`,
      );
    } catch (submitError) {
      setActionError(submitError.message || "Không thể đổi trạng thái sân.");
    } finally {
      setSubmittingFieldId(null);
    }
  };

  const handleDeleteField = async (field) => {
    const confirmed = window.confirm(
      `Xóa sân ${field.name}? Thao tác này không thể hoàn tác.`,
    );

    if (!confirmed) {
      return;
    }

    try {
      setSubmittingFieldId(field.id);
      setActionError("");
      setActionSuccess("");
      await deleteField(field.id);
      setActionSuccess(`Đã xóa sân ${field.name} thành công.`);
    } catch (submitError) {
      setActionError(submitError.message || "Không thể xóa sân.");
    } finally {
      setSubmittingFieldId(null);
    }
  };

  const fieldColumns = useMemo(
    () => [
      {
        key: "id",
        label: "ID",
        render: (row) => <span className="field-id-tag">#{row.id}</span>,
      },
      { key: "name", label: "Tên sân" },
      {
        key: "location",
        label: "Địa chỉ",
        render: (row) => (
          <span className="field-location">
            <span>📍</span>
            {row.location}
          </span>
        ),
      },
      {
        key: "sportName",
        label: "Loại sân",
        render: (row) => (
          <span className="field-sport-type">{row.sportName}</span>
        ),
      },
      {
        key: "phone",
        label: "SĐT",
        render: (row) => <span className="user-meta-cell">📱 {row.phone}</span>,
      },
      { key: "managerName", label: "Quản lý" },
      {
        key: "status",
        label: "Trạng thái",
        render: (row) => renderFieldStatus(row.status),
      },
      {
        key: "actions",
        label: "Thao tác",
        render: (row) => (
          <div className="field-actions">
            <button
              type="button"
              className="field-action edit"
              onClick={() => openEditModal(row)}
            >
              ✏️ Sửa
            </button>
            <button
              type="button"
              className="field-action toggle"
              onClick={() => handleToggleStatus(row)}
              disabled={
                submittingFieldId === row.id || row.status === "maintenance"
              }
            >
              {submittingFieldId === row.id
                ? "Đang xử lý..."
                : row.status === "maintenance"
                  ? "Bảo trì"
                  : row.status === "active"
                    ? "Tắt"
                    : "Bật"}
            </button>
            <button
              type="button"
              className="field-action delete"
              onClick={() => handleDeleteField(row)}
              disabled={submittingFieldId === row.id}
            >
              🗑 Xóa
            </button>
          </div>
        ),
      },
    ],
    [submittingFieldId],
  );

  return (
    <section className="fields-page">
      <header className="fields-hero">
        <div className="dashboard-hero-left">
          <div className="dashboard-hero-icon">🏟️</div>
          <div>
            <p className="dashboard-hero-kicker">Dashboard</p>
            <h2>Quản Lý Sân Bóng</h2>
          </div>
        </div>

        <div className="dashboard-hero-right">
          <div className="dashboard-role-switcher" aria-label="Vai trò">
            <span className="is-active">👷 Quản trị viên</span>
            <span>📘 Quản lý</span>
            <span>👤 Người dùng</span>
          </div>
          <div className="dashboard-user-chip">
            <span className="dashboard-user-badge">ADMIN</span>
            <strong>Admin</strong>
          </div>
        </div>
      </header>

      <section className="fields-stats-grid">
        <article
          className="admin-stat-card"
          style={{ ["--accent-color"]: "#1a8f5a" }}
        >
          <div className="admin-stat-copy">
            <p>Tổng số sân</p>
            <h3>{stats.total.toLocaleString("vi-VN")}</h3>
            <span>Toàn bộ sân trong hệ thống</span>
          </div>
          <div className="admin-stat-icon">🏟️</div>
        </article>
        <article
          className="admin-stat-card"
          style={{ ["--accent-color"]: "#0f766e" }}
        >
          <div className="admin-stat-copy">
            <p>Đang hoạt động</p>
            <h3>{stats.active.toLocaleString("vi-VN")}</h3>
            <span>Sẵn sàng cho booking</span>
          </div>
          <div className="admin-stat-icon">✅</div>
        </article>
        <article
          className="admin-stat-card"
          style={{ ["--accent-color"]: "#ff8c42" }}
        >
          <div className="admin-stat-copy">
            <p>Bảo trì</p>
            <h3>{stats.maintenance.toLocaleString("vi-VN")}</h3>
            <span>Đang tạm ngưng phục vụ</span>
          </div>
          <div className="admin-stat-icon">🛠️</div>
        </article>
      </section>

      <section className="fields-toolbar card-surface">
        <div className="fields-search-wrap">
          <label className="fields-search-box">
            <span>🔎</span>
            <input
              type="search"
              value={searchText}
              onChange={(event) => setSearchText(event.target.value)}
              placeholder="Tìm kiếm sân bóng..."
            />
          </label>
          <select
            value={statusFilter}
            onChange={(event) => setStatusFilter(event.target.value)}
          >
            {statusOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>

        <button
          type="button"
          className="fields-add-btn"
          onClick={openCreateModal}
        >
          + Thêm Sân Bóng
        </button>
      </section>

      {error && <p className="dashboard-state error">{error}</p>}
      {actionError && <p className="dashboard-state error">{actionError}</p>}
      {actionSuccess && (
        <p className="dashboard-state success">{actionSuccess}</p>
      )}

      <section className="fields-table-card section-card">
        <div className="fields-table-head">
          <div>
            <h3>Danh sách sân bóng</h3>
            <p>
              Hiển thị {filteredRows.length.toLocaleString("vi-VN")} /{" "}
              {fields.length.toLocaleString("vi-VN")} sân.
            </p>
          </div>
          <div className="fields-table-chip">
            {loading ? "Đang tải dữ liệu..." : "Đồng bộ backend live"}
          </div>
        </div>

        <AdminTable
          columns={fieldColumns}
          rows={filteredRows}
          emptyMessage="Không có sân nào khớp với bộ lọc hiện tại."
        />
      </section>

      <FieldFormModal
        mode="create"
        open={createModalOpen}
        formState={createForm}
        sportTypes={sportTypes}
        loading={creatingField}
        error={actionError}
        onClose={closeModals}
        onChange={handleFieldChange(setCreateForm)}
        onSubmit={handleCreateSubmit}
      />

      <FieldFormModal
        mode="edit"
        open={editModalOpen}
        formState={editForm}
        sportTypes={sportTypes}
        loading={editingField}
        error={actionError}
        onClose={closeModals}
        onChange={handleFieldChange(setEditForm)}
        onSubmit={handleEditSubmit}
      />
    </section>
  );
}
