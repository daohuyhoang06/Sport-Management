export default function ListFilters({
  searchPlaceholder,
  searchText,
  onSearchChange,
  statusFilter,
  onStatusChange,
  totalCount,
  filteredCount,
  hasActiveFilters,
  onResetFilters,
}) {
  return (
    <div className="list-filters-wrap">
      <div className="list-filters">
        <input
          type="search"
          placeholder={searchPlaceholder}
          value={searchText}
          onChange={(event) => onSearchChange(event.target.value)}
        />
        <select
          value={statusFilter}
          onChange={(event) => onStatusChange(event.target.value)}
        >
          <option value="all">All statuses</option>
          <option value="active">Active</option>
          <option value="pending">Pending</option>
          <option value="blocked">Blocked</option>
        </select>
        <button
          type="button"
          className="btn-secondary filter-reset"
          onClick={onResetFilters}
          disabled={!hasActiveFilters}
        >
          Clear
        </button>
      </div>

      <p className="list-filters-summary">
        Showing {filteredCount} of {totalCount} records
      </p>
    </div>
  );
}
