package org.xodium.illyriaplus.mechanics.world

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import org.xodium.illyriaplus.Utils.ItemUtils.getCustomName
import org.xodium.illyriaplus.Utils.MM
import org.xodium.illyriaplus.Utils.ScheduleUtils.schedule
import org.xodium.illyriaplus.data.TeleportAnchorData
import org.xodium.illyriaplus.interfaces.MechanicInterface
import org.xodium.illyriaplus.managers.ConfigManager
import xyz.xenondevs.invui.gui.Animation
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemBuilder
import xyz.xenondevs.invui.window.Window
import kotlin.math.cos
import kotlin.math.sin

/** Represents a mechanic handling teleportation within the system. */
internal object TeleportMechanic : MechanicInterface {
    /**
     * Represents the data structure for a collection of teleport anchors.
     *
     * @property anchors The list of [TeleportAnchorData] entries available for teleportation.
     */
    @Serializable
    private data class Anchors(
        val anchors: MutableList<TeleportAnchorData> = mutableListOf(),
    )

    /** Holds user-facing MiniMessage strings for teleport anchor interactions. */
    object Messages {
        const val ANCHOR_CREATION =
            "<gradient:#43A047:#66BB6A><b>You have created a Teleportation Anchor!</b></gradient>"
        const val ANCHOR_REMOVAL =
            "<gradient:#CB2D3E:#EF473A><b>You have removed a Teleportation Anchor!</b></gradient>"
        const val ANCHOR_NAME_UPDATE =
            "<gradient:#FFE259:#FFA751><b>You have successfully changed the Anchor's name!</b></gradient>"
        const val TELEPORT_TITLE =
            "<gradient:#FFE259:#FFA751><b>Teleporting...</b></gradient>"
        const val TELEPORT_SUBTITLE =
            "<gradient:#CB2D3E:#EF473A><b><remaining></b></gradient>"
    }

    private const val CONFIG_FILE = "anchors.json"

    private lateinit var state: Anchors

    override fun register(): Long {
        state = ConfigManager.load(CONFIG_FILE, Anchors())
        return super.register()
    }

    private val TELEPORTING = mutableSetOf<Player>()
    private val PAGE_CHANGE_ANIMATION =
        Animation
            .builder()
            .setSlotSelector(Animation::horizontalSnakeSlotSelector)
            .filterTaggedSlots('x')
            .setFreezing(false)
            .build()
    private val BACK_BUTTON =
        BoundItem
            .pagedBuilder()
            .setItemProvider(ItemBuilder(Material.ARROW).hideTooltip(true))
            .addClickHandler { _, gui, _ ->
                gui.cancelAnimation()
                gui.page--
                gui.playAnimation(PAGE_CHANGE_ANIMATION)
            }.build()
    private val FORWARD_BUTTON =
        BoundItem
            .pagedBuilder()
            .setItemProvider(ItemBuilder(Material.ARROW).hideTooltip(true))
            .addClickHandler { _, gui, _ ->
                gui.cancelAnimation()
                gui.page++
                gui.playAnimation(PAGE_CHANGE_ANIMATION)
            }.build()

    @EventHandler
    fun on(event: PlayerInteractEvent) {
        val block = event.clickedBlock ?: return
        val anchor = state.anchors.firstOrNull { it.matches(block.location) } ?: return

        when {
            event.action != Action.RIGHT_CLICK_BLOCK -> {
                return
            }

            event.clickedBlock?.type != Material.LODESTONE -> {
                return
            }

            event.item?.type == Material.COMPASS -> {
                return
            }

            event.item?.type == Material.NAME_TAG -> {
                rename(event)
                return
            }

            else -> {
                playAnchorFlame(anchor)
                window(block.location, anchor.name).open(event.player)
            }
        }
    }

    @EventHandler
    fun on(event: BlockPlaceEvent) {
        val block = event.blockPlaced

        if (block.type != Material.LODESTONE) return

        val location = block.location

        if (state.anchors.any { it.matches(block.location) }) return

        state.anchors.add(TeleportAnchorData(block.world, location, TeleportAnchorData.nextName(state.anchors)))
        save()
        event.player.sendActionBar(MM.deserialize(Messages.ANCHOR_CREATION))
    }

    @EventHandler
    fun on(event: BlockBreakEvent) {
        val block = event.block

        if (block.type != Material.LODESTONE) return
        if (!state.anchors.removeIf { it.matches(block.location) }) return

        save()
        event.player.sendActionBar(MM.deserialize(Messages.ANCHOR_REMOVAL))
    }

    /** Persists the current anchor state to the plugin data folder. */
    private fun save() = ConfigManager.save(CONFIG_FILE, state)

