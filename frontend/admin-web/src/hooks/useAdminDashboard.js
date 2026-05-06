import { useEffect, useState } from "react";
import { adminFetch } from "../services/adminApi";

const initialDashboard = {
  totalUsers: 0,
  totalManagers: 0,
  totalFields: 0,
  activeFields: 0,
  totalBookings: 0,
  todayBookings: 0,
  totalRevenue: 0,
  monthlyRevenue: 0,
  activeUsers: 0,
  inactiveUsers: 0,
  pendingBookings: 0,
  confirmedBookings: 0,
  completedBookings: 0,
  cancelledBookings: 0,
  fieldInactive: 0,
  fieldMaintenance: 0,
};

function formatCurrency(value) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(Number(value || 0));
}

export default function useAdminDashboard() {
  const [dashboard, setDashboard] = useState(initialDashboard);
  const [monthlyRevenue, setMonthlyRevenue] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;

    async function loadDashboard() {
      try {
        setLoading(true);
        setError("");

        const [
          dashboardResponse,
          monthlyRevenueResponse,
          usersStatsResponse,
          fieldsStatsResponse,
          bookingsStatsResponse,
        ] = await Promise.all([
          adminFetch("/api/admin/dashboard"),
          adminFetch(
            `/api/admin/revenue/monthly?year=${new Date().getFullYear()}`,
          ),
          adminFetch("/api/admin/users/stats"),
          adminFetch("/api/admin/fields/stats"),
          adminFetch("/api/admin/bookings/stats"),
        ]);

        if (!active) {
          return;
        }

        const data = dashboardResponse?.data ?? dashboardResponse;
        const usersStats = usersStatsResponse?.data ?? {};
        const fieldsStats = fieldsStatsResponse?.data ?? {};
        const bookingsStats = bookingsStatsResponse?.data ?? {};

        setDashboard({
          ...initialDashboard,
          totalUsers: Number(data?.totalUsers ?? 0),
          totalManagers: Number(data?.totalManagers ?? 0),
          totalFields: Number(data?.totalFields ?? 0),
          activeFields: Number(data?.activeFields ?? 0),
          totalBookings: Number(data?.totalBookings ?? 0),
          todayBookings: Number(data?.todayBookings ?? 0),
          totalRevenue: Number(data?.totalRevenue ?? 0),
          monthlyRevenue: Number(data?.monthlyRevenue ?? 0),
          activeUsers: Number(
            usersStats?.active ?? usersStats?.activeUsers ?? 0,
          ),
          inactiveUsers: Number(
            usersStats?.inactive ?? usersStats?.inactiveUsers ?? 0,
          ),
          pendingBookings: Number(bookingsStats?.pending ?? 0),
          confirmedBookings: Number(bookingsStats?.confirmed ?? 0),
          completedBookings: Number(bookingsStats?.completed ?? 0),
          cancelledBookings: Number(bookingsStats?.cancelled ?? 0),
          fieldInactive: Number(fieldsStats?.inactive ?? 0),
          fieldMaintenance: Number(fieldsStats?.maintenance ?? 0),
        });

        setMonthlyRevenue(
          Array.isArray(monthlyRevenueResponse?.data)
            ? monthlyRevenueResponse.data
            : [],
        );
      } catch (fetchError) {
        if (!active) {
          return;
        }

        setError(fetchError.message || "Unable to load dashboard data");
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    loadDashboard();

    return () => {
      active = false;
    };
  }, []);

  const stats = [
    { label: "Users", value: dashboard.totalUsers.toLocaleString("vi-VN") },
    {
      label: "Managers",
      value: dashboard.totalManagers.toLocaleString("vi-VN"),
    },
    {
      label: "Bookings",
      value: dashboard.totalBookings.toLocaleString("vi-VN"),
    },
    { label: "Revenue", value: formatCurrency(dashboard.totalRevenue) },
  ];

  return {
    dashboard,
    monthlyRevenue,
    stats,
    loading,
    error,
    formatCurrency,
  };
}
