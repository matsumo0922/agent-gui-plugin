# IntelliJ Platform ContentManager & Compose Tab API

## Overview

This document provides comprehensive guidance on using the IntelliJ Platform's ContentManager API to manage dynamic tabs in ToolWindows, along with information about the Jewel bridge's `addComposeTab()` extension function.

---

## ContentManager API

### Location and Access

The ContentManager API is located in `com.intellij.ui.content.*` package. Access it from a ToolWindow instance:

```kotlin
val toolWindow: ToolWindow = /* obtained from ToolWindowManager */
val contentManager: ContentManager = toolWindow.getContentManager()
```

### Core Methods

#### Adding Tabs/Content

```kotlin
/**
 * Add a Content (tab) to the ContentManager.
 *
 * @param content The Content object to add
 */
void addContent(@NotNull Content content)

/**
 * Add a Content at a specific position.
 *
 * @param content The Content object to add
 * @param order The position index
 */
void addContent(@NotNull Content content, int order)
```

#### Removing Tabs/Content

```kotlin
/**
 * Remove a Content (tab) from the ContentManager.
 *
 * @param content The Content object to remove
 * @param dispose Whether to call dispose() on the removed content
 * @return true if the content was removed
 */
boolean removeContent(@NotNull Content content, boolean dispose)

/**
 * Remove all contents from the manager.
 */
void removeAllContents(boolean dispose)
```

#### Managing Selection

```kotlin
/**
 * Make a specific Content the active/selected tab.
 *
 * @param content The Content to select
 */
void setSelectedContent(@NotNull Content content)

/**
 * Make a specific Content the active/selected tab with focus control.
 *
 * @param content The Content to select
 * @param requestFocus Whether to request focus for the tab
 */
void setSelectedContent(@NotNull Content content, boolean requestFocus)

/**
 * Get the currently selected Content.
 *
 * @return The active Content, or null if none selected
 */
@Nullable Content getSelectedContent()

/**
 * Get all selected contents (usually just one).
 *
 * @return Array of selected Contents
 */
@NotNull Content[] getSelectedContents()

/**
 * Select the next tab.
 */
void selectNextContent()

/**
 * Select the previous tab.
 */
void selectPreviousContent()
```

#### Querying Content

```kotlin
/**
 * Get the total number of tabs/contents.
 *
 * @return The number of contents
 */
int getContentCount()

/**
 * Get all contents.
 *
 * @return Array of all Content objects
 */
@NotNull Content[] getContents()

/**
 * Get the index of a specific Content.
 *
 * @param content The Content to find
 * @return Index of the content, or -1 if not found
 */
int getIndexOfContent(@NotNull Content content)

/**
 * Find content by a specific name.
 *
 * @param name The name to search for
 * @return The Content with that name, or null
 */
@Nullable Content findContent(@NotNull String name)
```

---

## Creating Content Objects

### ContentFactory

Use the factory to create Content objects:

```kotlin
val contentFactory = ContentFactory.SERVICE.getInstance()

// Create a simple Content from a Swing component
val myComponent: JComponent = /* your component */
val content: Content = contentFactory.createContent(
    myComponent,
    "Tab Title",   // Display name
    true           // Is closable
)
```

### Content Configuration

```kotlin
// Set the preferred component to receive focus
content.setPreferredFocusableComponent(focusableComponent)

// Register a Disposable for cleanup
content.setDisposer(Disposable {
    // Clean up resources when tab is closed
})

// Get the component from a Content
val component: JComponent = content.component

// Get the display name
val title: String = content.displayName

// Check if closable
val isClosable: Boolean = content.isClosable()

// Set closable status (note: may be overridden by canCloseContents policy)
content.isClosable = true
```

### Note on Tab Closability

**Important**: The `canCloseContents` parameter must be set when registering the tool window (either in `plugin.xml` via `<toolWindow canCloseContents="true">` or programmatically). Setting `Content.isClosable = true` alone will not work without this setting.

---

## Listening for Tab Changes

### ContentManagerListener

Register a listener to monitor content manager events:

