import { useMemo, useState } from "react";
import AdminTable from "../../components/admin/AdminTable";
import useAdminEmployees from "../../hooks/useAdminEmployees";
import useAdminFields from "../../hooks/useAdminFields";
import { adminFetch } from "../../services/adminApi";

const initialEmployeeForm = {
  person_name: "",
  email: "",
  username: "",
  password: "",
  phone: "",
  address: "",
  birthday: "",
  sex: "male",
  status: "active",
};

const statusOptions = [
  { value: "all", label: "Tất cả trạng thái" },
  { value: "active", label: "Hoạt động" },
  { value: "inactive", label: "Không hoạt động" },
];

function renderStatusPill(status) {
  const labels = {
    active: "HOẠT ĐỘNG",
    inactive: "KHÔNG HOẠT ĐỘNG",
  };

  return (
    <span className={`field-status-pill ${status || "inactive"}`}>
      {labels[status] || labels.inactive}
    </span>
  );
}

function renderSexLabel(sex) {
  const labels = {
    male: "Nam",
    female: "Nữ",
    other: "Khác",
  };

  return labels[sex] || sex || "-";
}

function EmployeeFormModal({
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
        className="modal-card modal-card-users modal-card-employees"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="section-head modal-head">
          <div>
            <h3>{isEdit ? "Sửa nhân viên" : "Thêm nhân viên"}</h3>
            <p>
              Giao diện thống nhất với dashboard và đồng bộ trực tiếp từ
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
            <span>Họ và tên</span>
            <input
              type="text"
              name="person_name"
              value={formState.person_name}
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

          {!isEdit && (
            <label className="modal-field">
              <span>Mật khẩu</span>
              <input
                type="password"
                name="password"
                value={formState.password}
                onChange={onChange}
                placeholder="Mật khẩu tạm"
              />
            </label>
          )}

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
              <option value="male">Nam</option>
              <option value="female">Nữ</option>
              <option value="other">Khác</option>
            </select>
          </label>

          <label className="modal-field">
            <span>Trạng thái</span>
            <select name="status" value={formState.status} onChange={onChange}>
              <option value="active">Hoạt động</option>
              <option value="inactive">Không hoạt động</option>
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
                  : "Tạo nhân viên"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function AssignModal({
  open,
  employee,
  fields,
  selectedFieldId,
  loading,
  error,
  onClose,
  onChange,
  onSubmit,
}) {
  if (!open || !employee) {
    return null;
  }

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div
        className="modal-card modal-card-employees"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="section-head modal-head">
          <div>
            <h3>Phân công sân</h3>
            <p>
              {employee.name} - chọn sân để gán quản lý từ danh sách backend.
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
            <span>Nhân viên</span>
            <input type="text" value={employee.name} readOnly />
          </label>

          <label className="modal-field">
            <span>Sân quản lý</span>
            <select name="field_id" value={selectedFieldId} onChange={onChange}>
              {fields.length === 0 ? (
                <option value="">Không có sân khả dụng</option>
              ) : (
                fields.map((field) => (
                  <option key={field.id} value={String(field.id)}>
                    {field.name || field.field_name || `Sân #${field.id}`}
                  </option>
                ))
              )}
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
            <button
              type="submit"
              className="btn-primary"
              disabled={loading || fields.length === 0}
            >
              {loading ? "Đang phân công..." : "Phân công"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

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

  const [searchText, setSearchText] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [assignModalOpen, setAssignModalOpen] = useState(false);
  const [creatingEmployee, setCreatingEmployee] = useState(false);
  const [editingEmployee, setEditingEmployee] = useState(false);
  const [assigning, setAssigning] = useState(false);
  const [actionError, setActionError] = useState("");
  const [actionSuccess, setActionSuccess] = useState("");
  const [selectedEmployee, setSelectedEmployee] = useState(null);
  const [selectedFieldId, setSelectedFieldId] = useState("");
  const [loadingEditEmployeeId, setLoadingEditEmployeeId] = useState(null);
  const [submittingEmployeeId, setSubmittingEmployeeId] = useState(null);
  const [createForm, setCreateForm] = useState(initialEmployeeForm);
  const [editForm, setEditForm] = useState(initialEmployeeForm);

  const filteredRows = useMemo(() => {
    const keyword = searchText.trim().toLowerCase();

    return employees.filter((employee) => {
      const matchesKeyword =
        !keyword ||
        [
          employee.name,
          employee.email,
          employee.phone,
          employee.assignedField,
          employee.role,
        ]
          .join(" ")
          .toLowerCase()
          .includes(keyword);

      const matchesStatus =
        statusFilter === "all" || employee.status === statusFilter;

      return matchesKeyword && matchesStatus;
    });
  }, [employees, searchText, statusFilter]);

  const handleFieldChange = (setter) => (event) => {
    const { name, value } = event.target;
    setter((current) => ({ ...current, [name]: value }));
  };

  const openCreateModal = () => {
    setActionError("");
    setActionSuccess("");
    setCreateForm(initialEmployeeForm);
    setCreateModalOpen(true);
  };

  const closeCreateModal = () => {
    if (creatingEmployee) {
      return;
    }

    setCreateModalOpen(false);
    setCreateForm(initialEmployeeForm);
  };

  const openEditModal = async (employee) => {
    try {
      setLoadingEditEmployeeId(employee.id);
      setActionError("");
      setActionSuccess("");

      const details = await getEmployeeById(employee.id);

      setEditForm({
        person_name: details?.name || employee.name || "",
        email:
          details?.email ||
          (employee.email && employee.email !== "-" ? employee.email : ""),
        username: details?.username || "",
        password: "",
        phone:
          details?.phone ||
          (employee.phone && employee.phone !== "-" ? employee.phone : ""),
        address: details?.address || "",
        birthday: details?.birthday || "",
        sex: details?.sex || "male",
        status: details?.status || employee.status || "active",
      });

      setSelectedEmployee({ ...employee, id: employee.id });
      setEditModalOpen(true);
    } catch (submitError) {
      setActionError(
        submitError.message || "Không thể tải thông tin nhân viên.",
      );
    } finally {
      setLoadingEditEmployeeId(null);
    }
  };

  const closeEditModal = () => {
    if (editingEmployee) {
      return;
    }

    setEditModalOpen(false);
    setSelectedEmployee(null);
    setEditForm(initialEmployeeForm);
  };

  const openAssignModal = (employee) => {
    setActionError("");
    setActionSuccess("");
    setSelectedEmployee(employee);
    setSelectedFieldId(fields[0]?.id ? String(fields[0].id) : "");
    setAssignModalOpen(true);
  };

  const closeAssignModal = () => {
    if (assigning) {
      return;
    }

    setAssignModalOpen(false);
    setSelectedEmployee(null);
    setSelectedFieldId("");
  };

  const handleCreateSubmit = async (event) => {
    event.preventDefault();

    if (
      !createForm.person_name.trim() ||
      !createForm.username.trim() ||
      !createForm.password
    ) {
      setActionError("Vui lòng nhập đầy đủ họ tên, username và mật khẩu.");
      return;
    }

    try {
      setCreatingEmployee(true);
      setActionError("");
      setActionSuccess("");

      await createEmployee({
        name: createForm.person_name.trim(),
        email: createForm.email.trim() || null,
        username: createForm.username.trim(),
        password: createForm.password,
        phone: createForm.phone.trim() || null,
        address: createForm.address.trim() || null,
        birthday: createForm.birthday || null,
        sex: createForm.sex || null,
        status: createForm.status,
      });

      setActionSuccess(
        `Đã tạo nhân viên ${createForm.person_name.trim()} thành công.`,
      );
      closeCreateModal();
    } catch (submitError) {
      setActionError(submitError.message || "Không thể tạo nhân viên.");
    } finally {
      setCreatingEmployee(false);
    }
  };

  const handleEditSubmit = async (event) => {
    event.preventDefault();

    if (!selectedEmployee) {
      return;
    }

    if (!editForm.person_name.trim()) {
      setActionError("Vui lòng nhập họ tên nhân viên.");
      return;
    }

    try {
      setEditingEmployee(true);
      setActionError("");
      setActionSuccess("");

      await updateEmployee(selectedEmployee.id, {
        name: editForm.person_name.trim(),
        email: editForm.email.trim() || null,
        phone: editForm.phone.trim() || null,
        address: editForm.address.trim() || null,
        birthday: editForm.birthday || null,
        sex: editForm.sex || null,
        status: editForm.status,
      });

      setActionSuccess(
        `Đã cập nhật nhân viên ${editForm.person_name.trim()} thành công.`,
      );
      closeEditModal();
    } catch (submitError) {
      setActionError(submitError.message || "Không thể cập nhật nhân viên.");
    } finally {
      setEditingEmployee(false);
    }
  };

  const handleAssignSubmit = async (event) => {
    event.preventDefault();

    if (!selectedEmployee || !selectedFieldId) {
      setActionError("Vui lòng chọn sân trước khi phân công.");
      return;
    }

    try {
      setAssigning(true);
      setActionError("");
      setActionSuccess("");

      await adminFetch("/api/admin/employees/assign-field", {
        method: "POST",
        body: JSON.stringify({
          employeeId: selectedEmployee.id,
          field_id: Number(selectedFieldId),
        }),
      });

      setActionSuccess(
        `Đã phân công sân cho ${selectedEmployee.name} thành công.`,
      );
      await reload();
      closeAssignModal();
    } catch (submitError) {
      setActionError(submitError.message || "Không thể phân công sân.");
    } finally {
      setAssigning(false);
    }
  };

  const handleDeactivateEmployee = async (employee) => {
    const confirmed = window.confirm(`Vô hiệu hóa nhân viên ${employee.name}?`);
    if (!confirmed) {
      return;
    }

    try {
      setSubmittingEmployeeId(employee.id);
      setActionError("");
      setActionSuccess("");
      await deleteEmployee(employee.id);
      setActionSuccess(`Đã vô hiệu hóa nhân viên ${employee.name}.`);
    } catch (submitError) {
      setActionError(submitError.message || "Không thể vô hiệu hóa nhân viên.");
    } finally {
      setSubmittingEmployeeId(null);
    }
  };

  const employeeColumns = useMemo(
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
        key: "assignedField",
        label: "Sân quản lý",
        render: (row) => (
          <span className="employee-field-pill">🏟️ {row.assignedField}</span>
        ),
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
              disabled={
                loadingEditEmployeeId === row.id ||
                submittingEmployeeId === row.id
              }
            >
              ✏️ Sửa
            </button>
            <button
              type="button"
              className="field-action toggle"
              onClick={() => openAssignModal(row)}
              disabled={submittingEmployeeId === row.id || fieldsLoading}
            >
              🏟️ Phân công
            </button>
            <button
              type="button"
              className="field-action delete"
              onClick={() => handleDeactivateEmployee(row)}
              disabled={
                submittingEmployeeId === row.id || row.status === "inactive"
              }
            >
              {submittingEmployeeId === row.id
                ? "Đang xử lý..."
                : "🗑 Vô hiệu hóa"}
            </button>
          </div>
        ),
      },
    ],
    [fieldsLoading, loadingEditEmployeeId, submittingEmployeeId],
  );

  return (
    <section className="users-page employees-page">
      <header className="employees-hero">
        <div className="dashboard-hero-left">
          <div className="dashboard-hero-icon">👔</div>
          <div>
            <p className="dashboard-hero-kicker">Dashboard</p>
            <h2>Quản Lý Nhân Viên</h2>
          </div>
        </div>

        <div className="dashboard-hero-right employees-hero-right">
          <div className="dashboard-role-switcher" aria-label="Vai trò">
            <span className="is-active">👷 Quản trị viên</span>
            <span>📘 Quản lý</span>
            <span>👤 Người dùng</span>
          </div>
          <button
            type="button"
            className="fields-add-btn employees-add-btn"
            onClick={openCreateModal}
          >
            + Thêm Nhân Viên
          </button>
          <div className="dashboard-user-chip">
            <span className="dashboard-user-badge">ADMIN</span>
            <strong>Admin</strong>
          </div>
        </div>
      </header>

      <section className="fields-stats-grid users-stats-grid employees-stats-grid">
        <article
          className="admin-stat-card"
          style={{ ["--accent-color"]: "#1a8f5a" }}
        >
          <div className="admin-stat-copy">
            <p>Tổng nhân viên</p>
            <h3>{stats.total.toLocaleString("vi-VN")}</h3>
            <span>Toàn bộ tài khoản quản lý sân</span>
          </div>
          <div className="admin-stat-icon">👔</div>
        </article>
        <article
          className="admin-stat-card"
          style={{ ["--accent-color"]: "#0f766e" }}
        >
          <div className="admin-stat-copy">
            <p>Đang hoạt động</p>
            <h3>{stats.active.toLocaleString("vi-VN")}</h3>
            <span>Nhân viên đang làm việc</span>
          </div>
          <div className="admin-stat-icon">✅</div>
        </article>
        <article
          className="admin-stat-card"
          style={{ ["--accent-color"]: "#ff8c42" }}
        >
          <div className="admin-stat-copy">
            <p>Không hoạt động</p>
            <h3>{stats.inactive.toLocaleString("vi-VN")}</h3>
            <span>Nhân viên đã vô hiệu hóa</span>
          </div>
          <div className="admin-stat-icon">❌</div>
        </article>
      </section>

      <section className="fields-toolbar card-surface employees-toolbar">
        <div className="fields-search-wrap">
          <label className="fields-search-box">
            <span>🔎</span>
            <input
              type="search"
              value={searchText}
              onChange={(event) => setSearchText(event.target.value)}
              placeholder="Tìm kiếm nhân viên..."
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
      </section>

      {error && <p className="dashboard-state error">{error}</p>}
      {fieldsError && <p className="dashboard-state error">{fieldsError}</p>}
      {actionError && <p className="dashboard-state error">{actionError}</p>}
      {actionSuccess && (
        <p className="dashboard-state success">{actionSuccess}</p>
      )}

      <section className="fields-table-card section-card users-table-card employees-table-card">
        <div className="fields-table-head">
          <div>
            <h3>Danh sách nhân viên</h3>
            <p>
              Hiển thị {filteredRows.length.toLocaleString("vi-VN")} /{" "}
              {employees.length.toLocaleString("vi-VN")} nhân viên.
            </p>
          </div>
          <div className="fields-table-chip">
            {loading ? "Đang tải dữ liệu..." : "Đồng bộ backend live"}
          </div>
        </div>

        <AdminTable
          columns={employeeColumns}
          rows={filteredRows}
          emptyMessage="Không có nhân viên nào khớp với bộ lọc hiện tại."
        />
      </section>

      <EmployeeFormModal
        open={createModalOpen}
        mode="create"
        formState={createForm}
        loading={creatingEmployee}
        error={actionError}
        onClose={closeCreateModal}
        onChange={handleFieldChange(setCreateForm)}
        onSubmit={handleCreateSubmit}
      />

      <EmployeeFormModal
        open={editModalOpen}
        mode="edit"
        formState={editForm}
        loading={editingEmployee}
        error={actionError}
        onClose={closeEditModal}
        onChange={handleFieldChange(setEditForm)}
        onSubmit={handleEditSubmit}
      />

      <AssignModal
        open={assignModalOpen}
        employee={selectedEmployee}
        fields={fields}
        selectedFieldId={selectedFieldId}
        loading={assigning}
        error={actionError}
        onClose={closeAssignModal}
        onChange={(event) => setSelectedFieldId(event.target.value)}
        onSubmit={handleAssignSubmit}
      />
    </section>
  );
}
