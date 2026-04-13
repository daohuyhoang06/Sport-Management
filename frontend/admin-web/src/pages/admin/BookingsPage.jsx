import ModulePlaceholderPage from "../../components/admin/ModulePlaceholderPage";

const bookingEndpoints = [
  { method: "GET", path: "/api/admin/bookings" },
  { method: "GET", path: "/api/admin/bookings/stats" },
  { method: "GET", path: "/api/admin/bookings/date-range" },
  { method: "GET", path: "/api/admin/bookings/:id" },
  { method: "PATCH", path: "/api/admin/bookings/:id/status" },
  { method: "PATCH", path: "/api/admin/bookings/:id/cancel" },
];

export default function BookingsPage() {
  return (
    <ModulePlaceholderPage
      moduleName="Bookings"
      description="Placeholder page for booking control. The future UI will connect to the existing backend endpoints without changing this shell."
      endpoints={bookingEndpoints}
    />
  );
}
