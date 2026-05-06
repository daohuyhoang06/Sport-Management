export default function TableToolbar({
  title,
  subtitle,
  actionLabel,
  onAction,
}) {
  return (
    <div className="table-head">
      <div>
        <h3>{title}</h3>
        {subtitle && <p>{subtitle}</p>}
      </div>
      {actionLabel && (
        <button type="button" onClick={onAction}>
          {actionLabel}
        </button>
      )}
    </div>
  );
}
