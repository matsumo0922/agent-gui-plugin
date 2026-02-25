# Split Tab Architecture — リファクタリング計画書

## 1. 概要

現在の `TabManager` は `toolWindow.contentManager` に対して Jewel の `addComposeTab()` で直接タブを追加するフラット構造。
これを、Android Studio の「Running Devices」ツールウィンドウのように**ドラッグ＆ドロップやコンテキストメニューで上下左右に分割できる UI**へ移行する。

### Before (現状)

```
ToolWindow
└── ContentManager
    ├── Content[0] (addComposeTab) → ComposePanel → ChatPanel
    ├── Content[1] (addComposeTab) → ComposePanel → ChatPanel
    └── Content[N] (addComposeTab) → ComposePanel → ChatPanel
```

### After (目標)

```
ToolWindow
└── ContentManager
    └── Content[0] (単一ルート: SplitRootPanel)
        └── SplitNode (再帰的ツリー)
            ├── LeafNode → JBTabs
            │   ├── TabInfo[0] → ComposePanel → ChatPanel
            │   └── TabInfo[1] → ComposePanel → ChatPanel
            └── SplitNode (JBSplitter: Horizontal)
                ├── LeafNode → JBTabs
                │   └── TabInfo[2] → ComposePanel → ChatPanel
                └── LeafNode → JBTabs
                    └── TabInfo[3] → ComposePanel → ChatPanel
```

---

## 2. 新設するクラスと役割

### 2.1 コンポーネントツリー (`toolwindow/split/`)

#### `SplitNode` — 再帰的分割ツリーのノード (sealed interface)

```kotlin
// toolwindow/split/SplitNode.kt

sealed interface SplitNode {
    val component: JComponent  // ツリーをたどって最終的に表示する Swing コンポーネント

    /** 末端ノード: JBTabs を保持し、複数の ChatTab をタブとして管理 */
    class Leaf(
        val tabs: JBTabs,
        val disposable: Disposable,
    ) : SplitNode {
        override val component: JComponent get() = tabs.component
    }

    /** 分割ノード: JBSplitter を保持し、2つの子ノードを管理 */
    class Split(
        val splitter: JBSplitter,
        var first: SplitNode,
        var second: SplitNode,
        val orientation: SplitOrientation,
    ) : SplitNode {
        override val component: JComponent get() = splitter
    }
}

enum class SplitOrientation { HORIZONTAL, VERTICAL }
```

#### `SplitRootPanel` — ToolWindow に登録する単一ルート JPanel

```kotlin
// toolwindow/split/SplitRootPanel.kt

/**
 * ToolWindow の ContentManager に登録する唯一の JPanel。
 * 内部に SplitNode ツリーのルートを保持し、ツリーの変更時に
 * 自身の中身を入れ替える。
 */
class SplitRootPanel(
    private val disposable: Disposable,
) : JPanel(BorderLayout()) {
    var rootNode: SplitNode = ...
        set(value) {
            field = value
            rebuildUI()
        }

    private fun rebuildUI() {
        removeAll()
        add(rootNode.component, BorderLayout.CENTER)
        revalidate()
        repaint()
    }
}
```

#### `SplitTreeManager` — ツリー操作の中心ロジック

```kotlin
// toolwindow/split/SplitTreeManager.kt

/**
 * SplitNode ツリーに対する構造操作を担当。
 * - splitLeaf(): Leaf を Split に変換
 * - mergeBack(): タブが空になった Leaf を除去して親 Split を Leaf に戻す
 * - findLeafContaining(tabInfo): TabInfo が属する Leaf を検索
 * - moveTab(tabInfo, fromLeaf, toLeaf): タブの移動
 */
class SplitTreeManager(
    private val rootPanel: SplitRootPanel,
    private val tabFactory: ChatTabFactory,
    private val parentDisposable: Disposable,
) {
    fun splitLeaf(
        leaf: SplitNode.Leaf,
        orientation: SplitOrientation,
        tabInfo: TabInfo?,         // null なら空の新しいタブを作成
        placeSecond: Boolean = true, // true = 新ペインを右/下に配置
    ): SplitNode.Split { ... }

    fun mergeBack(split: SplitNode.Split, surviving: SplitNode): SplitNode { ... }

    fun allLeaves(): List<SplitNode.Leaf> { ... }

    fun findLeafContaining(tabInfo: TabInfo): SplitNode.Leaf? { ... }
}
```

