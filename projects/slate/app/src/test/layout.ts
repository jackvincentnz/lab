import { vi } from "@lab/test-utils";

// jsdom has no layout, and dnd-kit finds the next row, and the group a dragged row is over, by position.
export function layOutRowsVertically() {
  vi.spyOn(Element.prototype, "getBoundingClientRect").mockImplementation(
    function (this: Element) {
      const targets = [...document.querySelectorAll("li, [data-empty-group]")];
      const inside = this.matches("[data-group]")
        ? targets.filter((target) => this.contains(target))
        : [this.closest("li, [data-empty-group]")];
      const indexes = inside.map((target) =>
        target ? targets.indexOf(target) : 0,
      );
      const first = Math.min(...indexes);
      const last = Math.max(...indexes);
      return DOMRect.fromRect({
        x: 0,
        y: first * 40,
        width: 400,
        height: (last - first + 1) * 40,
      });
    },
  );
}
