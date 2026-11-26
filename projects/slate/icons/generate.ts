import { readFileSync, writeFileSync } from "node:fs";
import { join } from "node:path";
import { chromium, type Page } from "@playwright/test";

// `bazel run` starts in the runfiles, so the checked-in files are found through the workspace.
const folder = join(
  process.env["BUILD_WORKSPACE_DIRECTORY"] ?? ".",
  "projects/slate/icons",
);
const logo = readFileSync(join(folder, "slate.svg"), "utf8");

// The letters are paths, so that the band does not depend on the fonts a machine has.
const devBand =
  '<clipPath id="body"><rect x="100" y="100" width="824" height="824" rx="185"/></clipPath>' +
  '<g clip-path="url(#body)"><rect x="100" y="770" width="824" height="160" fill="#D9468F"/>' +
  '<path d="M375 808 V892 H403 A42 42 0 0 0 403 808 Z M547 808 H487 V892 H547 M487 850 H535 M579 808 L614 892 L649 808" ' +
  'fill="none" stroke="#FFFFFF" stroke-width="20" stroke-linejoin="round"/></g>';
const devLogo = logo.replace("<!--dev-->", devBand);

// The web UI's header shows the logo without the margin macOS keeps around an app icon.
writeFileSync(
  join(folder, "slate-mark.svg"),
  logo
    .replace('viewBox="0 0 1024 1024"', 'viewBox="100 100 824 824"')
    .replace(/\n\s*<!--dev-->/, ""),
);

const browser = await chromium.launch();
try {
  const page = await browser.newPage();
  const render = (svg: string, size: number) => renderPng(page, svg, size);

  writeFileSync(join(folder, "Slate.icns"), await icns(logo, render));
  // A run from Bazel is not an app bundle, so it gets its Dock icon from these.
  writeFileSync(join(folder, "slate.png"), await render(logo, 512));
  writeFileSync(join(folder, "slate-dev.png"), await render(devLogo, 512));
  // Sent to MCP clients, which show it at about 16 to 32 px.
  writeFileSync(join(folder, "slate-64.png"), await render(logo, 64));
} finally {
  await browser.close();
}

async function renderPng(page: Page, svg: string, size: number) {
  await page.setViewportSize({ width: size, height: size });
  const source = `data:image/svg+xml;base64,${Buffer.from(svg).toString("base64")}`;
  await page.setContent(
    `<body style="margin:0"><img src="${source}" width="${size}" height="${size}" style="display:block"></body>`,
  );
  return page.screenshot({ omitBackground: true });
}

async function icns(
  svg: string,
  render: (svg: string, size: number) => Promise<Buffer>,
) {
  const types = {
    icp4: 16,
    icp5: 32,
    ic11: 32,
    ic07: 128,
    ic12: 64,
    ic08: 256,
    ic13: 256,
    ic09: 512,
    ic14: 512,
    ic10: 1024,
  };
  const entries = [];
  for (const [type, size] of Object.entries(types)) {
    const png = await render(svg, size);
    const header = Buffer.alloc(8);
    header.write(type, 0, "ascii");
    header.writeUInt32BE(8 + png.length, 4);
    entries.push(header, png);
  }
  const body = Buffer.concat(entries);
  const header = Buffer.alloc(8);
  header.write("icns", 0, "ascii");
  header.writeUInt32BE(8 + body.length, 4);
  return Buffer.concat([header, body]);
}
