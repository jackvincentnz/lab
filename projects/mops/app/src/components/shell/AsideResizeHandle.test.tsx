import {
  describe,
  expect,
  fireEvent,
  render,
  screen,
  test,
  vi,
} from "../../test";
import { AsideResizeHandle } from "./AsideResizeHandle";
import { ASIDE_MAX_WIDTH, ASIDE_MIN_WIDTH } from "./shellState";

describe("AsideResizeHandle", () => {
  test("clamps resize updates to the minimum width", () => {
    const setAsideWidth = vi.fn();

    render(
      <AsideResizeHandle
        asideWidth={ASIDE_MIN_WIDTH}
        setAsideWidth={setAsideWidth}
      />,
    );

    fireEvent.mouseDown(screen.getByLabelText("Resize chat panel"), {
      clientX: 100,
    });
    fireEvent.mouseMove(document, { clientX: 101 });
    fireEvent.mouseUp(document);

    expect(setAsideWidth).toHaveBeenCalledWith(ASIDE_MIN_WIDTH);
  });

  test("clamps resize updates to the max width", () => {
    const setAsideWidth = vi.fn();

    render(
      <AsideResizeHandle
        asideWidth={ASIDE_MAX_WIDTH}
        setAsideWidth={setAsideWidth}
      />,
    );

    fireEvent.mouseDown(screen.getByLabelText("Resize chat panel"), {
      clientX: 100,
    });
    fireEvent.mouseMove(document, { clientX: 99 });
    fireEvent.mouseUp(document);

    expect(setAsideWidth).toHaveBeenCalledWith(ASIDE_MAX_WIDTH);
  });

  test("resizes up", () => {
    const setAsideWidth = vi.fn();

    render(
      <AsideResizeHandle
        asideWidth={ASIDE_MIN_WIDTH}
        setAsideWidth={setAsideWidth}
      />,
    );

    fireEvent.mouseDown(screen.getByLabelText("Resize chat panel"), {
      clientX: 100,
    });
    fireEvent.mouseMove(document, { clientX: 99 });
    fireEvent.mouseUp(document);

    expect(setAsideWidth).toHaveBeenCalledWith(ASIDE_MIN_WIDTH + 1);
  });

  test("resizes down", () => {
    const setAsideWidth = vi.fn();

    render(
      <AsideResizeHandle
        asideWidth={ASIDE_MAX_WIDTH}
        setAsideWidth={setAsideWidth}
      />,
    );

    fireEvent.mouseDown(screen.getByLabelText("Resize chat panel"), {
      clientX: 100,
    });
    fireEvent.mouseMove(document, { clientX: 101 });
    fireEvent.mouseUp(document);

    expect(setAsideWidth).toHaveBeenCalledWith(ASIDE_MAX_WIDTH - 1);
  });
});
