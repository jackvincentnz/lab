import { Link, NavLink } from "react-router-dom";
import { kinds, labels } from "../lib/kinds";
import { icon } from "./icons";

export function Sidebar({ onOpenGuides }: { onOpenGuides: () => void }) {
  return (
    <aside className="sidebar">
      <Link className="brand" to="/signals">
        <span className="brand-mark" aria-hidden="true">
          W
        </span>
        WIDE
      </Link>
      <nav aria-label="Explore WIDE">
        {kinds.map((kind) => (
          <NavLink key={kind} to={`/${kind}`}>
            {icon[kind]}
            {labels[kind]}
          </NavLink>
        ))}
        <NavLink to="/strategy">
          {icon.strategy}
          Strategy
        </NavLink>
      </nav>
      <div className="sidebar-footer">
        <button type="button" className="guides" onClick={onOpenGuides}>
          {icon.guide}
          Refinement guides
        </button>
        <span className="sidebar-note">Local explorer · read-only</span>
      </div>
    </aside>
  );
}