### 2.2 タブファクトリとライフサイクル (`service/`)

#### `ChatTabFactory` — タブ生成の責務を分離

```kotlin
// service/ChatTabFactory.kt

/**
 * JBTabs 用の TabInfo + ComposePanel + ChatViewModel のセットを生成する。
 * ViewModel のライフサイクルはこのクラスが管理する。
 */
class ChatTabFactory(
    private val project: Project,
    private val settingsService: SettingsService,
    private val scope: CoroutineScope,
) {
    // TabInfo → ViewModel/Job の管理マップ (旧 TabManager の責務を継承)
    private val viewModels = ConcurrentHashMap<TabInfo, ChatViewModel>()
    private val titleJobs = ConcurrentHashMap<TabInfo, Job>()

    /** 新しい ChatTab を生成して返す。JBTabs への追加は呼び出し側が行う */
    fun createTab(
        title: String = "New chat",
        resumeInfo: ResumeInfo? = null,
    ): TabInfo { ... }

    /** タブを破棄 (ViewModel dispose, ComposePanel dispose) */
    fun disposeTab(tabInfo: TabInfo) { ... }

    fun getViewModel(tabInfo: TabInfo): ChatViewModel? = viewModels[tabInfo]

    fun disposeAll() { ... }
}

data class ResumeInfo(
    val sessionId: String,
    val summary: SessionHistoryService.SessionSummary,
    val messages: List<ChatMessage>,
)
```

#### `TabManager` (リファクタリング後)

```kotlin
// service/TabManager.kt — 責務を大幅にスリム化

/**
 * ToolWindow と SplitTreeManager の橋渡し + 高レベル操作を提供。
 * 旧 TabManager の「Content ↔ ViewModel」管理は ChatTabFactory に移譲。
 */
class TabManager(
    private val toolWindow: ToolWindow,
    private val project: Project,
    private val settingsService: SettingsService,
    private val scope: CoroutineScope,
) {
    lateinit var splitTreeManager: SplitTreeManager
    lateinit var tabFactory: ChatTabFactory

    fun initialize() {
        tabFactory = ChatTabFactory(project, settingsService, scope)
        val rootPanel = SplitRootPanel(...)
        splitTreeManager = SplitTreeManager(rootPanel, tabFactory, ...)

        // 単一の Content として登録
        val content = ContentFactory.getInstance().createContent(rootPanel, "", false).apply {
            isCloseable = false
        }
        toolWindow.contentManager.addContent(content)
    }

    // --- 公開 API (既存インターフェースを維持) ---
    fun addTab(title: String = "New chat") { ... }
    fun resumeSession(summary: ..., messages: ...) { ... }
    fun dispose() { ... }

    // --- 分割操作 ---
    fun splitCurrentTab(orientation: SplitOrientation) { ... }
}
```

### 2.3 アクション (`toolwindow/action/`)

#### `SplitRightAction` / `SplitDownAction`

```kotlin
// toolwindow/action/SplitActions.kt

class SplitRightAction : DumbAwareAction("Split Right", ..., AllIcons.Actions.SplitVertically) {
    override fun actionPerformed(e: AnActionEvent) {
        val tabManager = ...
        tabManager.splitCurrentTab(SplitOrientation.HORIZONTAL)
    }
}

class SplitDownAction : DumbAwareAction("Split Down", ..., AllIcons.Actions.SplitHorizontally) {
    override fun actionPerformed(e: AnActionEvent) {
        val tabManager = ...
        tabManager.splitCurrentTab(SplitOrientation.VERTICAL)
    }
}
```

#### `TabContextMenuProvider` — JBTabs のコンテキストメニュー

```kotlin
// toolwindow/action/TabContextMenuProvider.kt

/**
 * JBTabs に右クリックメニューを登録する。
 * 「Split Right」「Split Down」「Close」「Close Others」等のアクションを提供。
 */
object TabContextMenuProvider {
    fun install(tabs: JBTabs, tabManager: TabManager) { ... }
}
```

