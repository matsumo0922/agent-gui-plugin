# Agent GUI

[![Build](https://github.com/matsumo0922/agent-gui-plugin/workflows/Build/badge.svg)](https://github.com/matsumo0922/agent-gui-plugin/actions/workflows/build.yml)
[![Version](https://img.shields.io/jetbrains/plugin/v/me.matsumo.agent-gui-plugin.svg)](https://plugins.jetbrains.com/plugin/XXXXX-agent-gui)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/me.matsumo.agent-gui-plugin.svg)](https://plugins.jetbrains.com/plugin/XXXXX-agent-gui)

A unified GUI for AI coding agents inside your JetBrains IDE. Currently supports [Claude Code](https://docs.anthropic.com/en/docs/claude-code), with support for [OpenAI Codex](https://github.com/openai/codex) and other agents planned.

No terminal switching — just open the panel and start chatting.

## Supported Agents

| Agent | Status |
|---|---|
| Claude Code | Fully supported |
| OpenAI Codex | Planned |
| More agents | Coming soon |

## Features

- **Interactive Chat UI** — Multi-turn conversations rendered with full Markdown support including code blocks, tables, and GFM alerts.
- **Streaming Responses** — See responses as they arrive in real time, with thinking block visualization.
- **Tool Use Visualization** — Watch agents read files, edit code, run commands, and more — each tool call is displayed with rich detail.
- **Permission Management** — Approve or deny tool-use requests directly from the UI with a single click.
- **Sub-agent Tasks** — Track background agent tasks with dedicated task cards.
- **File Attachments** — Attach project files to your messages for context-aware assistance.
- **Session History** — Resume previous conversations across IDE restarts.
- **Native Look & Feel** — Built with Compose for IDE (Jewel), the UI adapts seamlessly to your IDE theme (light/dark).

## Requirements

- **JetBrains IDE** 2025.3 or later (IntelliJ IDEA, Android Studio, WebStorm, etc.)

### Claude Code

- **[Claude Code CLI](https://docs.anthropic.com/en/docs/claude-code)** installed and available on your PATH
- A valid **Anthropic API key** or **Claude Max subscription** configured in Claude Code

## Installation

### From JetBrains Marketplace

1. Open your IDE and go to **Settings** → **Plugins** → **Marketplace**
2. Search for **"Agent GUI"**
3. Click **Install** and restart the IDE

### Manual Installation

1. Download the latest release from [GitHub Releases](https://github.com/matsumo0922/agent-gui-plugin/releases)
2. Go to **Settings** → **Plugins** → **Install Plugin from Disk...**
3. Select the downloaded `.zip` file

## Getting Started

1. Open the **Agent GUI** tool window from the right sidebar
2. Start chatting!

## Architecture

```
Plugin (Compose UI + ViewModel)
    ↓
claude-agent-sdk-kotlin (Pure JVM SDK)
    ↓
Claude Code CLI (subprocess: stdin/stdout stream-json)
```

The plugin communicates with AI coding agents through dedicated SDK layers. Currently, `claude-agent-sdk-kotlin` handles Claude Code CLI communication via subprocess and stream-JSON transport.

## Building from Source

```bash
# Build the plugin
./gradlew :plugin:buildPlugin

# Run in a development IDE
./gradlew :plugin:runIde

# Run tests
./gradlew :plugin:check
```

## License

<!-- TODO: Add license -->

## Links

- [Claude Code Documentation](https://docs.anthropic.com/en/docs/claude-code)
- [JetBrains Marketplace](https://plugins.jetbrains.com)
- [Issue Tracker](https://github.com/matsumo0922/agent-gui-plugin/issues)