```kotlin
contentManager.addContentManagerListener(object : ContentManagerListener {
    /**
     * Called when content selection changes.
     *
     * @param event The selection change event
     */
    override fun selectionChanged(event: ContentManagerEvent) {
        val newContent: Content = event.content
        val oldContent: Content? = event.oldContent
        val operation: ContentManagerEvent.ContentOperation = event.operation
        // Handle selection change
    }

    /**
     * Called when content is removed from the manager.
     *
     * @param event The removal event
     */
    override fun contentRemoved(event: ContentManagerEvent) {
        val removedContent: Content = event.content
        // Handle content removal
    }

    /**
     * Called when content is added to the manager.
     *
     * @param event The addition event
     */
    override fun contentAdded(event: ContentManagerEvent) {
        val addedContent: Content = event.content
        // Handle content addition
    }
})

// To remove a listener
contentManager.removeContentManagerListener(listener)
```

### ContentManagerEvent

The event object provides context about what changed:

```kotlin
class ContentManagerEvent {
    val content: Content           // The affected Content
    val oldContent: Content?       // Previous content (for selection changes)
    val operation: ContentOperation // Type of change (ADDED, REMOVED, SELECTED, etc.)
}
```

---

## Typical Pattern: Dynamic Tab Management

### Creating and Managing Multiple Tabs

```kotlin
class DynamicTabManager(private val toolWindow: ToolWindow) {
    private val contentManager = toolWindow.getContentManager()

    fun addNewTab(title: String, component: JComponent): Content {
        val content = ContentFactory.SERVICE.getInstance()
            .createContent(component, title, true)
        content.setDisposer(Disposable {
            // Cleanup when tab is closed
        })
        contentManager.addContent(content)
        contentManager.setSelectedContent(content)  // Activate the new tab
        return content
    }

    fun removeTab(content: Content) {
        contentManager.removeContent(content, true)  // dispose=true
    }

    fun getAllTabs(): List<Content> {
        return contentManager.getContents().toList()
    }

    fun getCurrentTab(): Content? {
        return contentManager.getSelectedContent()
    }

    fun switchToTab(content: Content) {
        contentManager.setSelectedContent(content, requestFocus = true)
    }
}
```

---

## Jewel Bridge: Compose Tab Integration

### ToolWindow.addComposeTab() Extension

The Jewel bridge provides a Compose-specific extension function to simplify adding Compose-based tabs to IntelliJ tool windows:

```kotlin
@Composable
fun MyComposableUI() {
    // Your Compose UI code
}

// In ToolWindowFactory.createToolWindowContent()
toolWindow.addComposeTab(
    title = "Claude Code",              // Tab title (nullable)
    focusOnClickInside = true           // Request focus when clicked
) {
    MyComposableUI()
}
```

### Key Characteristics

1. **Wraps ContentManager**: Internally uses ContentManager API to create and manage Compose-based content
2. **Nullable Title**: Recent updates made the tab title parameter nullable, providing more flexibility
3. **ComposePanel Integration**: Bridges Swing/IntelliJ APIs with Compose Desktop rendering
4. **Disposable Management**: Automatically handles resource cleanup when tabs are closed

### Current Limitations of addComposeTab

**Important Discovery**: `addComposeTab()` is designed to create a **single Compose root** per tool window invocation. If you call `addComposeTab()` multiple times in the same `createToolWindowContent()` call, **only the last one will be visible** because each call replaces the ContentManager's view.

### Solution: Hybrid Approach for Dynamic Tabs

To support dynamic tab creation with Compose, use a **hybrid approach**:

1. **Use ContentManager directly** for tab management (Swing-side API)
2. **Wrap each tab's content** with a `ComposePanel` to render Compose UI

```kotlin
class AgentToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentManager = toolWindow.getContentManager()
        val sessionService = project.service<SessionService>()

        // Create initial tab with Compose content
        addComposeTabWithManager(
            contentManager,
            "Chat",
            project,
            sessionService
        )
    }

    private fun addComposeTabWithManager(
        contentManager: ContentManager,
        title: String,
        project: Project,
        sessionService: SessionService
    ) {
        val tabViewModel = sessionService.getOrCreateTabViewModel()

        // Create a ComposePanel to render Compose UI
        val composePanel = ComposePanel()
        composePanel.setContent {
            val tvm = remember { tabViewModel }
            val activeVm by tvm.activeChatViewModel.collectAsState()

            LaunchedEffect(activeVm) {
                val vm = activeVm ?: return@LaunchedEffect
                if (vm.uiState.value.sessionState == SessionState.Disconnected) {
                    vm.start()
                }
            }

            ChatPanel(tabViewModel = tvm, project = project)
        }

        // Create Content from the ComposePanel
        val content = ContentFactory.SERVICE.getInstance()
            .createContent(composePanel, title, false)

        // Add to content manager
        contentManager.addContent(content)
        contentManager.setSelectedContent(content)
    }
}
```

