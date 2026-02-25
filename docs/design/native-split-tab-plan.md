# ToolWindow ネイティブ Split 機能 実装計画

## 背景

### これまでの試行と失敗

1. **標準 `ContentManager`**: 分割機能なし
2. **`JBSplitter` + `JBTabs` 独自実装**: 複雑すぎ、ネイティブ L&F の再現が困難
3. **`RunnerLayoutUi`**: Pill スタイルのタブが不適切、エディタ風分割とは異なる

### 決定的な解決策

IntelliJ Platform の `com.intellij.toolWindow.ToolWindowSplitContentProvider` 拡張ポイントを使用する。
標準 ToolWindow を維持したまま、IDE ネイティブの「Split Right / Down」を利用できる。

---

## API 調査結果 (intellij-community ソースより)

### ToolWindowSplitContentProvider

- **ソース**: `platform/platform-impl/src/com/intellij/toolWindow/ToolWindowSplitContentProvider.kt`
- **アノテーション**: `@ApiStatus.Experimental`

```kotlin
interface ToolWindowSplitContentProvider {
    @RequiresEdt
    fun createContentCopy(project: Project, content: Content): Content
}
```

### Extension Point 登録

- **EP名**: `com.intellij.toolWindow.splitContentProvider`
- **Bean**: `ToolWindowSplitContentProviderBean` (`toolWindowId` + `implementationClass`)

```xml
<toolWindow.splitContentProvider
    toolWindowId="YOUR_ID"
    implementationClass="com.example.YourProvider"/>
```

### Split アクションの呼び出しフロー

`ToolWindowSplitActionBase` (platform/platform-impl):

```kotlin
override fun actionPerformed(e: AnActionEvent, toolWindow: ToolWindow, content: Content?) {
    val splitProvider = ToolWindowSplitContentProviderBean.getForToolWindow(toolWindow.id) ?: return
    val decorator = findNearestDecorator(e) ?: return
    if (content == null) return

    val newContent = splitProvider.createContentCopy(toolWindow.project, content)
    decorator.splitWithContent(newContent, if (isRight) SwingConstants.RIGHT else SwingConstants.BOTTOM, -1)
}

override fun update(e: AnActionEvent, toolWindow: ToolWindow, content: Content?) {
    e.presentation.isEnabledAndVisible = toolWindow.canSplitTabs() &&
        ToolWindowSplitContentProviderBean.getForToolWindow(toolWindow.id) != null
}
```

### 有効化の前提条件

`ToolWindow.canSplitTabs()` が `true` を返す必要がある。
`ToolWindow.setTabsSplittingAllowed(true)` を呼び出すことで有効化。

```java
// platform/ide-core/src/com/intellij/openapi/wm/ToolWindow.java
@ApiStatus.Experimental
default boolean canSplitTabs() { return false; }

@ApiStatus.Experimental
default void setTabsSplittingAllowed(boolean allowed) { }
```

### 参考実装: Terminal プラグイン

`plugins/terminal/frontend/src/com/intellij/terminal/frontend/view/impl/TerminalToolWindowSplitContentProvider.kt`:

- Split 時に **新規ターミナルセッション** を生成（元タブのコピーではない）
- 元タブの作業ディレクトリを引き継ぐ
- `shouldAddToToolWindow(false)` — Content 自体は返すだけで `addContent` しない

### Jewel addComposeTab の内部動作

`platform/jewel/ide-laf-bridge/src/main/kotlin/org/jetbrains/jewel/bridge/ToolWindowExtensions.kt`:

```kotlin
fun ToolWindow.addComposeTab(..., content: @Composable ToolWindowScope.() -> Unit) {
    enableNewSwingCompositing()
    val tabContent = contentManager.factory.createContent(
        JewelComposePanel(focusOnClickInside) { scope.content() },
        tabDisplayName, isLockable,
    )
    tabContent.isCloseable = isCloseable
    contentManager.addContent(tabContent)
}
```

`JewelComposePanel` は `SwingBridgeTheme` でラップした `ComposePanel` を持つ `JComponent`。
`addComposeTab` = `JewelComposePanel` 生成 + `ContentFactory.createContent()` + `addContent()` の3ステップ。

---

## 実装ステップ

### Step 1: TabManager に Content 生成ロジックを抽出

**変更ファイル**: `plugin/src/main/kotlin/me/matsumo/agentguiplugin/service/TabManager.kt`

