/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.block.SignChangeEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.xodium.vanillaplus.utils.ExtUtils.mm

@Suppress("unused")
class SignModuleTests {

    private lateinit var signModule: SignModule
    private lateinit var mockEvent: SignChangeEvent

    @BeforeEach
    fun setUp() {
        signModule = SignModule()
        mockEvent = mock()
    }

    @Test
    fun `onSignChangeEvent should parse MiniMessage when module is enabled`() {
        // Arrange
        signModule.config.enabled = true
        val initialLines = mutableListOf<Component>(
            Component.text("<red>Hello</red>"),
            Component.text("World")
        )
        whenever(mockEvent.lines()).thenReturn(initialLines)

        // Act
        signModule.on(mockEvent)

        // Assert
        val expectedLines = listOf(
            Component.text("Hello").color(NamedTextColor.RED),
            "World".mm()
        )

        assertEquals(expectedLines[0], initialLines[0])
        assertEquals(expectedLines[1], initialLines[1])
    }

    @Test
    fun `onSignChangeEvent should not do anything when module is disabled`() {
        // Arrange
        signModule.config.enabled = false
        val initialLines = mutableListOf<Component>(
            Component.text("<red>Hello</red>")
        )
        val originalLines = ArrayList(initialLines)
        whenever(mockEvent.lines()).thenReturn(initialLines)

        // Act
        signModule.on(mockEvent)

        // Assert
        assertEquals(originalLines, initialLines)
        verify(mockEvent, never()).lines()
    }
}