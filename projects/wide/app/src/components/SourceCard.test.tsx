import {
  describe,
  expect,
  it,
  render,
  screen,
  userEvent,
} from "@lab/test-utils";
import { makeFragmentData } from "../__generated__";
import { SourceFieldsFragmentDoc } from "../__generated__/graphql";
import { link, signalFields } from "../test/records";
import { SourceCard } from "./SourceCard";

describe("SourceCard", () => {
  it("links to the signal and reveals its saved content verbatim", async () => {
    const content = "  Keep\n  spacing  ";
    const signal = makeFragmentData(
      signalFields({ id: "signal-7", content }),
      SourceFieldsFragmentDoc,
    );
    render(<SourceCard link={link({ signal }, "Supports it")} />, {
      route: "/",
    });

    await userEvent.click(screen.getByText("Read saved content"));

    expect(
      screen.getByRole("link", { name: "Synthetic observation" }),
    ).toHaveAttribute("href", "/signals/signal-7");
    expect(screen.getByText(/^Synthetic fixture ·/)).toBeVisible();
    expect(screen.getByText("Supports it")).toBeVisible();
    expect(
      screen.getAllByText(content, { normalizer: (text) => text }).at(-1),
    ).toBeVisible();
  });

  it("says when the signal has no source", () => {
    const signal = makeFragmentData(
      signalFields({ source: null }),
      SourceFieldsFragmentDoc,
    );

    render(<SourceCard link={link({ signal }, "Supports it")} />, {
      route: "/",
    });

    expect(screen.getByText(/^No source recorded ·/)).toBeVisible();
  });
});
