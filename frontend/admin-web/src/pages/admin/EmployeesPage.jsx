import ModulePlaceholderPage from "../../components/admin/ModulePlaceholderPage";

const employeeEndpoints = [
  { method: "GET", path: "/api/admin/employees" },
  { method: "GET", path: "/api/admin/employees/stats" },
  { method: "GET", path: "/api/admin/employees/:id" },
  { method: "POST", path: "/api/admin/employees/assign-field" },
];

export default function EmployeesPage() {
  return (
    <ModulePlaceholderPage
      moduleName="Employees"
      description="Placeholder page for employee management and field assignment. It is only the scaffold for the first commit."
      endpoints={employeeEndpoints}
    />
  );
}
