package com.github.erotourtes.harpoon.listeners

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ProjectOnStartListenerTest {
    @Test
    fun `should append gitignore entry without padding after existing content`() {
        val message = buildGitignoreMessage("*.iml")

        assertEquals("\n# Harpooner\nHarpooner Menu", message)
    }

    @Test
    fun `should append gitignore entry without leading newline on empty file`() {
        val message = buildGitignoreMessage("")

        assertEquals("# Harpooner\nHarpooner Menu", message)
    }

    @Test
    fun `should append gitignore entry without extra blank line after trailing newline`() {
        val message = buildGitignoreMessage("*.iml\n")

        assertEquals("# Harpooner\nHarpooner Menu", message)
    }
}
