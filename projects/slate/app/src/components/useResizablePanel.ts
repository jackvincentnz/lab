import { type RefCallback, useLayoutEffect, useRef, useState } from "react";
import { useSplitter, useWindowEvent } from "@mantine/hooks";
import {
  DEFAULT_PANEL_WIDTH,
  MIN_PANEL_WIDTH,
  maxPanelWidth,
  readSavedPanelWidth,
  savePanelWidth,
  shownPanelWidth,
} from "../panelWidth";

export type PanelHandleProps = ReturnType<
  useSplitter.ReturnValue["getHandleProps"]
>;

interface ResizablePanel {
  containerRef: RefCallback<HTMLDivElement | null>;
  width: number;
  handleProps: PanelHandleProps;
}

export function useResizablePanel(): ResizablePanel {
  const [windowWidth, setWindowWidth] = useState(() => window.innerWidth);
  const [saved, setSaved] = useState(readSavedPanelWidth);
  const width = shownPanelWidth(saved, windowWidth);
  const maxWidth = maxPanelWidth(windowWidth);
  // useSplitter keeps the onSizeChange from the first render for pointer drags.
  const shownWidth = useRef(width);

  useLayoutEffect(() => {
    shownWidth.current = width;
  });

  useWindowEvent("resize", () => setWindowWidth(window.innerWidth));

  function save(next: number) {
    setSaved(next);
    savePanelWidth(next);
  }

  const splitter = useSplitter({
    panels: [
      { defaultSize: 100 },
      {
        defaultSize: `${DEFAULT_PANEL_WIDTH}px`,
        min: `${MIN_PANEL_WIDTH}px`,
        max: `${maxWidth}px`,
      },
    ],
    sizes: [100, `${width}px`],
    onSizeChange: ([, panel]) => {
      const next = Math.round(parseFloat(String(panel)));
      // A click on the edge reports the shown width, which would replace a saved width that is narrowed to fit the window.
      if (next !== shownWidth.current) {
        save(next);
      }
    },
    step: "10px",
    shiftStep: "50px",
  });

  return {
    containerRef: splitter.ref,
    width,
    handleProps: {
      ...splitter.getHandleProps({ index: 0 }),
      "aria-valuenow": width,
      "aria-valuemin": MIN_PANEL_WIDTH,
      "aria-valuemax": maxWidth,
      onDoubleClick: () => save(DEFAULT_PANEL_WIDTH),
    },
  };
}