### 2.4 ドラッグ＆ドロップ (`toolwindow/dnd/`)

#### `TabDragHandler` — タブの D&D 処理

```kotlin
// toolwindow/dnd/TabDragHandler.kt

/**
 * JBTabs 間でのタブ移動を D&D で実現する。
 * ドロップ先のオーバーレイ表示 (左/右/上/下/中央) と
 * 分割 or 同一 JBTabs への挿入を制御する。
 */
class TabDragHandler(
    private val splitTreeManager: SplitTreeManager,
    private val tabFactory: ChatTabFactory,
) {
    fun installOn(leaf: SplitNode.Leaf) { ... }
}
```

---

## 3. Compose (Jewel) 統合パターン

### 3.1 ComposePanel のラップ方法

現在 `addComposeTab()` 内部で暗黙的に行われている ComposePanel の生成を、明示的に行う。

```kotlin
fun createComposePanel(
    viewModel: ChatViewModel,
    project: Project,
    parentDisposable: Disposable,
): ComposePanel {
    return ComposePanel().apply {
        // Jewel の Compose テーマを適用
        setContent {
            // SwingComposeTheme で Jewel テーマをブリッジ
            SwingComposeTheme {
                LaunchedEffect(viewModel) {
                    if (viewModel.uiState.value.sessionState == SessionState.Disconnected) {
                        viewModel.start()
                    }
                }
                ChatPanel(viewModel = viewModel, project = project)
            }
        }

        // Disposable チェーンに ComposePanel を登録して確実に破棄
        Disposer.register(parentDisposable, Disposable { this.removeNotify() })
    }
}
```

> **注意**: `addComposeTab()` は内部で `SwingComposeTheme` の適用と `Disposer` 登録を行っている。
> 直接 `ComposePanel` を使う場合はこれらを自前で行う必要がある。
> Jewel ソースの `addComposeTab()` 実装を参照し、テーマブリッジを正確に再現すること。

### 3.2 TabInfo へのマウント

```kotlin
fun ChatTabFactory.createTab(title: String, resumeInfo: ResumeInfo?): TabInfo {
    val vm = createViewModel(resumeInfo)
    val tabDisposable = Disposer.newDisposable("ChatTab-${UUID.randomUUID()}")
    val composePanel = createComposePanel(vm, project, tabDisposable)

    val tabInfo = TabInfo(composePanel).apply {
        setText(title)
        setTabLabelActions(ActionManager.getInstance().getAction("AgentGUI.TabActions"), "AgentGUI.Tab")
    }

    viewModels[tabInfo] = vm
    observeTitle(tabInfo, vm)

    return tabInfo
}
```

---

## 4. 段階的移行ステップ

### Phase 1: 基盤クラスの新設 (非破壊)

**ゴール**: 新しいクラス群を追加し、既存コードに一切影響を与えずにコンパイルが通る状態にする。

| Step | 作業内容 | 新設/変更ファイル |
|------|----------|------------------|
| 1-1 | `SplitNode` sealed interface を作成 | `toolwindow/split/SplitNode.kt` |
| 1-2 | `SplitRootPanel` を作成 | `toolwindow/split/SplitRootPanel.kt` |
| 1-3 | `SplitTreeManager` のスケルトンを作成 | `toolwindow/split/SplitTreeManager.kt` |
| 1-4 | `ChatTabFactory` を作成 (ViewModel 生成/破棄ロジックを TabManager からコピー) | `service/ChatTabFactory.kt` |
| 1-5 | ComposePanel ラッパー関数を作成 | `ui/ComposePanelFactory.kt` |

**検証**: `./gradlew :plugin:build` が通ること。既存の動作に影響なし。

### Phase 2: SplitTreeManager のコアロジック実装

**ゴール**: ツリー操作のロジックを完成させ、ユニットテスト可能にする。

