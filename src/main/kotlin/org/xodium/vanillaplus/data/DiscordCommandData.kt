/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import dev.kord.core.entity.interaction.ChatInputCommandInteraction

/**
 * Data class representing a Discord command with its name, action, and handler.
 * @property name The name of the command.
 * @property action The action to be performed by the command.
 * @property handler The suspend function that handles the command interaction.
 */
data class DiscordCommandData(
    val name: String,
    val action: String,
    val handler: suspend (ChatInputCommandInteraction, String) -> Unit
)
