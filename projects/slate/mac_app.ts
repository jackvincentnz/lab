import { execFileSync } from "node:child_process";
import { mkdirSync, mkdtempSync, readFileSync, rmSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { type OfficialArch, packager } from "@electron/packager";

const [appDir, electronDir, icon, output] = process.argv.slice(2);

const { version } = JSON.parse(
  readFileSync(join(electronDir, "package.json"), "utf8"),
) as { version: string };
// Macs are arm64 or x64, both of which Electron ships.
const arch = process.arch as OfficialArch;
const work = mkdtempSync(join(tmpdir(), "slate-mac-app-"));

// Bazel outputs are read-only, and packager keeps the modes of what it copies, so it gets writable
// copies to edit.
function writableCopy(source: string, name: string): string {
  const copy = join(work, name);
  execFileSync("ditto", [source, copy]);
  execFileSync("chmod", ["-R", "u+w", copy]);
  return copy;
}

try {
  // Workaround: packager takes Electron only as a release zip, which it would download, so this zips
  // the release the electron package already holds.
  const electron = writableCopy(join(electronDir, "dist"), "electron");
  const zips = join(work, "zips");
  mkdirSync(zips);
  execFileSync("ditto", [
    "-c",
    "-k",
    // Extended attributes would become `._` files, which break the signature.
    "--norsrc",
    "--noextattr",
    electron,
    join(zips, `electron-v${version}-darwin-${arch}.zip`),
  ]);

  const [packaged] = await packager({
    dir: writableCopy(appDir, "app"),
    out: join(work, "out"),
    tmpdir: join(work, "tmp"),
    name: "Slate",
    appBundleId: "lab.slate",
    icon,
    electronVersion: version,
    electronZipDir: zips,
    platform: "darwin",
    arch,
    asar: true,
    // Apple silicon runs only signed code. The hardened runtime would block ad hoc signed frameworks.
    osxSign: {
      identity: "-",
      identityValidation: false,
      optionsForFile: () => ({ hardenedRuntime: false }),
    },
    quiet: true,
  });

  execFileSync("ditto", [join(packaged, "Slate.app"), output]);
} finally {
  rmSync(work, { recursive: true, force: true });
}
