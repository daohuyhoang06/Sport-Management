import { useMemo, useState } from "react";
import AdminTable from "../../components/admin/AdminTable";
import useAdminSportTypes from "../../hooks/useAdminSportTypes";

const initialForm = { sport_name: "" };

function SportFormModal({
  mode,
  open,
  formState,
  loading,
  error,
  onClose,
  onChange,
  onSubmit,
}) {
  if (!open) return null;

  const title = mode === "edit" ? "Sửa loại sân" : "Thêm loại sân";

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-card" onClick={(e) => e.stopPropagation()}>
        <div className="section-head modal-head">
          <div>
            <h3>{title}</h3>
            <p>Quản lý các loại sân sử dụng trong hệ thống.</p>
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
            <span>Tên loại sân</span>
            <input
              name="sport_name"
              value={formState.sport_name}
              onChange={onChange}
            />
          </label>

          <div className="modal-actions">
            <button
              type="button"
              className="btn-secondary"
              onClick={onClose}
              disabled={loading}
            >
              Hủy
            </button>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? "Đang..." : "Lưu"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default function SportTypesPage() {
  const { types, loading, error, createType, updateType, deleteType } =
    useAdminSportTypes();

  const [createOpen, setCreateOpen] = useState(false);
  const [editOpen, setEditOpen] = useState(false);
  const [form, setForm] = useState(initialForm);
  const [editing, setEditing] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [actionError, setActionError] = useState("");

  const rows = useMemo(
    () => types.map((t) => ({ id: t.id, name: t.name })),
    [types],
  );

  const openCreate = () => {
    setActionError("");
    setForm(initialForm);
    setCreateOpen(true);
  };

  const openEdit = (row) => {
    setActionError("");
    setEditing(row);
    setForm({ sport_name: row.name || "" });
    setEditOpen(true);
  };

  const closeAll = () => {
    if (submitting) return;
    setCreateOpen(false);
    setEditOpen(false);
    setEditing(null);
    setForm(initialForm);
  };

  const handleChange = (e) =>
    setForm((s) => ({ ...s, [e.target.name]: e.target.value }));

  const handleCreate = async (e) => {
    e.preventDefault();
    if (!form.sport_name.trim()) {
      setActionError("Tên loại sân không được để trống.");
      return;
    }

    try {
      setSubmitting(true);
      setActionError("");
      await createType({ sport_name: form.sport_name.trim() });
      closeAll();
    } catch (err) {
      setActionError(err.message || "Không thể tạo loại sân.");
    } finally {
      setSubmitting(false);
    }
  };

  const handleEdit = async (e) => {
    e.preventDefault();
    if (!editing) return;
    if (!form.sport_name.trim()) {
      setActionError("Tên loại sân không được để trống.");
      return;
    }

    try {
      setSubmitting(true);
      setActionError("");
      await updateType(editing.id, { sport_name: form.sport_name.trim() });
      closeAll();
    } catch (err) {
      setActionError(err.message || "Không thể cập nhật loại sân.");
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (row) => {
    const ok = window.confirm(`Xóa loại sân '${row.name}'?`);
    if (!ok) return;

    try {
      setSubmitting(true);
      setActionError("");
      await deleteType(row.id);
    } catch (err) {
      setActionError(err.message || "Không thể xóa loại sân.");
    } finally {
      setSubmitting(false);
    }
  };

  const columns = [
    { key: "id", label: "ID" },
    { key: "name", label: "Tên loại" },
    {
      key: "actions",
      label: "Thao tác",
      render: (r) => (
        <div className="field-actions">
          <button className="field-action edit" onClick={() => openEdit(r)}>
            ✏️ Sửa
          </button>
          <button
            className="field-action delete"
            onClick={() => handleDelete(r)}
            disabled={submitting}
          >
            🗑 Xóa
          </button>
        </div>
      ),
    },
  ];

  return (
    <section className="sport-types-page">
      <header className="section-head">
        <div>
          <p className="dashboard-hero-kicker">Quản trị</p>
          <h2>Loại Sân</h2>
        </div>
        <div>
          <button className="btn-primary" onClick={openCreate}>
            + Thêm loại sân
          </button>
        </div>
      </header>

      {error && <p className="dashboard-state error">{error}</p>}
      {actionError && <p className="dashboard-state error">{actionError}</p>}

      <section className="section-card">
        <AdminTable
          columns={columns}
          rows={rows}
          emptyMessage="Không có loại sân."
        />
      </section>

      <SportFormModal
        mode="create"
        open={createOpen}
        formState={form}
        loading={submitting}
        error={actionError}
        onClose={closeAll}
        onChange={handleChange}
        onSubmit={handleCreate}
      />
      <SportFormModal
        mode="edit"
        open={editOpen}
        formState={form}
        loading={submitting}
        error={actionError}
        onClose={closeAll}
        onChange={handleChange}
        onSubmit={handleEdit}
      />
    </section>
  );
}
