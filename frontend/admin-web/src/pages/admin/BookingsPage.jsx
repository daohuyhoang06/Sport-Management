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
  { method: "PATCH", path: "/api/admin/bookings/:id/cancel" },
];

const bookingColumns = [
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
];

export default function BookingsPage() {
  const { bookings, stats, loading, error } = useAdminBookings();

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
    </section>
  );
}
