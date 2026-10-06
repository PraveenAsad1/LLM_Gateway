export default function StatBlock({ title, value, showFailover }) {
  return (
    <div className="stat-block">
      <p className="stat-value mono">
        {value}
        {showFailover && <span className="failover-dot" title="Failover event detected in these requests"></span>}
      </p>
      <h3>{title}</h3>
    </div>
  )
}
