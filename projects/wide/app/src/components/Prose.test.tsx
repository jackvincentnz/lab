import { describe, expect, it, render, screen } from "@lab/test-utils";
import { Prose } from "./Prose";

describe("Prose", () => {
  it("renders Markdown, including tables", () => {
    render(<Prose>{"**Bold**\n\n| A |\n| - |\n| 1 |"}</Prose>);

    expect(screen.getByText("Bold").tagName).toBe("STRONG");
    expect(screen.getByRole("table")).toBeVisible();
  });

  it("does not load images from stored text", () => {
    const { container } = render(
      <Prose>
        {
          "![tracking](https://example.test/a.png) ![](https://example.test/b.png)"
        }
      </Prose>,
    );

    expect(container.querySelector("img")).toBeNull();
    expect(screen.getByText("[Image: tracking]")).toBeVisible();
    expect(screen.getByText("[Image: not loaded]")).toBeVisible();
  });

  it("opens links in a new tab without a referrer", () => {
    render(<Prose>{"[Docs](https://example.test/docs)"}</Prose>);

    const link = screen.getByRole("link", { name: "Docs" });
    expect(link).toHaveAttribute("target", "_blank");
    expect(link).toHaveAttribute("rel", "noreferrer");
  });

  it("skips raw HTML", () => {
    const { container } = render(
      <Prose>{'<script>alert("x")</script>Text'}</Prose>,
    );

    expect(container.querySelector("script")).toBeNull();
  });
});
