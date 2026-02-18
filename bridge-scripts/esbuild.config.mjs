import { build } from "esbuild";

await build({
  entryPoints: ["main.mjs"],
  bundle: true,
  platform: "node",
  format: "esm",
  outfile: "../plugin/src/main/resources/bridge/main.mjs",
  external: [],
  banner: {
    js: "// Auto-generated - do not edit\nimport { createRequire } from 'module';\nconst require = createRequire(import.meta.url);",
  },
});

console.log("Bridge script bundled successfully.");
