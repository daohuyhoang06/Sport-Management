import { useMemo, useState } from "react";
import AdminTable from "../../components/admin/AdminTable";
import useAdminUsers from "../../hooks/useAdminUsers";

const initialUserForm = {
  name: "",
  email: "",
  username: "",
  password: "",
  phone: "",
  address: "",
  birthday: "",
  sex: "Nam",
  role: "user",
  status: "active",
};

const roleOptions = [
  { value: "all", label: "Tất cả vai trò" },
  { value: "user", label: "Người dùng" },
  { value: "manager", label: "Quản lý" },
  { value: "admin", label: "Quản trị viên" },
];

const statusOptions = [
  { value: "all", label: "Tất cả trạng thái" },
  { value: "active", label: "Hoạt động" },
  { value: "inactive", label: "Không hoạt động" },
];

function renderRolePill(role) {
  const labels = {
    user: "NGƯỜI DÙNG",
    manager: "QUẢN LÝ",
    admin: "QUẢN TRỊ VIÊN",
  };

  return (
    <span className={`user-role-pill ${role || "user"}`}>
      {labels[role] || labels.user}
    </span>
  );
}

function renderStatusPill(status) {
  const labels = {
    active: "HOẠT ĐỘNG",
    inactive: "KHÔNG HOẠT ĐỘNG",
  };

  return (
    <span className={`user-status-pill ${status || "inactive"}`}>
      {labels[status] || labels.inactive}
    </span>
  );
}

