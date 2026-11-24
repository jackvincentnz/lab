import { vi } from "@lab/test-utils";

// jsdom has no layout, and dnd-kit's keyboard sensor finds the next row by its position.
export function layOutRowsVertically() {
  vi.spyOn(Element.prototype, "getBoundingClientRect").mockImplementation(
    function (this: Element) {
      const row = this.closest("li");
      const index = row ? [...row.parentElement!.children].indexOf(row) : 0;
      return DOMRect.fromRect({ x: 0, y: index * 40, width: 400, height: 40 });
    },
  );
}
