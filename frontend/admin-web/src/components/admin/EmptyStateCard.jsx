export default function EmptyStateCard({
  title = "Backend is ready:",
  children,
}) {
  return (
    <section className="section-card empty-state">
      <strong>{title}</strong> {children}
    </section>
  );
}