### Handling Multiple Dynamic Tabs

For the multi-tab feature as described in `docs/design/multi-tab-plan.md`, the architecture should be:

1. **Inside the single Compose root** (created once by `addComposeTab`), implement tab management with Compose state:
   - `TabBar` Compose component displays tab list
   - `TabViewModel` manages active tab state
   - `ChatPanel` renders based on current tab selection
   - No ContentManager-level tabs needed

OR

2. **If ContentManager-level tabs are desired**, use ComposePanel wrapping per tab:
   - Each call to `addComposeTabWithManager()` creates one physical tab
   - ContentManager handles tab switching natively
   - More native IDE feel but more complex setup

### Recommended Approach for This Project

Based on the multi-tab plan, **use Compose-only state management** (Option 1):
- Single ContentManager tab (created once)
- Internal Compose `TabBar` for tab switching
- All tab state managed in `TabViewModel`
- Simpler, cleaner, and aligns with existing UI architecture

---

## Best Practices

### 1. Resource Cleanup

Always set a Disposer to clean up resources:

```kotlin
content.setDisposer(Disposable {
    // Stop background jobs
    // Close connections
    // Release other resources
})
```

### 2. Tab Closability Configuration

Ensure tool window supports closable tabs:

```xml
<!-- In plugin.xml -->
<toolWindow id="MyToolWindow"
            anchor="right"
            icon="AllIcons.General.Notifications"
            canCloseContents="true"
            factoryClass="com.example.MyToolWindowFactory"/>
```

### 3. Focus Management

Control where focus goes:

```kotlin
content.setPreferredFocusableComponent(myFocusableComponent)
contentManager.setSelectedContent(content, requestFocus = true)
```

### 4. Selection Handling

Listen for and respond to tab changes:

```kotlin
contentManager.addContentManagerListener(object : ContentManagerListener {
    override fun selectionChanged(event: ContentManagerEvent) {
        val activeTab = contentManager.getSelectedContent()
        // Update UI state, load data, etc.
    }
})
```

### 5. Thread Safety

ContentManager operations should be performed on the EDT (Event Dispatch Thread):

```kotlin
ApplicationManager.getApplication().invokeLater {
    contentManager.addContent(content)
}
```

---

## Comparison: Single Compose Root vs. Multiple Tabs

| Aspect | Single Compose Root | Multiple ContentManager Tabs |
|--------|-------------------|------------------------------|
| **Implementation** | `addComposeTab()` once, manage tabs in Compose | Multiple `ComposePanel` wrappers, ContentManager handles tabs |
| **IDE Integration** | Less native, custom tab appearance | Native IDE tab bar, native feel |
| **State Complexity** | All in `TabViewModel` (Compose) | Split between ContentManager and Compose |
| **Performance** | Single Compose tree, simpler rendering | Multiple Compose instances (more memory) |
| **Recommended For** | Internal tab switching, quick implementation | Deep IDE integration, native look |
| **This Project** | ✓ Recommended | Not recommended initially |

---

## References

- [IntelliJ Platform Plugin SDK - Tool Windows](https://plugins.jetbrains.com/docs/intellij/tool-windows.html)
- [ContentManager API Documentation](https://dploeger.github.io/intellij-api-doc/com/intellij/ui/content/ContentManager.html)
- [JetBrains Jewel - Compose for IntelliJ](https://github.com/JetBrains/jewel)
- [IntelliJ SDK Code Samples - Tool Window](https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/tool_window)
- [JetBrains Compose Plugin Template](https://github.com/JetBrains/intellij-platform-compose-plugin-template)

---

## Summary

**Key Findings:**

1. **ContentManager** is the standard API for managing multiple tabs in ToolWindows
   - Use `addContent()`, `removeContent()`, `setSelectedContent()`
   - Listen with `addContentManagerListener()` for tab changes
   - Requires `canCloseContents="true"` in plugin.xml for closable tabs

2. **addComposeTab()** creates a single Compose root, not multiple tabs
   - Internally uses ContentManager for single tab
   - Cannot be called multiple times for multiple tabs in same ToolWindow
   - Best for embedding Compose UI within a single tab

3. **Recommended for multi-tab feature**:
   - Keep current `addComposeTab()` implementation (single tab)
   - Implement tab management inside Compose (TabBar + TabViewModel)
   - Simpler, cleaner, aligns with project architecture

4. **Alternative (future)**: Use ContentManager with ComposePanel per tab for native IDE tab bar appearance
