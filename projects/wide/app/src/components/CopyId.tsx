import { useState } from "react";
import { icon } from "./icons";

interface CopyIdProps {
  id: string;
}

export function CopyId({ id }: CopyIdProps) {
  const [feedback, setFeedback] = useState<string>();
  const show = (text: string) => {
    setFeedback(text);
    window.setTimeout(() => setFeedback(undefined), 1500);
  };
  const copy = async () => {
    try {
      // Outside a secure context navigator.clipboard is undefined, so this throws too.
      await navigator.clipboard.writeText(id);
      show("Copied");
    } catch {
      show("Copy failed");
    }
  };
  return (
    <button
      type="button"
      className="copy-id"
      title={`${id} · copy to paste into your agent session`}
      onClick={() => void copy()}
    >
      {icon.copy}
      {feedback || "Copy ID"}
    </button>
  );
}