現在の `addTab()` / `resumeSession()` は `toolWindow.addComposeTab()` に直接依存している。
これを「Content を生成して返すメソッド」と「ContentManager に追加するメソッド」に分離する。

#### Before

```kotlin
fun addTab(title: String = "New chat") {
    val vm = createViewModel()
    toolWindow.addComposeTab(title, focusOnClickInside = true, isCloseable = true) {
        LaunchedEffect(vm) { ... }
        ChatPanel(viewModel = vm, project = project)
    }
    val content = toolWindow.contentManager.contents.last()
    viewModels[content] = vm
    toolWindow.contentManager.setSelectedContent(content, true)
    observeTitle(content, vm)
}
```

#### After

```kotlin
import com.intellij.ui.content.ContentFactory
import org.jetbrains.jewel.bridge.JewelComposePanel
import org.jetbrains.jewel.foundation.enableNewSwingCompositing

/**
 * ChatViewModel と Compose UI を持つ Content を生成して返す。
 * ContentManager への追加は呼び出し側の責務。
 */
fun createContent(
    title: String = "New chat",
    vm: ChatViewModel = createViewModel(),
    shouldAutoStart: Boolean = true,
): Content {
    enableNewSwingCompositing()

    val component = JewelComposePanel(focusOnClickInside = true) {
        if (shouldAutoStart) {
            LaunchedEffect(vm) {
                if (vm.uiState.value.sessionState == SessionState.Disconnected) {
                    vm.start()
                }
            }
        }
        ChatPanel(viewModel = vm, project = project)
    }

    val content = ContentFactory.getInstance().createContent(component, title, false)
    content.isCloseable = true

    viewModels[content] = vm
    observeTitle(content, vm)

    return content
}

/**
 * 新しいチャットタブを追加して選択する。
 */
fun addTab(title: String = "New chat") {
    val content = createContent(title)
    toolWindow.contentManager.addContent(content)
    toolWindow.contentManager.setSelectedContent(content, true)
}
```

`resumeSession()` も同様にリファクタリング:

```kotlin
fun resumeSession(summary: SessionSummary, historyMessages: List<ChatMessage>) {
    val title = summary.firstPrompt?.take(40) ?: "Resumed session"
    val vm = createViewModel()
    vm.importHistory(historyMessages)

    val content = createContent(
        title = title,
        vm = vm,
        shouldAutoStart = false,
    )

    // Content 内の LaunchedEffect では autoStart しないので、Compose 側で resume を処理
    // → shouldAutoStart=false のケースでは LaunchedEffect 内で resumeSessionId 付きの start を行う
    // 方法: createContent に resumeSessionId パラメータを追加するか、
    //       vm.start(resumeSessionId) を Compose の LaunchedEffect で呼ぶ形に統一

    toolWindow.contentManager.addContent(content)
    toolWindow.contentManager.setSelectedContent(content, true)
}
```

> **Note**: `resumeSession` のリファクタでは、`createContent` に `resumeSessionId` パラメータを追加して LaunchedEffect 内で `vm.start(resumeSessionId = ...)` を呼ぶ形に統一するのがクリーン。

### Step 2: AgentToolWindowFactory で Split を有効化

**変更ファイル**: `plugin/src/main/kotlin/me/matsumo/agentguiplugin/toolwindow/AgentToolWindowFactory.kt`

```kotlin
override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    // Split 機能を有効化
    toolWindow.setTabsSplittingAllowed(true)

    val sessionService = project.service<SessionService>()
    val tabManager = sessionService.getOrCreateTabManager(toolWindow)
    // ... 以下は既存のまま
}
```

### Step 3: AgentSplitContentProvider を新規作成

**新規ファイル**: `plugin/src/main/kotlin/me/matsumo/agentguiplugin/toolwindow/AgentSplitContentProvider.kt`

```kotlin
package me.matsumo.agentguiplugin.toolwindow

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.toolWindow.ToolWindowSplitContentProvider
import com.intellij.ui.content.Content
import me.matsumo.agentguiplugin.service.SessionService

class AgentSplitContentProvider : ToolWindowSplitContentProvider {
    override fun createContentCopy(project: Project, content: Content): Content {
        val sessionService = project.service<SessionService>()
        val tabManager = sessionService.tabManager
            ?: error("TabManager not initialized")

        // Split 時は新規チャットセッションを生成して返す
        // ContentManager への addContent は Split アクション側が splitWithContent() で処理する
        return tabManager.createContent()
    }
}
```

