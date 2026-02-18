# Claude Code Built-in Tools Reference

Claude Code が呼び出しうる全ビルトインツールの一覧と、その JSON Schema（入出力）をまとめたドキュメント。

## 情報ソース

- [Agent SDK overview (platform.claude.com)](https://platform.claude.com/docs/en/agent-sdk/overview) — ツール一覧と概要
- [Agent SDK reference - TypeScript (platform.claude.com)](https://platform.claude.com/docs/en/agent-sdk/typescript) — 全ツールの Input/Output 型定義（本ドキュメントの主要ソース）
- [Handle approvals and user input (platform.claude.com)](https://platform.claude.com/docs/en/agent-sdk/user-input) — AskUserQuestion の詳細な挙動とハンドリング方法
- [Custom Tools (platform.claude.com)](https://platform.claude.com/docs/en/agent-sdk/custom-tools) — MCP カスタムツールの定義方法
- [@anthropic-ai/claude-agent-sdk (npm)](https://www.npmjs.com/package/@anthropic-ai/claude-agent-sdk) — TypeScript SDK パッケージ（型定義のエクスポート元）

> **Note**: 本ドキュメントは 2026-02-18 時点の公式ドキュメントに基づく。Claude Code / Agent SDK のアップデートによりスキーマが変更される可能性がある。

---

## ツール一覧

| # | Tool Name | 概要 | カテゴリ |
|---|-----------|------|----------|
| 1 | [Bash](#1-bash) | シェルコマンド実行 | 実行系 |
| 2 | [BashOutput](#2-bashoutput) | バックグラウンドシェルの出力取得 | 実行系 |
| 3 | [KillBash](#3-killbash) | バックグラウンドシェルの停止 | 実行系 |
| 4 | [Read](#4-read) | ファイル読み取り | ファイル操作系 |
| 5 | [Write](#5-write) | ファイル書き込み | ファイル操作系 |
| 6 | [Edit](#6-edit) | ファイルの部分編集 | ファイル操作系 |
| 7 | [NotebookEdit](#7-notebookedit) | Jupyter Notebook セル編集 | ファイル操作系 |
| 8 | [Glob](#8-glob) | ファイルパターンマッチ検索 | 検索系 |
| 9 | [Grep](#9-grep) | ファイル内容の正規表現検索 | 検索系 |
| 10 | [WebSearch](#10-websearch) | Web 検索 | Web 系 |
| 11 | [WebFetch](#11-webfetch) | URL コンテンツ取得 | Web 系 |
| 12 | [Task](#12-task) | サブエージェント起動 | エージェント系 |
| 13 | [AskUserQuestion](#13-askuserquestion) | ユーザーへの質問（選択式） | 対話系 |
| 14 | [TodoWrite](#14-todowrite) | タスクリスト管理 | 対話系 |
| 15 | [ExitPlanMode](#15-exitplanmode) | プランモード終了 | 対話系 |
| 16 | [ListMcpResources](#16-listmcpresources) | MCP リソース一覧取得 | MCP 系 |
| 17 | [ReadMcpResource](#17-readmcpresource) | MCP リソース読み取り | MCP 系 |

> MCP サーバー経由のツールは `mcp__{server_name}__{tool_name}` という命名規則で追加される。上記はビルトインツールのみ。

---

## 全ツール Input/Output Schema

### 1. Bash

シェルコマンドを実行する。持続的なシェルセッション内で動作し、タイムアウトやバックグラウンド実行をサポート。

#### Input

```json
{
  "type": "object",
  "properties": {
    "command": {
      "type": "string",
      "description": "実行するコマンド"
    },
    "timeout": {
      "type": "number",
      "description": "タイムアウト（ミリ秒）。最大 600000（10分）",
      "maximum": 600000
    },
    "description": {
      "type": "string",
      "description": "コマンドの説明（5-10語）"
    },
    "run_in_background": {
      "type": "boolean",
      "description": "true でバックグラウンド実行"
    }
  },
  "required": ["command"]
}
```

#### Output

```json
{
  "type": "object",
  "properties": {
    "output": {
      "type": "string",
      "description": "stdout と stderr の結合出力"
    },
    "exitCode": {
      "type": "number",
      "description": "コマンドの終了コード"
    },
    "killed": {
      "type": "boolean",
      "description": "タイムアウトで kill されたかどうか"
    },
    "shellId": {
      "type": "string",
      "description": "バックグラウンドプロセスのシェル ID"
    }
  }
}
```

---

### 2. BashOutput

実行中または完了したバックグラウンドシェルの出力を取得する。

#### Input

```json
{
  "type": "object",
  "properties": {
    "bash_id": {
      "type": "string",
      "description": "出力を取得するバックグラウンドシェルの ID"
    },
    "filter": {
      "type": "string",
      "description": "出力行をフィルタする正規表現（任意）"
    }
  },
  "required": ["bash_id"]
}
```

#### Output

```json
{
  "type": "object",
  "properties": {
    "output": {
      "type": "string",
      "description": "前回確認以降の新しい出力"
    },
    "status": {
      "type": "string",
      "enum": ["running", "completed", "failed"],
      "description": "シェルの現在のステータス"
    },
    "exitCode": {
      "type": "number",
      "description": "終了コード（completed 時のみ）"
    }
  }
}
```

---

### 3. KillBash

実行中のバックグラウンドシェルを停止する。

#### Input

```json
{
  "type": "object",
  "properties": {
    "shell_id": {
      "type": "string",
      "description": "停止するバックグラウンドシェルの ID"
    }
  },
  "required": ["shell_id"]
}
```

#### Output

```json
{
  "type": "object",
  "properties": {
    "message": {
      "type": "string",
      "description": "成功メッセージ"
    },
    "shell_id": {
      "type": "string",
      "description": "停止したシェルの ID"
    }
  }
}
```

---

### 4. Read

ローカルファイルシステムからファイルを読み取る。テキスト、画像、PDF、Jupyter Notebook に対応。

#### Input

```json
{
  "type": "object",
  "properties": {
    "file_path": {
      "type": "string",
      "description": "読み取るファイルの絶対パス"
    },
    "offset": {
      "type": "number",
      "description": "読み取り開始行番号"
    },
    "limit": {
      "type": "number",
      "description": "読み取る行数"
    }
  },
  "required": ["file_path"]
}
```

#### Output

ファイルの種類に応じて異なる形式を返す。

**テキストファイルの場合:**

```json
{
  "type": "object",
  "properties": {
    "content": {
      "type": "string",
      "description": "行番号付きのファイル内容"
    },
    "total_lines": {
      "type": "number",
      "description": "ファイルの総行数"
    },
    "lines_returned": {
      "type": "number",
      "description": "実際に返された行数"
    }
  }
}
```

**画像ファイルの場合:**

```json
{
  "type": "object",
  "properties": {
    "image": {
      "type": "string",
      "description": "Base64 エンコードされた画像データ"
    },
    "mime_type": {
      "type": "string",
      "description": "画像の MIME タイプ"
    },
    "file_size": {
      "type": "number",
      "description": "ファイルサイズ（バイト）"
    }
  }
}
```

**PDF ファイルの場合:**

```json
{
  "type": "object",
  "properties": {
    "pages": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "page_number": { "type": "number" },
          "text": { "type": "string" },
          "images": {
            "type": "array",
            "items": {
              "type": "object",
              "properties": {
                "image": { "type": "string" },
                "mime_type": { "type": "string" }
              }
            }
          }
        }
      }
    },
    "total_pages": {
      "type": "number",
      "description": "総ページ数"
    }
  }
}
```

**Jupyter Notebook の場合:**

```json
{
  "type": "object",
  "properties": {
    "cells": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "cell_type": { "type": "string", "enum": ["code", "markdown"] },
          "source": { "type": "string" },
          "outputs": { "type": "array" },
          "execution_count": { "type": "number" }
        }
      }
    },
    "metadata": { "type": "object" }
  }
}
```

---

### 5. Write

ファイルを書き込む（既存ファイルは上書き）。

#### Input

```json
{
  "type": "object",
  "properties": {
    "file_path": {
      "type": "string",
      "description": "書き込むファイルの絶対パス"
    },
    "content": {
      "type": "string",
      "description": "書き込む内容"
    }
  },
  "required": ["file_path", "content"]
}
```

#### Output

```json
{
  "type": "object",
  "properties": {
    "message": {
      "type": "string",
      "description": "成功メッセージ"
    },
    "bytes_written": {
      "type": "number",
      "description": "書き込んだバイト数"
    },
    "file_path": {
      "type": "string",
      "description": "書き込んだファイルのパス"
    }
  }
}
```

---

### 6. Edit

ファイル内の文字列を正確に置換する。

#### Input

```json
{
  "type": "object",
  "properties": {
    "file_path": {
      "type": "string",
      "description": "編集するファイルの絶対パス"
    },
    "old_string": {
      "type": "string",
      "description": "置換対象の文字列"
    },
    "new_string": {
      "type": "string",
      "description": "置換後の文字列（old_string と異なる必要がある）"
    },
    "replace_all": {
      "type": "boolean",
      "default": false,
      "description": "true で全出現箇所を置換"
    }
  },
  "required": ["file_path", "old_string", "new_string"]
}
```

#### Output

```json
{
  "type": "object",
  "properties": {
    "message": {
      "type": "string",
      "description": "確認メッセージ"
    },
    "replacements": {
      "type": "number",
      "description": "置換回数"
    },
    "file_path": {
      "type": "string",
      "description": "編集したファイルのパス"
    }
  }
}
```

---

### 7. NotebookEdit

Jupyter Notebook のセルを編集する。

#### Input

```json
{
  "type": "object",
  "properties": {
    "notebook_path": {
      "type": "string",
      "description": "Jupyter Notebook ファイルの絶対パス"
    },
    "cell_id": {
      "type": "string",
      "description": "編集するセルの ID"
    },
    "new_source": {
      "type": "string",
      "description": "セルの新しいソース"
    },
    "cell_type": {
      "type": "string",
      "enum": ["code", "markdown"],
      "description": "セルの種類"
    },
    "edit_mode": {
      "type": "string",
      "enum": ["replace", "insert", "delete"],
      "description": "編集の種類"
    }
  },
  "required": ["notebook_path", "new_source"]
}
```

#### Output

```json
{
  "type": "object",
  "properties": {
    "message": {
      "type": "string",
      "description": "成功メッセージ"
    },
    "edit_type": {
      "type": "string",
      "enum": ["replaced", "inserted", "deleted"],
      "description": "実行された編集の種類"
    },
    "cell_id": {
      "type": "string",
      "description": "影響を受けたセル ID"
    },
    "total_cells": {
      "type": "number",
      "description": "編集後のノートブックの総セル数"
    }
  }
}
```

---

### 8. Glob

glob パターンでファイルを高速検索する。

#### Input

```json
{
  "type": "object",
  "properties": {
    "pattern": {
      "type": "string",
      "description": "glob パターン（例: \"**/*.ts\", \"src/**/*.py\"）"
    },
    "path": {
      "type": "string",
      "description": "検索ディレクトリ（デフォルト: cwd）"
    }
  },
  "required": ["pattern"]
}
```

#### Output

```json
{
  "type": "object",
  "properties": {
    "matches": {
      "type": "array",
      "items": { "type": "string" },
      "description": "マッチしたファイルパスの配列"
    },
    "count": {
      "type": "number",
      "description": "マッチ数"
    },
    "search_path": {
      "type": "string",
      "description": "検索に使用したディレクトリ"
    }
  }
}
```

---

### 9. Grep

ripgrep ベースの強力なファイル内容検索。

#### Input

```json
{
  "type": "object",
  "properties": {
    "pattern": {
      "type": "string",
      "description": "検索する正規表現パターン"
    },
    "path": {
      "type": "string",
      "description": "検索対象のファイルまたはディレクトリ（デフォルト: cwd）"
    },
    "glob": {
      "type": "string",
      "description": "ファイルフィルタ用 glob パターン（例: \"*.js\"）"
    },
    "type": {
      "type": "string",
      "description": "ファイル種別フィルタ（例: \"js\", \"py\", \"rust\"）"
    },
    "output_mode": {
      "type": "string",
      "enum": ["content", "files_with_matches", "count"],
      "description": "出力モード"
    },
    "-i": {
      "type": "boolean",
      "description": "大文字小文字を区別しない検索"
    },
    "-n": {
      "type": "boolean",
      "description": "行番号を表示（content モード時）"
    },
    "-B": {
      "type": "number",
      "description": "マッチ前の表示行数"
    },
    "-A": {
      "type": "number",
      "description": "マッチ後の表示行数"
    },
    "-C": {
      "type": "number",
      "description": "マッチ前後の表示行数"
    },
    "head_limit": {
      "type": "number",
      "description": "出力を先頭 N 行/エントリに制限"
    },
    "multiline": {
      "type": "boolean",
      "description": "マルチラインモードを有効化"
    }
  },
  "required": ["pattern"]
}
```

#### Output

**content モード:**

```json
{
  "type": "object",
  "properties": {
    "matches": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "file": { "type": "string" },
          "line_number": { "type": "number" },
          "line": { "type": "string" },
          "before_context": { "type": "array", "items": { "type": "string" } },
          "after_context": { "type": "array", "items": { "type": "string" } }
        }
      }
    },
    "total_matches": { "type": "number" }
  }
}
```

**files_with_matches モード:**

```json
{
  "type": "object",
  "properties": {
    "files": {
      "type": "array",
      "items": { "type": "string" }
    },
    "count": { "type": "number" }
  }
}
```

**count モード:**

```json
{
  "type": "object",
  "properties": {
    "counts": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "file": { "type": "string" },
          "count": { "type": "number" }
        }
      }
    },
    "total": { "type": "number" }
  }
}
```

---

### 10. WebSearch

Web を検索してフォーマットされた結果を返す。

#### Input

```json
{
  "type": "object",
  "properties": {
    "query": {
      "type": "string",
      "description": "検索クエリ"
    },
    "allowed_domains": {
      "type": "array",
      "items": { "type": "string" },
      "description": "結果をこれらのドメインのみに限定"
    },
    "blocked_domains": {
      "type": "array",
      "items": { "type": "string" },
      "description": "これらのドメインの結果を除外"
    }
  },
  "required": ["query"]
}
```

#### Output

```json
{
  "type": "object",
  "properties": {
    "results": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "title": { "type": "string" },
          "url": { "type": "string" },
          "snippet": { "type": "string" },
          "metadata": { "type": "object" }
        }
      }
    },
    "total_results": { "type": "number" },
    "query": { "type": "string" }
  }
}
```

---

### 11. WebFetch

URL からコンテンツを取得し、AI モデルで処理する。

#### Input

```json
{
  "type": "object",
  "properties": {
    "url": {
      "type": "string",
      "format": "uri",
      "description": "取得する URL"
    },
    "prompt": {
      "type": "string",
      "description": "取得したコンテンツに対して実行するプロンプト"
    }
  },
  "required": ["url", "prompt"]
}
```

#### Output

```json
{
  "type": "object",
  "properties": {
    "response": {
      "type": "string",
      "description": "AI モデルによる分析結果"
    },
    "url": {
      "type": "string",
      "description": "取得した URL"
    },
    "final_url": {
      "type": "string",
      "description": "リダイレクト後の最終 URL"
    },
    "status_code": {
      "type": "number",
      "description": "HTTP ステータスコード"
    }
  }
}
```

---

### 12. Task

サブエージェントを起動して、複雑なマルチステップタスクを自律的に処理する。

#### Input

```json
{
  "type": "object",
  "properties": {
    "description": {
      "type": "string",
      "description": "タスクの短い説明（3-5語）"
    },
    "prompt": {
      "type": "string",
      "description": "エージェントに実行させるタスク"
    },
    "subagent_type": {
      "type": "string",
      "description": "使用する特殊エージェントの種類"
    }
  },
  "required": ["description", "prompt", "subagent_type"]
}
```

#### Output

```json
{
  "type": "object",
  "properties": {
    "result": {
      "type": "string",
      "description": "サブエージェントからの最終結果メッセージ"
    },
    "usage": {
      "type": "object",
      "properties": {
        "input_tokens": { "type": "number" },
        "output_tokens": { "type": "number" },
        "cache_creation_input_tokens": { "type": "number" },
        "cache_read_input_tokens": { "type": "number" }
      }
    },
    "total_cost_usd": { "type": "number" },
    "duration_ms": { "type": "number" }
  }
}
```

---

### 13. AskUserQuestion

ユーザーに選択式の質問を投げる。1〜4つの質問を含められ、各質問には2〜4つの選択肢がある。ユーザーは常に "Other" を選んで自由記述も可能。

> **GUI 実装者への重要な注意**: このツールは `canUseTool` コールバック経由で処理される。Claude がこのツールを呼ぶと、`canUseTool(toolName="AskUserQuestion", input={questions: [...]})` としてコールバックが発火する。GUI 側で質問を表示し、回答を `answers` フィールドに詰めて `updatedInput` として返す必要がある。
>
> 詳細は [Handle approvals and user input](https://platform.claude.com/docs/en/agent-sdk/user-input) を参照。

#### Input

```json
{
  "type": "object",
  "properties": {
    "questions": {
      "type": "array",
      "minItems": 1,
      "maxItems": 4,
      "description": "ユーザーに聞く質問（1-4個）",
      "items": {
        "type": "object",
        "properties": {
          "question": {
            "type": "string",
            "description": "質問文（明確で、疑問符で終わる）"
          },
          "header": {
            "type": "string",
            "maxLength": 12,
            "description": "短いラベル（チップ/タグ表示用、最大12文字）。例: \"Auth method\", \"Library\""
          },
          "options": {
            "type": "array",
            "minItems": 2,
            "maxItems": 4,
            "description": "選択肢（2-4個）。\"Other\" は自動追加される",
            "items": {
              "type": "object",
              "properties": {
                "label": {
                  "type": "string",
                  "description": "選択肢の表示テキスト（1-5語）"
                },
                "description": {
                  "type": "string",
                  "description": "選択肢の説明（トレードオフや影響の補足）"
                }
              },
              "required": ["label", "description"]
            }
          },
          "multiSelect": {
            "type": "boolean",
            "default": false,
            "description": "true で複数選択を許可"
          }
        },
        "required": ["question", "header", "options", "multiSelect"]
      }
    },
    "answers": {
      "type": "object",
      "additionalProperties": { "type": "string" },
      "description": "ユーザーの回答。キー=質問文、値=選択したラベル。複数選択はカンマ区切り。パーミッションシステムにより投入される"
    }
  },
  "required": ["questions"]
}
```

#### Output

```json
{
  "type": "object",
  "properties": {
    "questions": {
      "type": "array",
      "description": "質問したリスト（入力と同じ構造）",
      "items": {
        "type": "object",
        "properties": {
          "question": { "type": "string" },
          "header": { "type": "string" },
          "options": {
            "type": "array",
            "items": {
              "type": "object",
              "properties": {
                "label": { "type": "string" },
                "description": { "type": "string" }
              }
            }
          },
          "multiSelect": { "type": "boolean" }
        }
      }
    },
    "answers": {
      "type": "object",
      "additionalProperties": { "type": "string" },
      "description": "ユーザーの回答。キー=質問文、値=選択ラベル。複数選択はカンマ区切り"
    }
  }
}
```

#### AskUserQuestion のハンドリングフロー

```
1. Claude が AskUserQuestion を tool_use として呼び出す
     ↓
2. canUseTool コールバックが発火（toolName="AskUserQuestion"）
     ↓
3. input.questions を GUI/CLI でユーザーに表示
     ↓
4. ユーザーが選択肢を選ぶ（または "Other" で自由記述）
     ↓
5. answers オブジェクトを構築:
   { "質問文": "選択したラベル", ... }
   （複数選択: "Label1, Label2"）
     ↓
6. { behavior: "allow", updatedInput: { questions: [...], answers: {...} } } を返す
     ↓
7. Claude が answers を受け取り、タスクを続行
```

#### 制限事項

- サブエージェント（Task ツール経由）では利用不可
- 各呼び出しで 1-4 質問、各質問に 2-4 選択肢

---

### 14. TodoWrite

構造化タスクリストの作成・管理。

#### Input

```json
{
  "type": "object",
  "properties": {
    "todos": {
      "type": "array",
      "description": "更新後のタスクリスト全体",
      "items": {
        "type": "object",
        "properties": {
          "content": {
            "type": "string",
            "description": "タスクの説明"
          },
          "status": {
            "type": "string",
            "enum": ["pending", "in_progress", "completed"],
            "description": "タスクのステータス"
          },
          "activeForm": {
            "type": "string",
            "description": "現在進行形のタスク説明（in_progress 時にスピナーで表示）"
          }
        },
        "required": ["content", "status", "activeForm"]
      }
    }
  },
  "required": ["todos"]
}
```

#### Output

```json
{
  "type": "object",
  "properties": {
    "message": {
      "type": "string",
      "description": "成功メッセージ"
    },
    "stats": {
      "type": "object",
      "properties": {
        "total": { "type": "number" },
        "pending": { "type": "number" },
        "in_progress": { "type": "number" },
        "completed": { "type": "number" }
      }
    }
  }
}
```

---

### 15. ExitPlanMode

プランニングモードを終了し、ユーザーにプランの承認を求める。

#### Input

```json
{
  "type": "object",
  "properties": {
    "plan": {
      "type": "string",
      "description": "ユーザーに承認を求めるプラン内容"
    }
  },
  "required": ["plan"]
}
```

#### Output

```json
{
  "type": "object",
  "properties": {
    "message": {
      "type": "string",
      "description": "確認メッセージ"
    },
    "approved": {
      "type": "boolean",
      "description": "ユーザーがプランを承認したかどうか"
    }
  }
}
```

---

### 16. ListMcpResources

接続された MCP サーバーから利用可能なリソース一覧を取得する。

#### Input

```json
{
  "type": "object",
  "properties": {
    "server": {
      "type": "string",
      "description": "リソースをフィルタするサーバー名（任意）"
    }
  },
  "required": []
}
```

#### Output

```json
{
  "type": "object",
  "properties": {
    "resources": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "uri": { "type": "string" },
          "name": { "type": "string" },
          "description": { "type": "string" },
          "mimeType": { "type": "string" },
          "server": { "type": "string" }
        }
      }
    },
    "total": { "type": "number" }
  }
}
```

---

### 17. ReadMcpResource

MCP サーバーから特定のリソースを読み取る。

#### Input

```json
{
  "type": "object",
  "properties": {
    "server": {
      "type": "string",
      "description": "MCP サーバー名"
    },
    "uri": {
      "type": "string",
      "description": "読み取るリソースの URI"
    }
  },
  "required": ["server", "uri"]
}
```

#### Output

```json
{
  "type": "object",
  "properties": {
    "contents": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "uri": { "type": "string" },
          "mimeType": { "type": "string" },
          "text": { "type": "string" },
          "blob": { "type": "string" }
        }
      }
    },
    "server": { "type": "string" }
  }
}
```

---

## GUI 実装者向け補足

### ツール許可（canUseTool）との関係

Claude Code の全ツール呼び出しは `canUseTool` コールバックを通過する。このコールバックは2つの役割を持つ:

1. **ツール実行許可**: Bash、Write、Edit 等の実行を許可/拒否する（Yes/No の判断）
2. **ユーザー入力収集**: AskUserQuestion の場合、質問を表示して回答を収集する

コールバックの応答は以下の2パターン:

| 応答 | TypeScript | 用途 |
|------|-----------|------|
| **Allow** | `{ behavior: "allow", updatedInput: ToolInput }` | ツール実行を許可（入力の変更も可能） |
| **Deny** | `{ behavior: "deny", message: string }` | ツール実行を拒否（理由メッセージ付き） |

### SDK バージョン要件

| 機能 | 最低 Claude Code バージョン |
|------|---------------------------|
| canUseTool コールバック | v1.0.82 |
| Agent SDK（agentSdk.query()） | v1.0.125 |
| サブエージェント新パス | v2.0.28 |
| スキル直接実行 | v2.1.0 |

### TypeScript 型定義の利用

npm パッケージ `@anthropic-ai/claude-agent-sdk` から全ツールの Input/Output 型が export されている:

```typescript
import type {
  AskUserQuestionInput,
  BashInput,
  BashOutputInput,
  FileEditInput,
  FileReadInput,
  FileWriteInput,
  GlobInput,
  GrepInput,
  KillShellInput,
  NotebookEditInput,
  WebFetchInput,
  WebSearchInput,
  TodoWriteInput,
  ExitPlanModeInput,
  ListMcpResourcesInput,
  ReadMcpResourceInput,
  AgentInput,       // Task tool
  ToolInput,        // Union of all above
} from "@anthropic-ai/claude-agent-sdk";
```
