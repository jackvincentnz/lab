/** Whether a navigation to `url` keeps the window on the origin that served Slate's page. */
export function staysOnOrigin(url: string, origin: string): boolean {
  return URL.parse(url)?.origin === origin;
}

/** Whether a page's request to open `url` in a new window goes to the user's browser instead. */
export function opensInBrowser(url: string): boolean {
  return URL.parse(url)?.protocol === "https:";
}
