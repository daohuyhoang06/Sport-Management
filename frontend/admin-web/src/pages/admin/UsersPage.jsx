import ModulePlaceholderPage from "../../components/admin/ModulePlaceholderPage";

const userEndpoints = [
  { method: "GET", path: "/api/admin/users" },
  { method: "GET", path: "/api/admin/users/stats" },
  { method: "GET", path: "/api/admin/users/:id" },
  { method: "PATCH", path: "/api/admin/users/:id/status" },
];

export default function UsersPage() {
  return (
    <ModulePlaceholderPage
      moduleName="Users"
      description="Placeholder page for the user management flow. Day 2 or later will plug in table, filters, and API hooks."
      endpoints={userEndpoints}
    />
  );
}
