package org.xodium.vanillaplus.modules

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.function.mask.BlockTypeMask
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.math.transform.AffineTransform
import com.sk89q.worldedit.session.ClipboardHolder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.world.StructureGrowEvent
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.hooks.FAWEHook
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.registries.MaterialRegistry
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.prefix
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import java.io.IOException
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.stream.Collectors

/** Represents a module handling tree mechanics within the system. */
internal class TreesModule : ModuleInterface<TreesModule.Config> {
    override val config: Config = Config()

    /** A map of sapling materials to a list of schematics. */
    private val schematicCache: Map<Material, List<Clipboard>> by lazy {
        config.saplingLink.mapValues { (_, dirs) ->
            dirs.flatMap { dir -> loadSchematics("/schematics/$dir") }
        }
    }

    override fun enabled(): Boolean = config.enabled && FAWEHook.get("TreesModule")

    override fun cmds(): List<CommandData> =
        listOf(
            CommandData(
                Commands
                    .literal("tree")
                    .requires { it.sender.hasPermission(perms()[0]) }
                    .then(
                        Commands
                            .argument("type", StringArgumentType.string())
                            .suggests { _, builder ->
                                config.saplingLink.keys.forEach { material ->
                                    builder.suggest(
                                        material.name
                                            .removeSuffix("_SAPLING")
                                            .removeSuffix("_PROPAGULE")
                                            .removeSuffix("_FUNGUS")
                                            .lowercase(),
                                    )
                                }
                                builder.buildFuture()
                            }.then(
                                Commands
                                    .argument("index", StringArgumentType.string())
                                    .suggests { ctx, builder ->
                                        StringArgumentType.getString(ctx, "type").toMaterial()?.let { material ->
                                            schematicCache[material]?.let { schematics ->
                                                schematics.indices.forEach { index ->
                                                    builder.suggest(index.toString())
                                                }
                                            }
                                        }
                                        builder.buildFuture()
                                    }.executes { ctx ->
                                        ctx.tryCatch {
                                            if (it.sender !is Player) instance.logger.warning("Command can only be executed by a Player!")
                                            handleTreeCmd((it.sender as Player), ctx, true)
                                        }
                                    },
                            ).executes { ctx ->
                                ctx.tryCatch {
                                    if (it.sender !is Player) instance.logger.warning("Command can only be executed by a Player!")
                                    handleTreeCmd((it.sender as Player), ctx, false)
                                }
                            },
                    ),
                "Triggers the spawning of a tree",
                listOf("tr"),
            ),
        )

    override fun perms(): List<Permission> =
        listOf(
            Permission(
                "${instance::class.simpleName}.tree".lowercase(),
                "Allows use of the tree command",
                PermissionDefault.OP,
            ),
        )

    /**
     * Handle the StructureGrowEvent.
     * @param event The StructureGrowEvent.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: StructureGrowEvent) {
        if (!enabled()) return
        event.location.block
            .takeIf {
                Tag.SAPLINGS.isTagged(it.type) ||
                    it.type == Material.WARPED_FUNGUS ||
                    it.type == Material.CRIMSON_FUNGUS
            }?.let { event.isCancelled = pasteSchematic(it) }
    }

    /**
     * Load schematics from the specified resource directory.
     * @param resourceDir The directory containing the schematics.
     * @return A list of loaded schematics.
     */
    private fun loadSchematics(resourceDir: String): List<Clipboard> {
        val url = javaClass.getResource(resourceDir) ?: error("Resource directory not found: $resourceDir")
        return try {
            FileSystems.newFileSystem(url.toURI(), mapOf("create" to false)).use { fs ->
                val dirPath = fs.getPath(resourceDir.removePrefix("/"))
                Files
                    .walk(dirPath, 1)
                    .filter { Files.isRegularFile(it) }
                    .collect(Collectors.toList())
                    .also { if (it.isEmpty()) error("No schematics found in directory: $resourceDir") }
                    .map { path ->
                        Files.newByteChannel(path, StandardOpenOption.READ).use { channel ->
                            readClipboard(path, channel)
                        }
                    }
            }
        } catch (e: IOException) {
            error("Failed to load schematics from $resourceDir: ${e.message}")
        }
    }

    /**
     * Read a schematic from the specified path.
     * @param path The path to the schematic file.
     * @param channel The channel to read the schematic from.
     * @return The loaded schematic.
     */
    private fun readClipboard(
        path: Path,
        channel: ReadableByteChannel,
    ): Clipboard {
        val format = ClipboardFormats.findByAlias("schem") ?: error("Unsupported schematic format for resource: $path")
        return try {
            format.getReader(Channels.newInputStream(channel)).read()
        } catch (e: Exception) {
            throw IOException("Failed to read schematic $path: ${e.message}", e)
        }
    }

    /**
     * Paste a random schematic at the specified block.
     * @param block The block to paste the schematic at.
     * @return True if the schematic was pasted successfully.
     */
    private fun pasteSchematic(block: Block): Boolean {
        val clipboards = schematicCache[block.type] ?: return false
        return pasteSchematic(block, clipboards.random())
    }

