import assert from "assert";
import fs from "fs";

// Make sure there's a file like build/static/js/main.12345678.js
const files = fs.readdirSync("apps/cra/build/static/js");
console.log(files);
assert.ok(files.some((f) => /main\.[0-9a-f]{8}\.js/.test(f)));
