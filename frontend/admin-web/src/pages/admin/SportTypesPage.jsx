import { useMemo, useState } from "react";
import AdminTable from "../../components/admin/AdminTable";
import useAdminSportTypes from "../../hooks/useAdminSportTypes";

const initialFormState = {
  sport_name: "",
};

function SportTypeFormModal({
  open,
  mode,
  formState,
  loading,
  error,
  onClose,
  onChange,
  onSubmit,
}) {
  if (!open) {
    return null;
  }

  const isEdit = mode === "edit";

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div
        className="modal-card modal-card-fields"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="section-head modal-head">
          <div>
            <h3>{isEdit ? "Sửa loại sân" : "Thêm loại sân"}</h3>
            <p>Quản lý danh mục loại sân để mở rộng hệ thống linh hoạt.</p>
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
          <label className="modal-field modal-field-full">
            <span>Tên loại sân</span>
            <input
              type="text"
              name="sport_name"
              value={formState.sport_name}
              onChange={onChange}
              placeholder="Ví dụ: Bóng đá, Cầu lông, Tennis"
            />
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
              {loading
                ? "Đang xử lý..."
                : isEdit
                  ? "Lưu thay đổi"
                  : "Tạo loại sân"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default function SportTypesPage() {
  const {
    sportTypes,
    loading,
    error,
    createSportType,
    updateSportType,
    deleteSportType,
  } = useAdminSportTypes();

  const [searchText, setSearchText] = useState("");
  const [submittingId, setSubmittingId] = useState(null);
  const [actionError, setActionError] = useState("");
  const [actionSuccess, setActionSuccess] = useState("");
  const [createOpen, setCreateOpen] = useState(false);
  const [editOpen, setEditOpen] = useState(false);
  const [creating, setCreating] = useState(false);
  const [editing, setEditing] = useState(false);
  const [selectedSportType, setSelectedSportType] = useState(null);
  const [createForm, setCreateForm] = useState(initialFormState);
  const [editForm, setEditForm] = useState(initialFormState);

  const filteredRows = useMemo(() => {
    const keyword = searchText.trim().toLowerCase();
    if (!keyword) {
      return sportTypes;
    }

    return sportTypes.filter((sportType) =>
      `${sportType.sport_id} ${sportType.sport_name}`
        .toLowerCase()
        .includes(keyword),
    );
  }, [sportTypes, searchText]);

  const openCreateModal = () => {
    setActionError("");
    setActionSuccess("");
    setCreateForm(initialFormState);
    setCreateOpen(true);
  };

  const openEditModal = (sportType) => {
    setActionError("");
    setActionSuccess("");
    setSelectedSportType(sportType);
    setEditForm({ sport_name: sportType.sport_name || "" });
    setEditOpen(true);
  };

  const closeModals = () => {
    if (creating || editing) {
      return;
    }

    setCreateOpen(false);
    setEditOpen(false);
    setSelectedSportType(null);
    setCreateForm(initialFormState);
    setEditForm(initialFormState);
  };

  const onFormChange = (setter) => (event) => {
    const { name, value } = event.target;
    setter((current) => ({
      ...current,
      [name]: value,
    }));
  };

  const handleCreateSubmit = async (event) => {
    event.preventDefault();

    if (!createForm.sport_name.trim()) {
      setActionError("Vui lòng nhập tên loại sân.");
      return;
    }

    try {
      setCreating(true);
      setActionError("");
      setActionSuccess("");
      await createSportType(createForm.sport_name.trim());
      setActionSuccess(`Đã tạo loại sân ${createForm.sport_name.trim()} thành công.`);
      closeModals();
    } catch (submitError) {
      setActionError(submitError.message || "Không thể tạo loại sân.");
    } finally {
      setCreating(false);
    }
  };

  const handleEditSubmit = async (event) => {
    event.preventDefault();

    if (!selectedSportType) {
      return;
    }

    if (!editForm.sport_name.trim()) {
      setActionError("Vui lòng nhập tên loại sân.");
      return;
    }

    try {
      setEditing(true);
      setActionError("");
      setActionSuccess("");
      await updateSportType(selectedSportType.sport_id, editForm.sport_name.trim());
      setActionSuccess(`Đã cập nhật loại sân ${editForm.sport_name.trim()} thành công.`);
      closeModals();
    } catch (submitError) {
      setActionError(submitError.message || "Không thể cập nhật loại sân.");
    } finally {
      setEditing(false);
    }
  };

  const handleDeleteSportType = async (row) => {
    const confirmed = window.confirm(
      `Xóa loại sân ${row.sport_name}? Thao tác này không thể hoàn tác.`,
    );

    if (!confirmed) {
      return;
    }

    try {
      setSubmittingId(row.sport_id);
      setActionError("");
      setActionSuccess("");
      await deleteSportType(row.sport_id);
      setActionSuccess(`Đã xóa loại sân ${row.sport_name} thành công.`);
    } catch (submitError) {
      setActionError(submitError.message || "Không thể xóa loại sân.");
    } finally {
      setSubmittingId(null);
    }
  };

  const columns = useMemo(
    () => [
      {
        key: "sport_id",
        label: "ID",
        render: (row) => <span className="field-id-tag">#{row.sport_id}</span>,
      },
      { key: "sport_name", label: "Loại sân" },
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
              className="field-action delete"
              onClick={() => handleDeleteSportType(row)}
              disabled={submittingId === row.sport_id}
            >
              {submittingId === row.sport_id ? "Đang xử lý..." : "🗑 Xóa"}
            </button>
          </div>
        ),
      },
    ],
    [submittingId],
  );

  return (
    <section className="fields-page">
      <header className="fields-hero">
        <div className="dashboard-hero-left">
          <div className="dashboard-hero-icon">🧩</div>
          <div>
            <p className="dashboard-hero-kicker">Dashboard</p>
            <h2>Quản Lý Loại Sân</h2>
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

      <section className="fields-toolbar card-surface">
        <div className="fields-search-wrap">
          <label className="fields-search-box">
            <span>🔎</span>
            <input
              type="search"
              value={searchText}
              onChange={(event) => setSearchText(event.target.value)}
              placeholder="Tìm kiếm loại sân..."
            />
          </label>
        </div>

        <button type="button" className="fields-add-btn" onClick={openCreateModal}>
          + Thêm Loại Sân
        </button>
      </section>

      {error && <p className="dashboard-state error">{error}</p>}
      {actionError && <p className="dashboard-state error">{actionError}</p>}
      {actionSuccess && <p className="dashboard-state success">{actionSuccess}</p>}

      <section className="fields-table-card section-card">
        <div className="fields-table-head">
          <div>
            <h3>Danh sách loại sân</h3>
            <p>
              Hiển thị {filteredRows.length.toLocaleString("vi-VN")} /{" "}
              {sportTypes.length.toLocaleString("vi-VN")} loại sân.
            </p>
          </div>
          <div className="fields-table-chip">
            {loading ? "Đang tải dữ liệu..." : "Đồng bộ backend live"}
          </div>
        </div>

        <AdminTable
          columns={columns}
          rows={filteredRows}
          emptyMessage="Không có loại sân nào khớp với bộ lọc hiện tại."
        />
      </section>

      <SportTypeFormModal
        open={createOpen}
        mode="create"
        formState={createForm}
        loading={creating}
        error={actionError}
        onClose={closeModals}
        onChange={onFormChange(setCreateForm)}
        onSubmit={handleCreateSubmit}
      />

      <SportTypeFormModal
        open={editOpen}
        mode="edit"
        formState={editForm}
        loading={editing}
        error={actionError}
        onClose={closeModals}
        onChange={onFormChange(setEditForm)}
        onSubmit={handleEditSubmit}
      />
    </section>
  );
}
