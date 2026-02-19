import {build} from "esbuild";
import {existsSync} from "fs";
import {resolve} from "path";

// Kotlin/JS IR compiler output path
const kotlinJsDir = resolve("../bridge/build/compileSync/js/main/productionExecutable/kotlin");
const kotlinJsEntry = resolve(kotlinJsDir, "agent-gui-plugin-bridge.js");

if (!existsSync(kotlinJsEntry)) {
  console.error(`Kotlin/JS output not found at: ${kotlinJsEntry}`);
  console.error("Run './gradlew :bridge:jsProductionExecutableCompileSync' first.");
  process.exit(1);
}

await build({
  entryPoints: [kotlinJsEntry],
  bundle: true,
  platform: "node",
  format: "esm",
  outfile: "../plugin/src/main/resources/bridge/main.mjs",
  external: [],
  // Resolve Kotlin runtime from co-located dir, npm packages from bridge-scripts
  nodePaths: [kotlinJsDir, resolve("node_modules")],
  banner: {
    js: "// Auto-generated - do not edit\nimport { createRequire } from 'module';\nconst require = createRequire(import.meta.url);",
  },
});

console.log("Bridge script bundled successfully.");
