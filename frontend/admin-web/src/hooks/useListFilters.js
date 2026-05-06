import { useMemo, useState } from "react";

export default function useListFilters({ rows = [], searchFields = [] }) {
  const [searchText, setSearchText] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");

  const filteredRows = useMemo(() => {
    const normalizedSearch = searchText.trim().toLowerCase();

    return rows.filter((row) => {
      const matchesStatus =
        statusFilter === "all" ? true : row.status === statusFilter;

      if (!matchesStatus) {
        return false;
      }

      if (!normalizedSearch) {
        return true;
      }

      return searchFields.some((field) => {
        const value = row[field];
        return String(value ?? "")
          .toLowerCase()
          .includes(normalizedSearch);
      });
    });
  }, [rows, searchFields, searchText, statusFilter]);

  const hasActiveFilters =
    searchText.trim().length > 0 || statusFilter !== "all";

  const resetFilters = () => {
    setSearchText("");
    setStatusFilter("all");
  };

  return {
    searchText,
    setSearchText,
    statusFilter,
    setStatusFilter,
    filteredRows,
    filteredCount: filteredRows.length,
    totalCount: rows.length,
    hasActiveFilters,
    resetFilters,
  };
}
