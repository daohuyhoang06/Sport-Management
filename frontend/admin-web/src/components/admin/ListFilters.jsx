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
  statusOptions = ["active", "pending", "blocked"],
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
          {statusOptions.map((option) => (
            <option key={option} value={option}>
              {option.charAt(0).toUpperCase() + option.slice(1)}
            </option>
          ))}
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
