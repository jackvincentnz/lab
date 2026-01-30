import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// https://vitejs.dev/config/
export default defineConfig({
  /*
    The vite devserver needs to make requests to "base: /journal/" when behind the
    proxy since the $http_referer can't be used to identify the right app to proxy to.
    Keep aligned with:
    - infra/local/proxy/nginx.conf: "proxy_pass"
    - //projects/organizer/journal_app:tar: "package_dir"
  */
  base: "/journal/",
  plugins: [watchNodeModules(["@lab/bubbles"]), react()],
  server: {
    port: 3004,
    proxy: {
      "/graphql": "http://localhost:4000",
    },
  },
});

// https://github.com/vitejs/vite/issues/8619
import type { PluginOption } from "vite";

export function watchNodeModules(modules: string[]): PluginOption {
  return {
    name: "watch-node-modules",
    config() {
      return {
        server: {
          watch: {
            ignored: modules.map((m) => `!**/node_modules/${m}/**`),
          },
        },
        optimizeDeps: {
          exclude: modules,
          include: ["@mantine/core", "@lab/bubbles", "@apollo/client"],
        },
      };
    },
  };
}
