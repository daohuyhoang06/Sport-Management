import AdminTable from "../../components/admin/AdminTable";
import EndpointPanel from "../../components/admin/EndpointPanel";
import ListFilters from "../../components/admin/ListFilters";
import PageHero from "../../components/admin/PageHero";
import StatusPill from "../../components/admin/StatusPill";
import TableSection from "../../components/admin/TableSection";
import useAdminFields from "../../hooks/useAdminFields";
import useListFilters from "../../hooks/useListFilters";

const fieldEndpoints = [
  { method: "GET", path: "/api/admin/fields" },
  { method: "GET", path: "/api/admin/fields/stats" },
  { method: "GET", path: "/api/admin/fields/:id" },
  { method: "PATCH", path: "/api/admin/fields/:id/status" },
  { method: "POST", path: "/api/admin/fields/:id/images" },
];

const fieldColumns = [
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
];

export default function FieldsPage() {
  const { fields, stats, loading, error } = useAdminFields();

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

      <TableSection
        title="Field list"
        subtitle="Live data from /api/admin/fields and /api/admin/fields/stats."
        actionLabel="Add field"
      >
        {error && <p className="dashboard-state error">{error}</p>}

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
      </TableSection>

      <EndpointPanel title="Fields endpoints" endpoints={fieldEndpoints} />
    </section>
  );
}
