import AdminTable from "../../components/admin/AdminTable";
import EndpointPanel from "../../components/admin/EndpointPanel";
import ListFilters from "../../components/admin/ListFilters";
import PageHero from "../../components/admin/PageHero";
import StatusPill from "../../components/admin/StatusPill";
import TableSection from "../../components/admin/TableSection";
import useListFilters from "../../hooks/useListFilters";

const bookingEndpoints = [
  { method: "GET", path: "/api/admin/bookings" },
  { method: "GET", path: "/api/admin/bookings/stats" },
  { method: "GET", path: "/api/admin/bookings/date-range" },
  { method: "GET", path: "/api/admin/bookings/:id" },
  { method: "PATCH", path: "/api/admin/bookings/:id/status" },
  { method: "PATCH", path: "/api/admin/bookings/:id/cancel" },
];

const bookingRows = [
  {
    id: "BK-001",
    customer: "Pham Quoc Dat",
    field: "San A1",
    slot: "19:00 - 20:30",
    date: "2026-04-14",
    status: "active",
  },
  {
    id: "BK-002",
    customer: "Le Nhat Linh",
    field: "San B2",
    slot: "20:30 - 22:00",
    date: "2026-04-14",
    status: "pending",
  },
  {
    id: "BK-003",
    customer: "Tran Gia Bao",
    field: "San C3",
    slot: "17:30 - 19:00",
    date: "2026-04-15",
    status: "blocked",
  },
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
    rows: bookingRows,
    searchFields: ["id", "customer", "field"],
  });

  return (
    <section className="page-shell">
      <PageHero
        badges={["Admin module", "Bookings"]}
        title="Bookings"
        description="Booking management scaffold with mock list. This can be connected to filters, date-range API, and actions in the next step."
      />

      <TableSection
        title="Bookings list (mock data)"
        subtitle="Snapshot view to verify columns, spacing, and statuses."
        actionLabel="Create booking"
      >
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
