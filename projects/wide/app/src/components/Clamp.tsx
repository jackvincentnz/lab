import { useState } from "react";

// Long free text stays available but does not push the record down the page.
export function Clamp({
  children,
  limit = 240,
}: {
  children: string;
  limit?: number;
}) {
  const [open, setOpen] = useState(false);
  const long = children.length > limit;
  return (
    <div className="clamp">
      <p className={long && !open ? "clamp-text clamped" : "clamp-text"}>
        {children}
      </p>
      {long && (
        <button
          type="button"
          className="clamp-toggle"
          aria-expanded={open}
          onClick={() => setOpen((value) => !value)}
        >
          {open ? "Show less" : "Show all"}
        </button>
      )}
    </div>
  );
}
