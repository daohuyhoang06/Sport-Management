import TableToolbar from "./TableToolbar";

export default function TableSection({
  title,
  subtitle,
  actionLabel,
  children,
}) {
  return (
    <section className="section-card table-card">
      <TableToolbar
        title={title}
        subtitle={subtitle}
        actionLabel={actionLabel}
      />
      {children}
    </section>
  );
}
