import AdminTable from "../../components/admin/AdminTable";
import EndpointPanel from "../../components/admin/EndpointPanel";
import ListFilters from "../../components/admin/ListFilters";
import PageHero from "../../components/admin/PageHero";
import StatusPill from "../../components/admin/StatusPill";
import TableSection from "../../components/admin/TableSection";
import useListFilters from "../../hooks/useListFilters";

const fieldEndpoints = [
  { method: "GET", path: "/api/admin/fields" },
  { method: "GET", path: "/api/admin/fields/stats" },
  { method: "GET", path: "/api/admin/fields/:id" },
  { method: "PATCH", path: "/api/admin/fields/:id/status" },
  { method: "POST", path: "/api/admin/fields/:id/images" },
];

const fieldRows = [
  {
    id: 1,
    name: "San A1",
    location: "Quan 7",
    type: "5v5",
    pricePerHour: "450.000",
    status: "active",
  },
  {
    id: 2,
    name: "San B2",
    location: "Thu Duc",
    type: "7v7",
    pricePerHour: "680.000",
    status: "pending",
  },
  {
    id: 3,
    name: "San C3",
    location: "Binh Thanh",
    type: "11v11",
    pricePerHour: "1.200.000",
    status: "blocked",
  },
];

const fieldColumns = [
  { key: "name", label: "Field" },
  { key: "location", label: "Location" },
  { key: "type", label: "Type" },
  {
    key: "pricePerHour",
    label: "Price / hour",
    render: (row) => `${row.pricePerHour} VND`,
  },
  {
    key: "status",
    label: "Status",
    render: (row) => <StatusPill status={row.status} />,
  },
];

export default function FieldsPage() {
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
    rows: fieldRows,
    searchFields: ["name", "location", "type"],
  });

  return (
    <section className="page-shell">
      <PageHero
        badges={["Admin module", "Fields"]}
        title="Fields"
        description="Field inventory scaffold with mock rows. Next step can replace this with real listing, search, and status updates from backend APIs."
      />

      <TableSection
        title="Field list (mock data)"
        subtitle="Quick preview of how field records will render in admin."
        actionLabel="Add field"
      >
        <ListFilters
          searchPlaceholder="Search by field, location, or type"
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
          columns={fieldColumns}
          rows={filteredRows}
          emptyMessage="No fields match the current filters."
        />
      </TableSection>

      <EndpointPanel title="Fields endpoints" endpoints={fieldEndpoints} />
    </section>
  );
}
