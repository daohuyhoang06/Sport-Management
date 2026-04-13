import ModulePlaceholderPage from "../../components/admin/ModulePlaceholderPage";

const fieldEndpoints = [
  { method: "GET", path: "/api/admin/fields" },
  { method: "GET", path: "/api/admin/fields/stats" },
  { method: "GET", path: "/api/admin/fields/:id" },
  { method: "PATCH", path: "/api/admin/fields/:id/status" },
  { method: "POST", path: "/api/admin/fields/:id/images" },
];

export default function FieldsPage() {
  return (
    <ModulePlaceholderPage
      moduleName="Fields"
      description="Placeholder page for field management. This keeps the route structure in sync with the backend before CRUD is added."
      endpoints={fieldEndpoints}
    />
  );
}
