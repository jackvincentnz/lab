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
import { ASIDE_MIN_WIDTH } from "./shellState";

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
    fireEvent.mouseMove(document, { clientX: 350 });
    fireEvent.mouseUp(document);

    expect(setAsideWidth).toHaveBeenCalledWith(ASIDE_MIN_WIDTH);
  });
});
