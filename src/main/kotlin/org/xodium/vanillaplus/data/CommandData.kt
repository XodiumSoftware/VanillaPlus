/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack

/**
 * Represents the data structure for command-related information.
 * @property commands A collection of command builders.
 * @property description A description of the command.
 * @property aliases A list of aliases for the command.
 */
data class CommandData(
    val commands: Collection<LiteralArgumentBuilder<CommandSourceStack>>,
    val description: String,
    val aliases: List<String>
)
