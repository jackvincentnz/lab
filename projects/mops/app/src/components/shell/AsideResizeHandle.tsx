import { clampAsideWidth } from "./shellState";

export interface AsideResizeHandleProps {
  asideWidth: number;
  setAsideWidth: (width: number) => void;
}

export function AsideResizeHandle({
  asideWidth,
  setAsideWidth,
}: AsideResizeHandleProps) {
  function handleAsideResize(e: React.MouseEvent) {
    e.preventDefault();
    const startX = e.clientX;
    const startWidth = asideWidth;

    const handleMouseMove = (moveEvent: MouseEvent) => {
      setAsideWidth(clampAsideWidth(startWidth - (moveEvent.clientX - startX)));
    };

    const handleMouseUp = () => {
      document.removeEventListener("mousemove", handleMouseMove);
      document.removeEventListener("mouseup", handleMouseUp);
    };

    document.addEventListener("mousemove", handleMouseMove);
    document.addEventListener("mouseup", handleMouseUp);
  }

  return (
    <div
      aria-label="Resize chat panel"
      onMouseDown={handleAsideResize}
      style={{
        position: "absolute",
        left: 0,
        top: 0,
        width: 4,
        height: "100%",
        cursor: "col-resize",
        borderLeft: "1px solid var(--mantine-color-gray-3)",
      }}
      onMouseEnter={(event) => {
        event.currentTarget.style.borderLeft =
          "5px solid var(--mantine-color-blue-5)";
      }}
      onMouseLeave={(event) => {
        event.currentTarget.style.borderLeft =
          "1px solid var(--mantine-color-gray-3)";
      }}
    />
  );
}
