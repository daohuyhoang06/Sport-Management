/**
 * StatsGrid Component
 * Displays statistics in a responsive grid layout
 *
 * @component
 * @param {Object} props
 * @param {Array<{label: string, value: string|number}>} props.stats - Statistics to display
 * @returns {JSX.Element}
 *
 * @example
 * <StatsGrid stats={[
 *   { label: "Total Revenue", value: "$45,234" },
 *   { label: "Bookings", value: "128" }
 * ]} />
 */
export default function StatsGrid({ stats = [] }) {
  return (
    <div className="stats-grid">
      {stats.map((stat, index) => (
        <div key={index} className="stat-card">
          <div className="stat-label">{stat.label}</div>
          <div className="stat-value">{stat.value}</div>
        </div>
      ))}
    </div>
  );
}
