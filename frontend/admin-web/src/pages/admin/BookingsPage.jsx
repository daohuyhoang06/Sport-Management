import { useMemo, useState } from "react";
import AdminTable from "../../components/admin/AdminTable";
import EndpointPanel from "../../components/admin/EndpointPanel";
import ListFilters from "../../components/admin/ListFilters";
import PageHero from "../../components/admin/PageHero";
import StatusPill from "../../components/admin/StatusPill";
import TableSection from "../../components/admin/TableSection";
import useAdminBookings from "../../hooks/useAdminBookings";
import useListFilters from "../../hooks/useListFilters";

const bookingEndpoints = [
  { method: "GET", path: "/api/admin/bookings" },
  { method: "GET", path: "/api/admin/bookings/stats" },
  { method: "GET", path: "/api/admin/bookings/date-range" },
  { method: "GET", path: "/api/admin/bookings/:id" },
  { method: "PATCH", path: "/api/admin/bookings/:id/status" },
  { method: "POST", path: "/api/admin/bookings/:id/cancel" },
];

function BookingActions({
  row,
  onUpdateStatus,
  onCancelBooking,
  submittingId,
}) {
  const isSubmitting = submittingId === row.id;

  return (
    <div className="table-actions booking-actions">
      <button
        type="button"
        className="btn-primary"
        onClick={() => onUpdateStatus(row)}
        disabled={isSubmitting}
      >
        {isSubmitting ? "Updating..." : "Update status"}
      </button>
      <button
        type="button"
        className="btn-secondary"
        onClick={() => onCancelBooking(row)}
        disabled={isSubmitting}
      >
        Cancel
      </button>
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
  const [selectedBooking, setSelectedBooking] = useState(null);
  const [modalMode, setModalMode] = useState("status");
  const [selectedStatus, setSelectedStatus] = useState("confirmed");
  const [noteText, setNoteText] = useState("");
  const [cancelReason, setCancelReason] = useState("");
  const [submittingId, setSubmittingId] = useState(null);
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
    rows: bookings,
    searchFields: ["id", "customer", "field"],
  });

  const bookingColumns = useMemo(
    () => [
      { key: "id", label: "Booking ID" },
      { key: "customer", label: "Customer" },
      { key: "field", label: "Field" },
      { key: "slot", label: "Time slot" },
      { key: "date", label: "Date" },
      {
        key: "status",
        label: "Status",
        render: (row) => <StatusPill status={row.status} />,
      },
      {
        key: "actions",
        label: "Actions",
        render: (row) => (
          <BookingActions
            row={row}
            submittingId={submittingId}
            onUpdateStatus={openStatusModal}
            onCancelBooking={openCancelModal}
          />
        ),
      },
    ],
    [submittingId],
  );

  function openStatusModal(row) {
    setSelectedBooking(row);
    setModalMode("status");
    setSelectedStatus(row.status === "pending" ? "confirmed" : "completed");
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
          setActionError("Please provide a cancellation reason.");
          return;
        }

        await cancelBooking(selectedBooking.id, cancelReason.trim());
        setActionSuccess(`Booking ${selectedBooking.id} was cancelled.`);
      } else {
        await updateBookingStatus(
          selectedBooking.id,
          selectedStatus,
          noteText.trim(),
        );
        setActionSuccess(
          `Booking ${selectedBooking.id} updated to ${selectedStatus}.`,
        );
      }

      closeModal();
    } catch (submitError) {
      setActionError(submitError.message || "Unable to update booking");
    } finally {
      setSubmittingId(null);
    }
  }

  return (
    <section className="page-shell">
      <PageHero
        badges={[
          "Admin module",
          "Bookings",
          loading ? "Loading from backend" : `${stats.total} total bookings`,
        ]}
        title="Bookings"
        description="Bookings page now uses backend list and stats endpoints so the admin web follows live booking states from database records."
      />

      <TableSection
        title="Bookings list"
        subtitle="Live data from /api/admin/bookings and /api/admin/bookings/stats."
        actionLabel="Create booking"
      >
        {error && <p className="dashboard-state error">{error}</p>}
        {actionError && <p className="dashboard-state error">{actionError}</p>}
        {actionSuccess && (
          <p className="dashboard-state success">{actionSuccess}</p>
        )}

        <ListFilters
          searchPlaceholder="Search by booking ID, customer, or field"
          searchText={searchText}
          onSearchChange={setSearchText}
          statusFilter={statusFilter}
          onStatusChange={setStatusFilter}
          totalCount={totalCount}
          filteredCount={filteredCount}
          hasActiveFilters={hasActiveFilters}
          onResetFilters={resetFilters}
          statusOptions={[
            "pending",
            "confirmed",
            "completed",
            "cancelled",
            "rejected",
          ]}
        />

        <AdminTable
          columns={bookingColumns}
          rows={filteredRows}
          emptyMessage="No bookings match the current filters."
        />
      </TableSection>

      <EndpointPanel title="Bookings endpoints" endpoints={bookingEndpoints} />

      {selectedBooking && (
        <div className="modal-backdrop" onClick={closeModal}>
          <div
            className="modal-card"
            onClick={(event) => event.stopPropagation()}
          >
            <div className="section-head modal-head">
              <div>
                <h3>
                  {modalMode === "cancel"
                    ? "Cancel booking"
                    : "Update booking status"}
                </h3>
                <p>
                  {selectedBooking.id} - {selectedBooking.customer} -{" "}
                  {selectedBooking.field}
                </p>
              </div>
              <button
                type="button"
                className="btn-secondary"
                onClick={closeModal}
                disabled={Boolean(submittingId)}
              >
                Close
              </button>
            </div>

            <form className="modal-form" onSubmit={handleModalSubmit}>
              <label className="modal-field">
                <span>Booking</span>
                <input type="text" value={selectedBooking.id} readOnly />
              </label>

              {modalMode === "cancel" ? (
                <label className="modal-field">
                  <span>Cancellation reason</span>
                  <textarea
                    rows="4"
                    value={cancelReason}
                    onChange={(event) => setCancelReason(event.target.value)}
                    placeholder="Enter the reason for cancellation"
                  />
                </label>
              ) : (
                <>
                  <label className="modal-field">
                    <span>Status</span>
                    <select
                      value={selectedStatus}
                      onChange={(event) =>
                        setSelectedStatus(event.target.value)
                      }
                    >
                      <option value="pending">Pending</option>
                      <option value="confirmed">Confirmed</option>
                      <option value="completed">Completed</option>
                      <option value="cancelled">Cancelled</option>
                      <option value="rejected">Rejected</option>
                    </select>
                  </label>

                  <label className="modal-field">
                    <span>Note</span>
                    <textarea
                      rows="4"
                      value={noteText}
                      onChange={(event) => setNoteText(event.target.value)}
                      placeholder="Optional note for the booking update"
                    />
                  </label>
                </>
              )}

              <div className="modal-actions">
                <button
                  type="button"
                  className="btn-secondary"
                  onClick={closeModal}
                  disabled={Boolean(submittingId)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn-primary"
                  disabled={Boolean(submittingId)}
                >
                  {submittingId
                    ? "Saving..."
                    : modalMode === "cancel"
                      ? "Confirm cancel"
                      : "Save changes"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </section>
  );
}