    /**
     * Paste a schematic at the specified block.
     * @param block The block to paste the schematic at.
     * @param clipboard The specific clipboard to paste.
     * @return True if the schematic was pasted successfully.
     */
    private fun pasteSchematic(
        block: Block,
        clipboard: Clipboard,
    ): Boolean {
        instance.server.scheduler.runTask(
            instance,
            Runnable {
                try {
                    WorldEdit
                        .getInstance()
                        .newEditSession(BukkitAdapter.adapt(block.world))
                        .use { editSession ->
                            block.type = Material.AIR
                            editSession.mask =
                                BlockTypeMask(
                                    editSession,
                                    MaterialRegistry.TREE_MASK.map { BukkitAdapter.asBlockType(it) },
                                )
                            ClipboardHolder(clipboard).apply {
                                transform = transform.combine(AffineTransform().rotateY(getRandomRotation().toDouble()))
                                Operations.complete(
                                    createPaste(editSession)
                                        .to(BlockVector3.at(block.x, block.y, block.z))
                                        .copyBiomes(config.copyBiomes)
                                        .copyEntities(config.copyEntities)
                                        .ignoreAirBlocks(config.ignoreAirBlocks)
                                        .ignoreStructureVoidBlocks(config.ignoreStructureVoidBlocks)
                                        .build(),
                                )
                            }
                        }
                } catch (e: Exception) {
                    instance.logger.severe("Error while pasting schematic: ${e.message}")
                }
            },
        )
        return true
    }

    /**
     * Handles the `/tree` command execution for spawning a tree structure.
     * @param player The [Player] executing the command.
     * @param ctx The [CommandContext] containing command arguments and context.
     * @param hasIndex Whether the command has an index argument.
     */
    private fun handleTreeCmd(
        player: Player,
        ctx: CommandContext<CommandSourceStack>,
        hasIndex: Boolean,
    ) {
        val typeName = StringArgumentType.getString(ctx, "type")
        val material = typeName.toMaterial() ?: return
        val clipboards = schematicCache[material] ?: return
        val clipboard =
            if (hasIndex) {
                val index = StringArgumentType.getString(ctx, "index").toInt()
                if (index < 0 || index >= clipboards.size) return
                clipboards[index]
            } else {
                clipboards.random()
            }
        try {
            val actor = BukkitAdapter.adapt(player)
            val session = WorldEdit.getInstance().sessionManager.get(actor)
            session.clipboard = ClipboardHolder(clipboard)
            player.sendMessage("${instance.prefix} Loaded $typeName tree into clipboard! Use //paste to place it".mm())
        } catch (e: Exception) {
            instance.logger.severe("Error while setting clipboard: ${e.message}")
            player.sendMessage("${instance.prefix} ${"Error Occurred, Check Console!".fireFmt()}".mm())
        }
    }

    /**
     * Attempts to resolve this [String] as a [Material], optionally trying suffixes
     * like "_SAPLING", "_PROPAGULE", and "_FUNGUS" if a direct match fails.
     * @receiver The material name to search for.
     * @return The matching [Material], or `null` if no match is found.
     */
    private fun String.toMaterial(): Material? {
        val name = this.uppercase()
        return Material.matchMaterial(name)
            ?: Material.matchMaterial("${name}_SAPLING")
            ?: Material.matchMaterial("${name}_PROPAGULE")
            ?: Material.matchMaterial("${name}_FUNGUS")
    }

    /**
     * Returns a random rotation angle from the set {0, 90, 180, 270}.
     * @return An integer representing a rotation angle in degrees.
     */
    private fun getRandomRotation() = listOf(0, 90, 180, 270).random()

    data class Config(
        override var enabled: Boolean = true,
        var copyBiomes: Boolean = false,
        var copyEntities: Boolean = false,
        var ignoreAirBlocks: Boolean = true,
        var ignoreStructureVoidBlocks: Boolean = true,
        var saplingLink: Map<Material, List<String>> =
            mapOf(
                Material.ACACIA_SAPLING to listOf("trees/acacia"),
                Material.BIRCH_SAPLING to listOf("trees/birch"),
                Material.CHERRY_SAPLING to listOf("trees/cherry"),
                Material.CRIMSON_FUNGUS to listOf("trees/crimson"),
                Material.DARK_OAK_SAPLING to listOf("trees/dark_oak"),
                Material.JUNGLE_SAPLING to listOf("trees/jungle"),
                Material.MANGROVE_PROPAGULE to listOf("trees/mangrove"),
                Material.OAK_SAPLING to listOf("trees/oak"),
                Material.PALE_OAK_SAPLING to listOf("trees/pale_oak"),
                Material.SPRUCE_SAPLING to listOf("trees/spruce"),
                Material.WARPED_FUNGUS to listOf("trees/warped"),
            ),
    ) : ModuleInterface.Config
}
