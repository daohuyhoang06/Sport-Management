import { useMemo, useState } from "react";
import AdminTable from "../../components/admin/AdminTable";
import useAdminBookings from "../../hooks/useAdminBookings";

const statusOptions = [
  { value: "all", label: "Tất cả trạng thái" },
  { value: "pending", label: "Chờ xác nhận" },
  { value: "confirmed", label: "Đã xác nhận" },
  { value: "completed", label: "Đã hoàn thành" },
  { value: "cancelled", label: "Đã hủy" },
  { value: "rejected", label: "Từ chối" },
];

function renderStatusPill(status) {
  const labels = {
    pending: "CHỜ XÁC NHẬN",
    confirmed: "ĐÃ XÁC NHẬN",
    completed: "HOÀN THÀNH",
    cancelled: "ĐÃ HỦY",
    rejected: "TỪ CHỐI",
  };

  return (
    <span className={`booking-status-pill ${status || "pending"}`}>
      {labels[status] || labels.pending}
    </span>
  );
}

function BookingModal({
  open,
  mode,
  booking,
  selectedStatus,
  noteText,
  cancelReason,
  loading,
  error,
  onClose,
  onSubmit,
  onStatusChange,
  onNoteChange,
  onReasonChange,
}) {
  if (!open || !booking) {
    return null;
  }

  const isCancel = mode === "cancel";

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div
        className="modal-card modal-card-bookings"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="section-head modal-head">
          <div>
            <h3>{isCancel ? "Hủy đặt sân" : "Cập nhật trạng thái đặt sân"}</h3>
            <p>
              #{booking.id} - {booking.customer} - {booking.field}
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
            <span>Mã đặt sân</span>
            <input type="text" value={booking.id} readOnly />
          </label>

          <label className="modal-field">
            <span>Khách hàng</span>
            <input type="text" value={booking.customer} readOnly />
          </label>

          <label className="modal-field">
            <span>Sân</span>
            <input type="text" value={booking.field} readOnly />
          </label>

          {!isCancel && (
            <label className="modal-field">
              <span>Trạng thái</span>
              <select
                value={selectedStatus}
                onChange={(event) => onStatusChange(event.target.value)}
              >
                <option value="pending">Chờ xác nhận</option>
                <option value="confirmed">Đã xác nhận</option>
                <option value="completed">Đã hoàn thành</option>
                <option value="cancelled">Đã hủy</option>
                <option value="rejected">Từ chối</option>
              </select>
            </label>
          )}

          {isCancel ? (
            <label className="modal-field modal-field-full">
              <span>Lý do hủy</span>
              <textarea
                rows="4"
                value={cancelReason}
                onChange={(event) => onReasonChange(event.target.value)}
                placeholder="Nhập lý do hủy đặt sân"
              />
            </label>
          ) : (
            <label className="modal-field modal-field-full">
              <span>Ghi chú</span>
              <textarea
                rows="4"
                value={noteText}
                onChange={(event) => onNoteChange(event.target.value)}
                placeholder="Ghi chú bổ sung cho thao tác cập nhật"
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
                : isCancel
                  ? "Xác nhận hủy"
                  : "Lưu thay đổi"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function BookingActions({ row, onView, onConfirm, onCancel, submittingId }) {
  const isSubmitting = submittingId === row.id;
  const canConfirm = row.status === "pending" || row.status === "confirmed";
  const canCancel = row.status === "pending" || row.status === "confirmed";

  return (
    <div className="booking-actions">
      <button
        type="button"
        className="booking-action view"
        onClick={() => onView(row)}
      >
        👁 Xem
      </button>
      {row.status === "pending" ? (
        <button
          type="button"
          className="booking-action confirm"
          onClick={() => onConfirm(row)}
          disabled={isSubmitting}
        >
          {isSubmitting ? "Đang xử lý..." : "✅ Xác nhận"}
        </button>
      ) : null}
      {canConfirm && row.status === "confirmed" && (
        <button
          type="button"
          className="booking-action confirm"
          onClick={() => onConfirm(row)}
          disabled={isSubmitting}
        >
          {isSubmitting ? "Đang xử lý..." : "✅ Hoàn tất"}
        </button>
      )}
      {canCancel && (
        <button
          type="button"
          className="booking-action cancel"
          onClick={() => onCancel(row)}
          disabled={isSubmitting}
        >
          {isSubmitting ? "Đang xử lý..." : "❌ Hủy"}
        </button>
      )}
    </div>
  );
}

export default function BookingsPage() {
  const {
    bookings,
    stats,
    loading,
    error,
    updateBookingStatus,
    cancelBooking,
  } = useAdminBookings();
  const [searchText, setSearchText] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");
  const [selectedBooking, setSelectedBooking] = useState(null);
  const [modalMode, setModalMode] = useState("status");
  const [selectedStatus, setSelectedStatus] = useState("confirmed");
  const [noteText, setNoteText] = useState("");
  const [cancelReason, setCancelReason] = useState("");
  const [submittingId, setSubmittingId] = useState(null);
  const [actionError, setActionError] = useState("");
  const [actionSuccess, setActionSuccess] = useState("");

  const filteredRows = useMemo(() => {
    const keyword = searchText.trim().toLowerCase();

    return bookings.filter((booking) => {
      const matchesKeyword =
        !keyword ||
        [
          booking.id,
          booking.customer,
          booking.field,
          booking.slot,
          booking.date,
        ]
          .join(" ")
          .toLowerCase()
          .includes(keyword);

      const matchesStatus =
        statusFilter === "all" || booking.status === statusFilter;

      return matchesKeyword && matchesStatus;
    });
  }, [bookings, searchText, statusFilter]);

  const bookingColumns = useMemo(
    () => [
      {
        key: "id",
        label: "ID",
        render: (row) => <span className="field-id-tag">#{row.id}</span>,
      },
      {
        key: "customer",
        label: "Khách hàng",
        render: (row) => (
          <strong className="user-name-cell">{row.customer}</strong>
        ),
      },
      {
        key: "field",
        label: "Sân",
        render: (row) => (
          <span className="booking-field-pill">🏟️ {row.field}</span>
        ),
      },
      {
        key: "date",
        label: "Ngày đặt",
        render: (row) => <span className="user-meta-cell">📅 {row.date}</span>,
      },
      {
        key: "slot",
        label: "Khung giờ",
        render: (row) => <span className="user-meta-cell">🕒 {row.slot}</span>,
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
          <BookingActions
            row={row}
            onView={openViewModal}
            onConfirm={openStatusModal}
            onCancel={openCancelModal}
            submittingId={submittingId}
          />
        ),
      },
    ],
    [submittingId],
  );

  function openViewModal(row) {
    setSelectedBooking(row);
    setModalMode("status");
    setSelectedStatus(
      row.status === "pending" ? "confirmed" : row.status || "confirmed",
    );
    setNoteText("");
    setCancelReason("");
    setActionError("");
    setActionSuccess("");
  }

  function openStatusModal(row) {
    setSelectedBooking(row);
    setModalMode("status");
    setSelectedStatus(
      row.status === "pending"
        ? "confirmed"
        : row.status === "confirmed"
          ? "completed"
          : "confirmed",
    );
    setNoteText("");
    setCancelReason("");
    setActionError("");
    setActionSuccess("");
  }

  function openCancelModal(row) {
    setSelectedBooking(row);
    setModalMode("cancel");
    setSelectedStatus("cancelled");
    setNoteText("");
    setCancelReason("");
    setActionError("");
    setActionSuccess("");
  }

  function closeModal() {
    if (submittingId) {
      return;
    }

    setSelectedBooking(null);
    setModalMode("status");
    setSelectedStatus("confirmed");
    setNoteText("");
    setCancelReason("");
    setActionError("");
    setActionSuccess("");
  }

  async function handleModalSubmit(event) {
    event.preventDefault();

    if (!selectedBooking) {
      return;
    }

    try {
      setSubmittingId(selectedBooking.id);
      setActionError("");
      setActionSuccess("");

      if (modalMode === "cancel") {
        if (!cancelReason.trim()) {
          setActionError("Vui lòng nhập lý do hủy đặt sân.");
          return;
        }

        await cancelBooking(selectedBooking.id, cancelReason.trim());
        setActionSuccess(`Đã hủy đặt sân #${selectedBooking.id} thành công.`);
      } else {
        await updateBookingStatus(
          selectedBooking.id,
          selectedStatus,
          noteText.trim(),
        );
        setActionSuccess(
          `Đã cập nhật đặt sân #${selectedBooking.id} thành ${selectedStatus}.`,
        );
      }

      closeModal();
    } catch (submitError) {
      setActionError(submitError.message || "Không thể cập nhật đặt sân.");
    } finally {
      setSubmittingId(null);
    }
  }

  return (
    <section className="bookings-page">
      <header className="bookings-hero">
        <div className="dashboard-hero-left">
          <div className="dashboard-hero-icon">📋</div>
          <div>
            <p className="dashboard-hero-kicker">Dashboard</p>
            <h2>Quản Lý Đặt Sân</h2>
          </div>
        </div>

        <div className="dashboard-hero-right bookings-hero-right">
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

      <section className="fields-stats-grid bookings-stats-grid">
        <article
          className="admin-stat-card"
          style={{ ["--accent-color"]: "#6b7cff" }}
        >
          <div className="admin-stat-copy">
            <p>Tổng đặt sân</p>
            <h3>{stats.total.toLocaleString("vi-VN")}</h3>
            <span>Toàn bộ đơn đặt trong hệ thống</span>
          </div>
          <div className="admin-stat-icon">📋</div>
        </article>
        <article
          className="admin-stat-card"
          style={{ ["--accent-color"]: "#f59e0b" }}
        >
          <div className="admin-stat-copy">
            <p>Chờ xác nhận</p>
            <h3>{stats.pending.toLocaleString("vi-VN")}</h3>
            <span>Đơn cần xử lý tiếp</span>
          </div>
          <div className="admin-stat-icon">⏳</div>
        </article>
        <article
          className="admin-stat-card"
          style={{ ["--accent-color"]: "#10b981" }}
        >
          <div className="admin-stat-copy">
            <p>Đã xác nhận</p>
            <h3>{stats.confirmed.toLocaleString("vi-VN")}</h3>
            <span>Đơn đã được duyệt</span>
          </div>
          <div className="admin-stat-icon">✅</div>
        </article>
        <article
          className="admin-stat-card"
          style={{ ["--accent-color"]: "#8b5cf6" }}
        >
          <div className="admin-stat-copy">
            <p>Đã hoàn thành</p>
            <h3>{stats.completed.toLocaleString("vi-VN")}</h3>
            <span>Đơn đã kết thúc</span>
          </div>
          <div className="admin-stat-icon">🏆</div>
        </article>
      </section>

      <section className="fields-toolbar card-surface bookings-toolbar">
        <div className="fields-search-wrap bookings-search-wrap">
          <label className="fields-search-box bookings-search-box">
            <span>🔎</span>
            <input
              type="search"
              value={searchText}
              onChange={(event) => setSearchText(event.target.value)}
              placeholder="Tìm kiếm đặt sân..."
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
      {actionError && <p className="dashboard-state error">{actionError}</p>}
      {actionSuccess && (
        <p className="dashboard-state success">{actionSuccess}</p>
      )}

      <section className="fields-table-card section-card bookings-table-card">
        <div className="fields-table-head">
          <div>
            <h3>Danh sách đặt sân</h3>
            <p>
              Hiển thị {filteredRows.length.toLocaleString("vi-VN")} /{" "}
              {bookings.length.toLocaleString("vi-VN")} đơn đặt sân.
            </p>
          </div>
          <div className="fields-table-chip">
            {loading ? "Đang tải dữ liệu..." : "Đồng bộ backend live"}
          </div>
        </div>

        <AdminTable
          columns={bookingColumns}
          rows={filteredRows}
          emptyMessage="Không có đặt sân nào khớp với bộ lọc hiện tại."
        />
      </section>

      <BookingModal
        open={Boolean(selectedBooking)}
        mode={modalMode}
        booking={selectedBooking}
        selectedStatus={selectedStatus}
        noteText={noteText}
        cancelReason={cancelReason}
        loading={Boolean(submittingId)}
        error={actionError}
        onClose={closeModal}
        onSubmit={handleModalSubmit}
        onStatusChange={setSelectedStatus}
        onNoteChange={setNoteText}
        onReasonChange={setCancelReason}
      />
    </section>
  );
}