**設計判断**: Terminal プラグインと同様、Split 時は **新しいチャットセッション** を生成する。
元タブの「コピー」ではなく、独立した新規チャットを作成。

### Step 4: SessionService の tabManager を公開

**変更ファイル**: `plugin/src/main/kotlin/me/matsumo/agentguiplugin/service/SessionService.kt`

```kotlin
@Service(Service.Level.PROJECT)
class SessionService(private val project: Project) : Disposable {
    private val scope = CoroutineScope(SupervisorJob())

    // private var tabManager → 外部からの読み取りを許可
    var tabManager: TabManager? = null
        private set

    fun getOrCreateTabManager(toolWindow: ToolWindow): TabManager {
        return tabManager ?: TabManager(
            toolWindow = toolWindow,
            project = project,
            settingsService = service<SettingsService>(),
            scope = scope,
        ).also { tabManager = it }
    }

    override fun dispose() {
        tabManager?.dispose()
        tabManager = null
        scope.cancel()
    }
}
```

### Step 5: plugin.xml に Extension を登録

**変更ファイル**: `plugin/src/main/resources/META-INF/plugin.xml`

```xml
<extensions defaultExtensionNs="com.intellij">
    <toolWindow id="Agent GUI"
                factoryClass="me.matsumo.agentguiplugin.toolwindow.AgentToolWindowFactory"
                anchor="right"
                canCloseContents="true"
                icon="AllIcons.Actions.Execute"/>

    <toolWindow.splitContentProvider
        toolWindowId="Agent GUI"
        implementationClass="me.matsumo.agentguiplugin.toolwindow.AgentSplitContentProvider"/>

    <projectService serviceImplementation="me.matsumo.agentguiplugin.service.SessionService"/>
    <projectService serviceImplementation="me.matsumo.agentguiplugin.service.SessionHistoryService"/>
    <applicationService serviceImplementation="me.matsumo.agentguiplugin.service.SettingsService"/>
</extensions>
```

---

## ライフサイクル管理

### 既存の ContentManagerListener がそのまま機能する理由

1. `ToolWindowSplitActionBase` が `decorator.splitWithContent(newContent, ...)` を呼ぶ
2. 内部で `contentManager.addContent(newContent)` 相当の処理が行われる
3. ユーザーが分割タブを閉じると、通常タブと同様に `contentRemoved` イベントが発生
4. `viewModels[content]` → `vm.dispose()` が実行される

### Content → ViewModel の紐づけタイミング

`createContent()` 内で `viewModels[content] = vm` と `observeTitle()` を実行するため、
Content が ContentManager に追加される前にマップエントリが存在する。
`contentRemoved` 時点で確実にクリーンアップが行われる。

### ComposePanel のクリーンアップ

`JewelComposePanelWrapper` は `removeNotify()` で AWTEventListener を解除する。
Content が削除されると Swing ツリーから除外され、`removeNotify()` が自動的に呼ばれる。

---

## 変更ファイル一覧

| ファイル | 操作 | 概要 |
|---------|------|------|
| `service/TabManager.kt` | 修正 | `createContent()` を抽出。`addComposeTab` → `JewelComposePanel` + `ContentFactory` に変更 |
| `service/SessionService.kt` | 修正 | `tabManager` の可視性を `private` → `var ... private set` に変更 |
| `toolwindow/AgentToolWindowFactory.kt` | 修正 | `setTabsSplittingAllowed(true)` を追加 |
| `toolwindow/AgentSplitContentProvider.kt` | 新規 | `ToolWindowSplitContentProvider` の実装 |
| `META-INF/plugin.xml` | 修正 | `toolWindow.splitContentProvider` の登録を追加 |

---

## リスク・注意事項

| リスク | 対策 |
|--------|------|
| `@ApiStatus.Experimental` — 将来の破壊的変更の可能性 | Terminal プラグインが使用中のため当面は安定。バージョンアップ時に要確認 |
| `enableNewSwingCompositing()` の呼び忘れ | `createContent()` の先頭で必ず呼ぶ（idempotent） |
| `sinceBuild` の整合性 | `253.31033` で `canSplitTabs` / `setTabsSplittingAllowed` が存在することを intellij-community ソースで確認済み |
| Split 後のタブ title 更新 | `observeTitle()` は `createContent()` 内で登録されるため、Split タブでも動作する |
