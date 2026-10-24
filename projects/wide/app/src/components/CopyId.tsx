import { useState } from "react";
import { icon } from "./icons";

export function CopyId({ id }: { id: string }) {
  const [copied, setCopied] = useState(false);
  return (
    <button
      type="button"
      className="copy-id"
      title={`${id} · copy to paste into your agent session`}
      onClick={() => {
        void navigator.clipboard?.writeText(id);
        setCopied(true);
        window.setTimeout(() => setCopied(false), 1500);
      }}
    >
      {icon.copy}
      {copied ? "Copied" : "Copy ID"}
    </button>
  );
}