    /**
     * Builds a paginated GUI showing all teleport anchors except the one at [exclude].
     *
     * @param exclude The [Location] of the anchor currently being interacted with; it is omitted from the list.
     * @return The configured [PagedGui] for anchor selection.
     */
    private fun gui(exclude: Location) =
        PagedGui
            .itemsBuilder()
            .setStructure(
                "# # # # # # # # #",
                "# x x x x x x x #",
                "# x x x x x x x #",
                "# x x x x x x x #",
                "# x x x x x x x #",
                "# # # < # > # # #",
            ).addIngredient('#', Item.simple(ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).hideTooltip(true)))
            .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('<', BACK_BUTTON)
            .addIngredient('>', FORWARD_BUTTON)
            .setContent(
                state.anchors
                    .filterNot { it.matches(exclude) }
                    .map { anchorItem(it) },
            ).build()

    /**
     * Creates a window for the teleport destination selector GUI.
     *
     * @param exclude The [Location] of the anchor being interacted with.
     * @param title The title to display on the window (typically the clicked anchor's name).
     * @return The configured [Window] builder.
     */
    private fun window(
        exclude: Location,
        title: String,
    ) = Window
        .builder()
        .setTitle(MM.deserialize(title))
        .setUpperGui(gui(exclude))

    /**
     * Creates a GUI item representing a teleport anchor.
     *
     * @param anchor The [TeleportAnchorData] to represent.
     * @return The constructed [Item] for the GUI.
     */
    @Suppress("UnstableApiUsage")
    private fun anchorItem(anchor: TeleportAnchorData): Item =
        Item
            .builder()
            .setItemProvider(
                ItemStack
                    .of(Material.LODESTONE)
                    .apply {
                        setData(DataComponentTypes.ITEM_NAME, MM.deserialize(anchor.name))
                        setData(
                            DataComponentTypes.LORE,
                            ItemLore.lore(
                                listOf(
                                    Component.empty(),
                                    MM.deserialize("World: ${anchor.world.name}"),
                                    MM.deserialize(
                                        "Location: ${anchor.location.x}, ${anchor.location.y}, ${anchor.location.z}",
                                    ),
                                ),
                            ),
                        )
                    },
            ).addClickHandler { _, click ->
                handleTeleport(click.player, anchor)
            }.build()

    /**
     * Handles teleporting a player to an anchor after a 3-second countdown.
     *
     * @param player The [Player] to teleport.
     * @param anchor The [TeleportAnchorData] destination.
     */
    private fun handleTeleport(
        player: Player,
        anchor: TeleportAnchorData,
    ) {
        if (player in TELEPORTING) return

        TELEPORTING.add(player)
        player.closeInventory()

        lateinit var task: BukkitTask
        var remaining = 3

        task =
            schedule(period = 20L) {
                if (remaining > 0) {
                    playExpansionEffect(player, anchor, (3 - remaining) / 3.0f)
                    player.showTitle(
                        Title.title(
                            MM.deserialize(Messages.TELEPORT_TITLE),
                            MM.deserialize(
                                Messages.TELEPORT_SUBTITLE.replace("<remaining>", remaining.toString()),
                            ),
                        ),
                    )
                    remaining--
                } else {
                    playLightningEffect(player.location)
                    player.teleport(anchor.location)
                    playTeleportEffects(player, anchor.location)
                    playLightningEffect(anchor.location)
                    TELEPORTING.remove(player)
                    task.cancel()
                }
            }
    }

    /**
     * Plays an enderman teleport sound and portal particles at the given location.
     *
     * @param player The [Player] context for the world.
     * @param location The [Location] where the effects should play.
     */
    private fun playTeleportEffects(
        player: Player,
        location: Location,
    ) {
        player.world.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f)
        player.world.spawnParticle(
            Particle.PORTAL,
            location.clone().add(0.0, 1.0, 0.0),
            50,
            0.5,
            1.0,
            0.5,
            0.1,
        )
    }

    /**
     * Spawns purple flame-like particles above the specified anchor.
     *
     * @param anchor The [TeleportAnchorData] to spawn particles above.
     */
    private fun playAnchorFlame(anchor: TeleportAnchorData) {
        anchor.world.spawnParticle(
            Particle.DUST,
            anchor.location.clone().add(0.5, 1.2, 0.5),
            15,
            0.15,
            0.3,
            0.15,
            0.0,
            Particle.DustOptions(Color.fromRGB(147, 51, 255), 1.0f),
        )
    }

    /**
     * Spawns an expanding horizontal ring of purple particles from the anchor toward the player.
     *
     * @param player The [Player] the effect is expanding toward.
     * @param anchor The [TeleportAnchorData] center of the expansion.
     * @param progress A float from 0.0 to 1.0 representing how far the ring has expanded.
     */
    private fun playExpansionEffect(
        player: Player,
        anchor: TeleportAnchorData,
        progress: Float,
    ) {
        val center = anchor.location.clone().add(0.5, 1.0, 0.5)
        val distance = center.distance(player.location.clone().add(0.0, 1.0, 0.0))
        val radius = (distance * progress).coerceAtLeast(0.1)
        val points = (20 + 20 * progress).toInt()

        for (i in 0 until points) {
            val angle = 2 * Math.PI * i / points
            val x = center.x + radius * cos(angle)
            val z = center.z + radius * sin(angle)
            val loc = Location(anchor.world, x, center.y, z)

            anchor.world.spawnParticle(
                Particle.DUST,
                loc,
                1,
                0.0,
                0.0,
                0.0,
                0.0,
                Particle.DustOptions(Color.fromRGB(147, 51, 255), 0.8f),
            )
        }
    }

    /**
     * Strikes a visual-only lightning bolt at the given location.
     *
     * @param location The [Location] to strike lightning at.
     */
    private fun playLightningEffect(location: Location) {
        location.world.strikeLightningEffect(location)
    }

    /**
     * Renames a teleport anchor using the custom name from a name tag.
     *
     * @param event The [PlayerInteractEvent] triggered when a player interacts with a lodestone while holding a name tag.
     */
    private fun rename(event: PlayerInteractEvent) {
        val block = event.clickedBlock ?: return
        val item = event.item ?: return
        val anchor = state.anchors.firstOrNull { it.matches(block.location) } ?: return
        val index = state.anchors.indexOf(anchor)

        state.anchors[index] = anchor.name(item.getCustomName() ?: return)
        save()
        event.player.sendActionBar(MM.deserialize(Messages.ANCHOR_NAME_UPDATE))
    }
}
