package org.xodium.illyriaplus

import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.TooltipDisplay
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.Item

/** General utilities. */
@Suppress("UnstableApiUsage")
internal object Utils {
    /** MiniMessage instance for parsing formatted strings. */
    val MM: MiniMessage = MiniMessage.miniMessage()

    /** The standardized prefix for [IllyriaKingdoms] messages. */
    val IllyriaKingdoms.prefix: String
        get() =
            "<gradient:#FFA751:#FFE259>[</gradient><gradient:#CB2D3E:#EF473A>" +
                "${this.javaClass.simpleName}" +
                "</gradient><gradient:#FFE259:#FFA751>]</gradient>"

    /** GUI-related utilities and pre-built items for InvUI. */
    object GUI {
        /** The default GUI filler item using black stained glass panes with hidden tooltips. */
        val FILLER_ITEM: Item =
            Item
                .builder()
                .setItemProvider(
                    ItemStack.of(Material.BLACK_STAINED_GLASS_PANE).apply {
                        setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true))
                    },
                ).build()

        /** Bound item for navigating to the previous page in the paginated GUI. */
        val PREVIOUS_PAGE_ITEM: BoundItem =
            BoundItem
                .pagedBuilder()
                .setItemProvider(
                    ItemStack.of(Material.ARROW).apply {
                        setData(DataComponentTypes.ITEM_NAME, MM.deserialize("<gray>Previous Page"))
                    },
                ).addClickHandler { _, gui, _ -> gui.page-- }
                .build()

        /** Bound item for navigating to the next page in the paginated GUI. */
        val NEXT_PAGE_ITEM: BoundItem =
            BoundItem
                .pagedBuilder()
                .setItemProvider(
                    ItemStack.of(Material.ARROW).apply {
                        setData(DataComponentTypes.ITEM_NAME, MM.deserialize("<gray>Next Page"))
                    },
                ).addClickHandler { _, gui, _ -> gui.page++ }
                .build()
    }

    object Command {
        /**
         * Registers a command execution handler on an [com.mojang.brigadier.builder.ArgumentBuilder] with automatic try/catch handling.
         * @param action The action executed when the command runs.
         * @return The same [com.mojang.brigadier.builder.ArgumentBuilder] for further configuration.
         */
        fun <T : ArgumentBuilder<CommandSourceStack, T>> T.executesCatching(
            action: (CommandContext<CommandSourceStack>) -> Unit,
        ): T {
            executes { ctx ->
                runCatching { action(ctx) }
                    .onFailure {
                        IllyriaKingdoms.instance.logger.severe(
                            """
                            Command error: ${it.message}
                            ${it.stackTraceToString()}
                            """.trimIndent(),
                        )
                        (ctx.source.sender as? Player)?.sendMessage(
                            MM.deserialize(
                                "${IllyriaKingdoms.instance.prefix} " +
                                    "<red>An error has occurred. Check server logs for details.",
                            ),
                        )
                    }
                com.mojang.brigadier.Command.SINGLE_SUCCESS
            }
            return this
        }

        /**
         * Registers a command execution handler on an [ArgumentBuilder] specifically for [Player] senders with automatic try/catch handling.
         * @param action The action executed when the command runs, receiving the [Player] and [CommandContext].
         * @return The same [ArgumentBuilder] for further configuration.
         * @throws IllegalStateException if the command is executed by a non-[Player] sender.
         */
        fun <T : ArgumentBuilder<CommandSourceStack, T>> T.playerExecuted(
            action: (Player, CommandContext<CommandSourceStack>) -> Unit,
        ): T {
            executesCatching {
                action(
                    it.source.sender as? Player ?: run {
                        IllyriaKingdoms.instance.logger.warning("Command can only be executed by a Player!")
                        return@executesCatching
                    },
                    it,
                )
            }
            return this
        }
    }
}