| Step | 作業内容 |
|------|----------|
| 2-1 | `SplitTreeManager.splitLeaf()` — Leaf を Split に変換するロジック |
| 2-2 | `SplitTreeManager.mergeBack()` — 空 Leaf の除去とツリーの縮退 |
| 2-3 | `SplitTreeManager.findLeafContaining()` — タブ検索 |
| 2-4 | `SplitTreeManager.allLeaves()` — 全 Leaf の列挙 |
| 2-5 | JBTabs の `TabsListener` で最後のタブが閉じられた時の `mergeBack` 呼び出し |

**検証**: ツリー操作の単体テスト (JBTabs/JBSplitter のモック or headless テスト)。

### Phase 3: TabManager の切り替え

**ゴール**: `TabManager` のタブ生成パスを `addComposeTab()` から新アーキテクチャへ切り替える。

| Step | 作業内容 |
|------|----------|
| 3-1 | `TabManager.initialize()` で `SplitRootPanel` を単一 Content として登録 |
| 3-2 | `TabManager.addTab()` を `ChatTabFactory.createTab()` + `JBTabs.addTab()` に置換 |
| 3-3 | `TabManager.resumeSession()` を同様に置換 |
| 3-4 | `ContentManagerListener` を削除し、`TabsListener` での ViewModel 破棄に移行 |
| 3-5 | 旧 `viewModels: ConcurrentHashMap<Content, ChatViewModel>` を削除 |

**検証**: `./gradlew :plugin:runIde` で単一ペインでのタブ追加/削除/セッション再開が動作すること。

### Phase 4: 分割アクションの実装

**ゴール**: コンテキストメニューとタイトルバーから分割操作が可能になる。

| Step | 作業内容 |
|------|----------|
| 4-1 | `SplitRightAction` / `SplitDownAction` を実装 |
| 4-2 | `TabContextMenuProvider` を実装し、JBTabs の右クリックメニューに Split/Close 等を追加 |
| 4-3 | `AgentToolWindowFactory` のタイトルバーアクションに Split ボタンを追加 |
| 4-4 | 分割時のフォーカス管理 (新ペインにフォーカス移動) |

**検証**: Split Right/Down で画面が分割され、各ペインで独立にチャットが動作すること。

### Phase 5: ドラッグ＆ドロップ

**ゴール**: タブを別のペインへ D&D で移動できるようにする。

| Step | 作業内容 |
|------|----------|
| 5-1 | `TabDragHandler` の実装 — DragSource/DropTarget の設定 |
| 5-2 | ドロップ位置のオーバーレイ表示 (左/右/上/下/中央の5分割インジケーター) |
| 5-3 | ドロップ結果に応じた `splitLeaf()` or `moveTab()` の呼び出し |
| 5-4 | ドラッグ元 Leaf が空になった場合の `mergeBack()` 呼び出し |

**検証**: タブを D&D で別ペインに移動、新ペインへの分割移動が動作すること。

### Phase 6: クリーンアップ

| Step | 作業内容 |
|------|----------|
| 6-1 | 不要になった `ChatTab` モデルの削除 |
| 6-2 | `SessionHistoryAction` の `tabManager.resumeSession()` 呼び出しを確認・調整 |
| 6-3 | `addComposeTab()` の import を完全に除去 |

---

## 5. ファイル構成 (移行後)

```
plugin/src/main/kotlin/me/matsumo/agentguiplugin/
├── model/
│   ├── AttachedFile.kt              (変更なし)
│   └── SessionHistoryJson.kt        (変更なし)
├── service/
│   ├── ChatTabFactory.kt            ★ 新設: TabInfo + ComposePanel + ViewModel 生成
│   ├── SessionHistoryService.kt     (変更なし)
│   ├── SessionService.kt            (微修正: TabManager.initialize() の呼び出し)
│   ├── SettingsService.kt           (変更なし)
│   └── TabManager.kt               ★ リファクタリング: 高レベル操作のみ
├── toolwindow/
│   ├── AgentToolWindowFactory.kt    (微修正: Split アクションの追加)
│   ├── SessionHistoryAction.kt      (変更なし or 微修正)
│   ├── action/
│   │   └── SplitActions.kt          ★ 新設: Split Right / Split Down アクション
│   ├── dnd/
│   │   └── TabDragHandler.kt        ★ 新設: D&D ハンドラー
│   └── split/
│       ├── SplitNode.kt             ★ 新設: ツリーノード定義
│       ├── SplitRootPanel.kt        ★ 新設: ルートパネル
│       └── SplitTreeManager.kt      ★ 新設: ツリー操作ロジック
├── ui/
│   ├── ChatPanel.kt                 (変更なし)
│   ├── ComposePanelFactory.kt       ★ 新設: ComposePanel 生成ユーティリティ
│   ├── chat/
│   │   └── ...                      (変更なし)
│   ├── component/
│   │   └── ...                      (変更なし)
│   └── theme/
│       └── ChatTheme.kt             (変更なし)
└── viewmodel/
    └── ...                          (変更なし)
```

