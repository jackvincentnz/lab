import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// https://vitejs.dev/config/
export default defineConfig({
  /*
    The vite devserver needs to make requests to "base: /journal/" when behind the
    proxy since the $http_referer can't be used to identify the right app to proxy to.
    Keep aligned with:
    - infra/local/proxy/nginx.conf: "proxy_pass"
    - //apps/journal_app:tar: "package_dir"
  */
  base: "/journal/",
  plugins: [react()],
  server: {
    port: 3004,
  },
});
