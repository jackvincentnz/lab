import { resolve } from "path";
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react-swc";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  build: {
    lib: {
      // Could also be a dictionary or array of multiple entry points
      entry: resolve(__dirname, "src/index.ts"),
      name: "bubbles",
      formats: ["es"], // adding 'umd' requires globals set to every external module
      fileName: (format) => `index.${format}.js`,
    },
    rollupOptions: {
      // make sure to externalize deps that shouldn't be bundled
      // into your library
      external: ["@mantine/core", "react", "@tabler/icons-react"],
      output: {
        // disable warning on src/index.ts using both default and named export
        exports: "named",
      },
    },
  },
});