---

## 6. 重要な設計判断と注意事項

### 6.1 JBTabs の生成方法

```kotlin
// JBTabsFactory を使ってエディタスタイルのタブを生成
val tabs = JBTabsFactory.createEditorTabs(
    project,
    parentDisposable,
)
```

- `createEditorTabs` はエディタと同じ見た目・操作感のタブバーを提供する
- タブの並び替え (reorder) は `JBTabs` が標準でサポートしている
- **ただし cross-JBTabs の D&D は標準サポート外** → `TabDragHandler` で自前実装が必要

### 6.2 ComposePanel の Dispose チェーン

```
Disposer 階層:
  ToolWindow (IDE が管理)
    └── SplitRootPanel の parentDisposable
        ├── SplitNode.Leaf の disposable
        │   └── JBTabs の Disposable
        └── ChatTabFactory 内の各 tabDisposable
            ├── ComposePanel
            └── ChatViewModel
```

- `Disposer.register(parent, child)` で階層を構築
- Leaf が除去される時は `Disposer.dispose(leaf.disposable)` で配下すべて破棄
- **ComposePanel は `removeNotify()` 時に内部の Compose ランタイムを破棄する** ため、明示的な dispose 呼び出しが必要

### 6.3 addComposeTab の内部実装との差異

`org.jetbrains.jewel.bridge.addComposeTab()` の内部実装では:

1. `ComposePanel` を生成
2. `SwingComposeTheme { content() }` でテーマをラップ
3. `ContentFactory.getInstance().createContent()` で Content 化
4. `Disposer.register(content, ...)` で ComposePanel を登録

直接 `ComposePanel` を使う場合、**`SwingComposeTheme` によるテーマブリッジを自前で適用する必要がある**。
これを忘れると IntelliJ のダークテーマ/ライトテーマの切り替えが Compose UI に反映されない。

```kotlin
// Jewel の addComposeTab 内部相当のコード
ComposePanel().apply {
    setContent {
        SwingComposeTheme {  // ← これが必須
            content()
        }
    }
}
```

### 6.4 スレッドセーフティ

- `SplitTreeManager` のツリー操作は **EDT (Event Dispatch Thread) 上で行う**
- `ChatViewModel` の生成・破棄は EDT で行い、内部の coroutine は vmScope で管理
- JBTabs/JBSplitter の操作はすべて Swing コンポーネントなので EDT 必須

### 6.5 最後のタブが閉じられた時の振る舞い

現在の `TabManager` と同じく、Leaf 内の最後のタブが閉じられた場合:
- その Leaf が唯一の Leaf (ルート) なら → 空のタブを自動生成
- その Leaf が分割ペインの一部なら → `mergeBack()` で分割を解消し、もう一方の子をルートに昇格

---

## 7. リスクと対策

| リスク | 影響度 | 対策 |
|--------|--------|------|
| `ComposePanel` の Dispose 漏れでメモリリーク | 高 | Disposer 階層の厳密な管理 + WeakReference での検証 |
| `SwingComposeTheme` の適用漏れでテーマ不整合 | 中 | `ComposePanelFactory` に一元化して抜け漏れ防止 |
| JBTabs 間の D&D 実装が複雑 | 中 | Phase 5 を後回しにし、まず Context Menu での分割を安定させる |
| 既存の `SessionHistoryAction` との互換性 | 低 | `TabManager.resumeSession()` の API シグネチャを維持 |
| JBSplitter のネストが深くなりすぎる | 低 | 分割深度の上限 (例: 4) を設けるか、UI 的に制限 |
