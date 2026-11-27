import { Box } from "@mantine/core";
import type { PanelHandleProps } from "./useResizablePanel";
import classes from "./PanelResizeHandle.module.css";

interface PanelResizeHandleProps {
  handleProps: PanelHandleProps;
}

export function PanelResizeHandle({ handleProps }: PanelResizeHandleProps) {
  return (
    <Box
      {...handleProps}
      aria-label="Resize item details"
      className={classes["handle"]}
      visibleFrom="md"
    />
  );
}