function UserFormModal({
  mode,
  open,
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
        className="modal-card modal-card-users"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="section-head modal-head">
          <div>
            <h3>{isEdit ? "Sửa người dùng" : "Thêm người dùng"}</h3>
            <p>
              Đồng bộ cùng giao diện dashboard và cập nhật trực tiếp từ backend.
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
            <span>Họ và tên</span>
            <input
              type="text"
              name="name"
              value={formState.name}
              onChange={onChange}
              placeholder="Nhập họ tên"
            />
          </label>

          <label className="modal-field">
            <span>Email</span>
            <input
              type="email"
              name="email"
              value={formState.email}
              onChange={onChange}
              placeholder="Nhập email"
            />
          </label>

          <label className="modal-field">
            <span>Tên đăng nhập</span>
            <input
              type="text"
              name="username"
              value={formState.username}
              onChange={onChange}
              placeholder="Nhập username"
            />
          </label>

          <label className="modal-field">
            <span>Số điện thoại</span>
            <input
              type="text"
              name="phone"
              value={formState.phone}
              onChange={onChange}
              placeholder="Nhập số điện thoại"
            />
          </label>

          <label className="modal-field">
            <span>Vai trò</span>
            <select name="role" value={formState.role} onChange={onChange}>
              <option value="user">Người dùng</option>
              <option value="manager">Quản lý</option>
              <option value="admin">Quản trị viên</option>
            </select>
          </label>

          <label className="modal-field">
            <span>Trạng thái</span>
            <select name="status" value={formState.status} onChange={onChange}>
              <option value="active">Hoạt động</option>
              <option value="inactive">Không hoạt động</option>
            </select>
          </label>

          <label className="modal-field">
            <span>Ngày sinh</span>
            <input
              type="date"
              name="birthday"
              value={formState.birthday}
              onChange={onChange}
            />
          </label>

          <label className="modal-field">
            <span>Giới tính</span>
            <select name="sex" value={formState.sex} onChange={onChange}>
              <option value="Nam">Nam</option>
              <option value="Nữ">Nữ</option>
              <option value="Khác">Khác</option>
            </select>
          </label>

          <label className="modal-field modal-field-full">
            <span>Địa chỉ</span>
            <textarea
              rows="3"
              name="address"
              value={formState.address}
              onChange={onChange}
              placeholder="Nhập địa chỉ"
            />
          </label>

          {!isEdit && (
            <label className="modal-field modal-field-full">
              <span>Mật khẩu</span>
              <input
                type="password"
                name="password"
                value={formState.password}
                onChange={onChange}
                placeholder="Đặt mật khẩu tạm"
              />
            </label>
          )}

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
                  : "Tạo người dùng"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default function UsersPage() {
  const {
    users,
    stats,
    loading,
    error,
    toggleUserStatus,
    deleteUser,
    createUser,
    updateUser,
  } = useAdminUsers();

  const [searchText, setSearchText] = useState("");
  const [roleFilter, setRoleFilter] = useState("all");
  const [statusFilter, setStatusFilter] = useState("all");
  const [submittingUserId, setSubmittingUserId] = useState(null);
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [creatingUser, setCreatingUser] = useState(false);
  const [editingUser, setEditingUser] = useState(false);
  const [actionError, setActionError] = useState("");
  const [actionSuccess, setActionSuccess] = useState("");
  const [selectedUser, setSelectedUser] = useState(null);
  const [createForm, setCreateForm] = useState(initialUserForm);
  const [editForm, setEditForm] = useState(initialUserForm);

  const filteredRows = useMemo(() => {
    const keyword = searchText.trim().toLowerCase();

    return users.filter((user) => {
      const matchesKeyword =
        !keyword ||
        [user.name, user.email, user.username, user.phone, user.role]
          .join(" ")
          .toLowerCase()
          .includes(keyword);

      const matchesRole = roleFilter === "all" || user.role === roleFilter;
      const matchesStatus =
        statusFilter === "all" || user.status === statusFilter;

      return matchesKeyword && matchesRole && matchesStatus;
    });
  }, [users, searchText, roleFilter, statusFilter]);

  const handleFieldChange = (setter) => (event) => {
    const { name, value } = event.target;
    setter((current) => ({ ...current, [name]: value }));
  };

  const openCreateModal = () => {
    setActionError("");
    setActionSuccess("");
    setCreateForm(initialUserForm);
    setCreateModalOpen(true);
  };

  const openEditModal = (user) => {
    setActionError("");
    setActionSuccess("");
    setSelectedUser(user);
    setEditForm({
      name: user.name || "",
      email: user.email || "",
      username: user.username || "",
      password: "",
      phone: user.phone || "",
      address: user.address || "",
      birthday: user.birthday || "",
      sex: user.sex || "Nam",
      role: user.role || "user",
      status: user.status || "active",
    });
    setEditModalOpen(true);
  };

  const closeModals = () => {
    if (creatingUser || editingUser) {
      return;
    }

    setCreateModalOpen(false);
    setEditModalOpen(false);
    setSelectedUser(null);
    setCreateForm(initialUserForm);
    setEditForm(initialUserForm);
  };

  const handleCreateSubmit = async (event) => {
    event.preventDefault();

    if (
      !createForm.name.trim() ||
      !createForm.email.trim() ||
      !createForm.username.trim() ||
      !createForm.password
    ) {
      setActionError(
        "Vui lòng nhập đầy đủ họ tên, email, username và mật khẩu.",
      );
      return;
    }

    try {
      setCreatingUser(true);
      setActionError("");
      setActionSuccess("");

      await createUser({
        name: createForm.name.trim(),
        email: createForm.email.trim(),
        username: createForm.username.trim(),
        password: createForm.password,
        phone: createForm.phone.trim() || null,
        address: createForm.address.trim() || null,
        birthday: createForm.birthday || null,
        sex: createForm.sex || null,
        role: createForm.role,
        status: createForm.status,
      });

      setActionSuccess(
        `Đã tạo người dùng ${createForm.name.trim()} thành công.`,
      );
      closeModals();
    } catch (submitError) {
      setActionError(submitError.message || "Không thể tạo người dùng.");
    } finally {
      setCreatingUser(false);
    }
  };

  const handleEditSubmit = async (event) => {
    event.preventDefault();

    if (!selectedUser) {
      return;
    }

    if (
      !editForm.name.trim() ||
      !editForm.email.trim() ||
      !editForm.username.trim()
    ) {
      setActionError("Vui lòng nhập đầy đủ họ tên, email và username.");
      return;
    }

    try {
      setEditingUser(true);
      setActionError("");
      setActionSuccess("");

      await updateUser(selectedUser.id, {
        name: editForm.name.trim(),
        email: editForm.email.trim(),
        username: editForm.username.trim(),
        phone: editForm.phone.trim() || null,
        address: editForm.address.trim() || null,
        birthday: editForm.birthday || null,
        sex: editForm.sex || null,
        role: editForm.role,
        status: editForm.status,
      });

      setActionSuccess(
        `Đã cập nhật người dùng ${editForm.name.trim()} thành công.`,
      );
      closeModals();
    } catch (submitError) {
      setActionError(submitError.message || "Không thể cập nhật người dùng.");
    } finally {
      setEditingUser(false);
    }
  };

  const handleToggleStatus = async (user) => {
    try {
      setSubmittingUserId(user.id);
      setActionError("");
      setActionSuccess("");
      await toggleUserStatus(user.id);
      setActionSuccess(
        `Đã chuyển người dùng ${user.name} sang trạng thái ${user.status === "active" ? "không hoạt động" : "hoạt động"}.`,
      );
    } catch (submitError) {
      setActionError(
        submitError.message || "Không thể đổi trạng thái người dùng.",
      );
    } finally {
      setSubmittingUserId(null);
    }
  };

  const handleDeleteUser = async (user) => {
    const confirmed = window.confirm(
      `Xóa người dùng ${user.name}? Thao tác này không thể hoàn tác.`,
    );
    if (!confirmed) {
      return;
    }

    try {
      setSubmittingUserId(user.id);
      setActionError("");
      setActionSuccess("");
      await deleteUser(user.id);
      setActionSuccess(`Đã xóa người dùng ${user.name} thành công.`);
    } catch (submitError) {
      setActionError(submitError.message || "Không thể xóa người dùng.");
    } finally {
      setSubmittingUserId(null);
    }
  };

  const userColumns = useMemo(
    () => [
      {
        key: "id",
        label: "ID",
        render: (row) => <span className="field-id-tag">#{row.id}</span>,
      },
      {
        key: "name",
        label: "Tên",
        render: (row) => <strong className="user-name-cell">{row.name}</strong>,
      },
      {
        key: "email",
        label: "Email",
        render: (row) => <span className="user-meta-cell">✉ {row.email}</span>,
      },
      {
        key: "phone",
        label: "Số điện thoại",
        render: (row) => <span className="user-meta-cell">📱 {row.phone}</span>,
      },
      {
        key: "role",
        label: "Vai trò",
        render: (row) => renderRolePill(row.role),
      },
      {
        key: "status",
        label: "Trạng thái",
        render: (row) => renderStatusPill(row.status),
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
              disabled={submittingUserId === row.id}
            >
              {submittingUserId === row.id
                ? "Đang xử lý..."
                : row.status === "active"
                  ? "Tắt"
                  : "Bật"}
            </button>
            <button
              type="button"
              className="field-action delete"
              onClick={() => handleDeleteUser(row)}
              disabled={submittingUserId === row.id}
            >
              🗑 Xóa
            </button>
          </div>
        ),
      },
    ],
    [submittingUserId],
  );

  return (
    <section className="users-page">
      <header className="fields-hero">
        <div className="dashboard-hero-left">
          <div className="dashboard-hero-icon">👥</div>
          <div>
            <p className="dashboard-hero-kicker">Dashboard</p>
            <h2>Quản Lý Người Dùng</h2>
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

      <section className="fields-stats-grid users-stats-grid">
        <article
          className="admin-stat-card"
          style={{ ["--accent-color"]: "#6b7cff" }}
        >
          <div className="admin-stat-copy">
            <p>Tổng người dùng</p>
            <h3>{stats.total.toLocaleString("vi-VN")}</h3>
            <span>Toàn bộ tài khoản trong hệ thống</span>
          </div>
          <div className="admin-stat-icon">👥</div>
        </article>
        <article
          className="admin-stat-card"
          style={{ ["--accent-color"]: "#18c48f" }}
        >
          <div className="admin-stat-copy">
            <p>Đang hoạt động</p>
            <h3>{stats.active.toLocaleString("vi-VN")}</h3>
            <span>Tài khoản đang dùng bình thường</span>
          </div>
          <div className="admin-stat-icon">✅</div>
        </article>
        <article
          className="admin-stat-card"
          style={{ ["--accent-color"]: "#ff5a4f" }}
        >
          <div className="admin-stat-copy">
            <p>Không hoạt động</p>
            <h3>{stats.inactive.toLocaleString("vi-VN")}</h3>
            <span>Tài khoản đã khóa hoặc vô hiệu hóa</span>
          </div>
          <div className="admin-stat-icon">❌</div>
        </article>
      </section>

      <section className="fields-toolbar card-surface users-toolbar">
        <div className="fields-search-wrap users-filter-wrap">
          <label className="fields-search-box users-search-box">
            <span>🔎</span>
            <input
              type="search"
              value={searchText}
              onChange={(event) => setSearchText(event.target.value)}
              placeholder="Tìm kiếm người dùng..."
            />
          </label>

          <select
            value={roleFilter}
            onChange={(event) => setRoleFilter(event.target.value)}
          >
            {roleOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>

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
          + Thêm Người Dùng
        </button>
      </section>

      {error && <p className="dashboard-state error">{error}</p>}
      {actionError && <p className="dashboard-state error">{actionError}</p>}
      {actionSuccess && (
        <p className="dashboard-state success">{actionSuccess}</p>
      )}

      <section className="fields-table-card section-card users-table-card">
        <div className="fields-table-head">
          <div>
            <h3>Danh sách người dùng</h3>
            <p>
              Hiển thị {filteredRows.length.toLocaleString("vi-VN")} /{" "}
              {users.length.toLocaleString("vi-VN")} người dùng.
            </p>
          </div>
          <div className="fields-table-chip">
            {loading ? "Đang tải dữ liệu..." : "Đồng bộ backend live"}
          </div>
        </div>

        <AdminTable
          columns={userColumns}
          rows={filteredRows}
          emptyMessage="Không có người dùng nào khớp với bộ lọc hiện tại."
        />
      </section>

      <UserFormModal
        mode="create"
        open={createModalOpen}
        formState={createForm}
        loading={creatingUser}
        error={actionError}
        onClose={closeModals}
        onChange={handleFieldChange(setCreateForm)}
        onSubmit={handleCreateSubmit}
      />

      <UserFormModal
        mode="edit"
        open={editModalOpen}
        formState={editForm}
        loading={editingUser}
        error={actionError}
        onClose={closeModals}
        onChange={handleFieldChange(setEditForm)}
        onSubmit={handleEditSubmit}
      />
    </section>
  );
}
