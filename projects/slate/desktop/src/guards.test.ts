import { describe, expect, it } from "vitest";
import { opensInBrowser, staysOnOrigin } from "./guards.js";

describe("staysOnOrigin", () => {
  const origin = "http://127.0.0.1:5297";

  it.each([
    "http://127.0.0.1:5297/",
    "http://127.0.0.1:5297/items?view=all#top",
  ])("allows %s on Slate's origin", (url) => {
    const allowed = staysOnOrigin(url, origin);

    expect(allowed).toBe(true);
  });

  it.each([
    "https://example.com/",
    "http://127.0.0.1:5298/",
    "https://127.0.0.1:5297/",
    "http://localhost:5297/",
    "http://127.0.0.1:5297@example.com/",
    "file:///etc/passwd",
    "about:blank",
    "javascript:alert(1)",
    "not a url",
  ])("refuses %s", (url) => {
    const allowed = staysOnOrigin(url, origin);

    expect(allowed).toBe(false);
  });
});

describe("opensInBrowser", () => {
  it("opens https links in the user's browser", () => {
    const opens = opensInBrowser("https://example.com/docs");

    expect(opens).toBe(true);
  });

  it.each([
    "http://example.com/",
    "file:///Applications/Calculator.app",
    "smb://example.com/share",
    "javascript:alert(1)",
    "not a url",
  ])("refuses %s", (url) => {
    const opens = opensInBrowser(url);

    expect(opens).toBe(false);
  });
});
