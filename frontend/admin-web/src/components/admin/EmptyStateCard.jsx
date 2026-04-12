export default function EmptyStateCard({
  title = "Backend endpoints ready:",
  children,
}) {
  return (
    <section className="section-card empty-state">
      <strong>{title}</strong> {children}
    </section>
  );
}
