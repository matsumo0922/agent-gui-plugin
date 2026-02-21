rootProject.name = "agent-gui-plugin"

include(":plugin")

includeBuild("claude-agent-sdk-kotlin") {
    dependencySubstitution {
        substitute(module("me.matsumo.claude.agent:agent"))
            .using(project(":agent"))
    }
}
