package org.xodium.vanillaplus.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.sound.Sound
import org.bukkit.Material
import org.bukkit.event.inventory.ClickType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.utils.ExtUtils.clickOpenUrl
import org.xodium.vanillaplus.utils.ExtUtils.clickRunCmd
import org.xodium.vanillaplus.utils.ExtUtils.clickSuggestCmd
import org.xodium.vanillaplus.utils.ExtUtils.prefix

/** Configuration data for the plugin. */
@Serializable
internal data class ConfigData(
    @SerialName("BooksModule")
    var booksModule: BooksModule = BooksModule(),
    @SerialName("CauldronModule")
    var cauldronModule: CauldronModule = CauldronModule(),
    @SerialName("ChatModule")
    var chatModule: ChatModule = ChatModule(),
    @SerialName("DimensionsModule")
    var dimensionsModule: DimensionsModule = DimensionsModule(),
    @SerialName("EntityModule")
    var entityModule: EntityModule = EntityModule(),
    @SerialName("InvModule")
    var invModule: InvModule = InvModule(),
    @SerialName("MotdModule")
    var motdModule: MotdModule = MotdModule(),
    @SerialName("OpenableModule")
    var openableModule: OpenableModule = OpenableModule(),
    @SerialName("PetModule")
    var petModule: PetModule = PetModule(),
    @SerialName("PlayerModule")
    var playerModule: PlayerModule = PlayerModule(),
    @SerialName("SitModule")
    var sitModule: SitModule = SitModule(),
    @SerialName("TabListModule")
    var tabListModule: TabListModule = TabListModule(),
    @SerialName("TreesModule")
    var treesModule: TreesModule = TreesModule(),
) {
    @Serializable
    data class BooksModule(
        var books: List<BookData> =
            listOf(
                BookData(
                    cmd = "rules",
                    pages =
                        listOf(
                            // Page 1: Player Rules (1-7)
                            listOf(
                                "<b><u><dark_aqua>Player Rules:<reset>",
                                "",
                                "<gold>▶ <dark_aqua>01 <dark_gray>| <red>No Griefing",
                                "<gold>▶ <dark_aqua>02 <dark_gray>| <red>No Spamming",
                                "<gold>▶ <dark_aqua>03 <dark_gray>| <red>No Advertising",
                                "<gold>▶ <dark_aqua>04 <dark_gray>| <red>No Cursing/No Constant Cursing",
                                "<gold>▶ <dark_aqua>05 <dark_gray>| <red>No Trolling/Flaming",
                                "<gold>▶ <dark_aqua>06 <dark_gray>| <red>No Asking for OP, Ranks, or Items",
                                "<gold>▶ <dark_aqua>07 <dark_gray>| <red>Respect all Players",
                            ),
                            // Page 2: Player Rules (8-13)
                            listOf(
                                "<gold>▶ <dark_aqua>08 <dark_gray>| <red>Obey Staff they are the Law Enforcers",
                                "<gold>▶ <dark_aqua>09 <dark_gray>| <red>No Racist or Sexist Remarks",
                                "<gold>▶ <dark_aqua>10 <dark_gray>| <red>No Mods/Hacks",
                                "<gold>▶ <dark_aqua>12 <dark_gray>| <red>No 1x1 Towers",
                                "<gold>▶ <dark_aqua>13 <dark_gray>| <red>Build in (Fantasy)Medieval style",
                            ),
                            // Page 3: Mod/Admin Rules
                            listOf(
                                "<b><u><dark_aqua>Mod/Admin Rules:<reset>",
                                "",
                                "<gold>▶ <dark_aqua>01 <dark_gray>| <red>Be Responsible with the power you are given as staff",
                                "<gold>▶ <dark_aqua>02 <dark_gray>| <red>Do not spawn blocks or items for other players",
                                "<gold>▶ <dark_aqua>03 <dark_gray>| <red>When Trading, only buy and sell legit items",
                                "<gold>▶ <dark_aqua>05 <dark_gray>| <red>No Power Abuse",
                            ),
                        ),
                ),
            ),
    )

    @Serializable
    data class CauldronModule(
        var convertConcretePowder: Boolean = true,
        var convertDirt: Boolean = true,
        var convertCoarseDirt: Boolean = true,
        var convertRootedDirt: Boolean = true,
    )

    @Serializable
    data class ChatModule(
        var chatFormat: String = "<player_head> <player> <reset><gradient:#FFE259:#FFA751>›</gradient> <message>",
        var welcomeText: List<String> =
            listOf(
                "<gradient:#FFA751:#FFE259>]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[</gradient>",
                "<image><gradient:#FFE259:#FFA751>⯈</gradient>",
                "<image><gradient:#FFE259:#FFA751>⯈</gradient>",
                "<image><gradient:#FFE259:#FFA751>⯈</gradient> <gradient:#CB2D3E:#EF473A>Welcome</gradient> <player> <white><sprite:item/name_tag></white>"
                    .clickSuggestCmd(
                        "/nickname",
                        "<gradient:#FFE259:#FFA751>Set your nickname!</gradient>",
                    ),
                "<image><gradient:#FFE259:#FFA751>⯈</gradient>",
                "<image><gradient:#FFE259:#FFA751>⯈</gradient> <gradient:#CB2D3E:#EF473A>Check out</gradient><gray>:",
                "<image><gradient:#FFE259:#FFA751>⯈</gradient> <white><sprite:item/writable_book></white>".clickRunCmd(
                    "/rules",
                    "<gradient:#FFE259:#FFA751>View the server /rules</gradient>",
                ),
                "<image><gradient:#FFE259:#FFA751>⯈</gradient> <white><sprite:item/light></white>".clickOpenUrl(
                    "https://illyria.fandom.com",
                    "<gradient:#FFE259:#FFA751>Visit the wiki!</gradient>",
                ),
                "<image><gradient:#FFE259:#FFA751>⯈</gradient>",
                "<gradient:#FFA751:#FFE259>]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[</gradient>",
            ),
        var whisperToFormat: String =
            "<gradient:#1488CC:#2B32B2>You</gradient> <gradient:#FFE259:#FFA751>➛</gradient> <player> <reset><gradient:#FFE259:#FFA751>›</gradient> <message>",
        var whisperFromFormat: String =
            "<player> <reset><gradient:#FFE259:#FFA751>➛</gradient> <gradient:#1488CC:#2B32B2>You</gradient> <gradient:#FFE259:#FFA751>›</gradient> <message>",
        var deleteCross: String = "<dark_gray>[<dark_red><b>X</b></dark_red><dark_gray>]",
        var i18n: I18n = I18n(),
    ) {
        @Serializable
        data class I18n(
            var clickMe: String = "<gradient:#FFE259:#FFA751>Click me!</gradient>",
            var clickToWhisper: String = "<gradient:#FFE259:#FFA751>Click to Whisper</gradient>",
            var playerIsNotOnline: String = "${instance.prefix} <gradient:#CB2D3E:#EF473A>Player is not Online!</gradient>",
            var deleteMessage: String = "<gradient:#FFE259:#FFA751>Click to delete your message</gradient>",
            var clickToClipboard: String = "<gradient:#FFE259:#FFA751>Click to copy position to clipboard</gradient>",
            var playerSetSpawn: String = "<gradient:#CB2D3E:#EF473A>❗</gradient> <gradient:#FFE259:#FFA751>›</gradient> <notification>",
        )
    }

    @Serializable
    data class DimensionsModule(
        var portalSearchRadius: Int = 128,
        var i18n: I18n = I18n(),
    ) {
        @Serializable
        data class I18n(
            var portalCreationDenied: String =
                "<gradient:#CB2D3E:#EF473A>No corresponding active portal found in the Overworld!</gradient>",
        )
    }

    @Serializable
    data class EntityModule(
        var disableBlazeGrief: Boolean = true,
        var disableCreeperGrief: Boolean = true,
        var disableEnderDragonGrief: Boolean = true,
        var disableEndermanGrief: Boolean = true,
        var disableGhastGrief: Boolean = true,
        var disableWitherGrief: Boolean = true,
        var entityEggDropChance: Double = 0.1,
    )

    @Serializable
    data class InvModule(
        var soundOnUnload: SoundData = SoundData("entity.player.levelup", Sound.Source.PLAYER),
        var i18n: I18n = I18n(),
    ) {
        @Serializable
        data class I18n(
            var noMaterialSpecified: String =
                "<gradient:#CB2D3E:#EF473A>You must specify a valid material " +
                    "or hold something in your hand</gradient>",
            var noMatchingItems: String =
                "<gradient:#CB2D3E:#EF473A>No containers contain " +
                    "<gradient:#F4C4F3:#FC67FA><b><material></b></gradient></gradient>",
            var foundItemsInChests: String =
                "<gradient:#FFE259:#FFA751>Found <gradient:#F4C4F3:#FC67FA><b><material></b></gradient> in container(s), follow trail(s)</gradient>",
            var noItemsUnloaded: String = "<gradient:#CB2D3E:#EF473A>No items were unloaded</gradient>",
            var inventoryUnloaded: String = "<gradient:#B3E94A:#54F47F>Inventory unloaded</gradient>",
        )
    }

    @Serializable
    data class MotdModule(
        val motd: List<String> =
            listOf(
                "<gradient:#CB2D3E:#EF473A><b>Ultimate Private SMP</b></gradient>",
                "<gradient:#FFE259:#FFA751><b>➤ WELCOME BACK LADS!</b></gradient>",
            ),
    )

    @Serializable
    data class OpenableModule(
        var initDelayInTicks: Long = 1,
        var allowDoubleDoors: Boolean = true,
        var allowKnocking: Boolean = true,
        var knockingRequiresEmptyHand: Boolean = true,
        var knockingRequiresShifting: Boolean = true,
        var soundKnock: SoundData = SoundData("entity.zombie.attack_wooden_door", Sound.Source.HOSTILE),
        var soundProximityRadius: Double = 10.0,
    )

    @Serializable
    data class PetModule(
        var transferRadius: Int = 10,
        var i18n: I18n = I18n(),
    ) {
        @Serializable
        data class I18n(
            var sourceTransfer: String =
                "<gradient:#CB2D3E:#EF473A>You have transferred</gradient> <pet> " +
                    "<gradient:#CB2D3E:#EF473A>to</gradient> <target>",
            var targetTransfer: String =
                "<source> <gradient:#CB2D3E:#EF473A>has transferred</gradient> <pet> " +
                    "<gradient:#CB2D3E:#EF473A>to you</gradient>",
        )
    }

    @Serializable
    data class PlayerModule(
        var enderChestClickType: ClickType = ClickType.SHIFT_RIGHT,
        var skullDropChance: Double = 0.1,
        var xpCostToBottle: Int = 11,
        var silkTouch: SilkTouchEnchantment = SilkTouchEnchantment(),
        var i18n: I18n = I18n(),
    ) {
        @Serializable
        data class SilkTouchEnchantment(
            var allowSpawnerSilk: Boolean = true,
            var allowBuddingAmethystSilk: Boolean = true,
        )

        @Serializable
        data class I18n(
            var playerHeadName: String = "<player>’s Skull",
            var playerHeadLore: List<String> = listOf("<player> killed by <killer>"),
//          var playerDeathMsg: String = "<killer> <gradient:#FFE259:#FFA751>⚔</gradient> <player>",
            var playerJoinMsg: String = "<green>➕<reset> <gradient:#FFE259:#FFA751>›</gradient> <player>",
            var playerQuitMsg: String = "<red>➖<reset> <gradient:#FFE259:#FFA751>›</gradient> <player>",
            var playerDeathMsg: String = "☠ <gradient:#FFE259:#FFA751>›</gradient>",
            var playerDeathScreenMsg: String = "☠",
            var playerAdvancementDoneMsg: String =
                "\uD83C\uDF89 <gradient:#FFE259:#FFA751>›</gradient> <player> " +
                    "<gradient:#FFE259:#FFA751>has made the advancement:</gradient> <advancement>",
            var nicknameUpdated: String = "<gradient:#CB2D3E:#EF473A>Nickname has been updated to: <nickname></gradient>",
        )
    }

    @Serializable
    data class SitModule(
        var useStairs: Boolean = true,
        var useSlabs: Boolean = true,
    )

    @Serializable
    data class TabListModule(
        var initDelayInTicks: Long = 0,
        var intervalInTicks: Long = 10,
        var header: List<String> =
            listOf(
                "<gradient:#FFE259:#FFA751>]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[</gradient>   <gradient:#CB2D3E:#EF473A>⚡ IllyriaRPG ⚡</gradient>   <gradient:#FFA751:#FFE259>]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[</gradient>",
                "",
            ),
        var footer: List<String> =
            listOf(
                "",
                "<gradient:#FFE259:#FFA751>]|[=]|[=]|[=]|[=]|[=]|[=]|[</gradient>  <gradient:#CB2D3E:#EF473A>TPS:</gradient> <tps> <gradient:#FFE259:#FFA751>|</gradient> <gradient:#CB2D3E:#EF473A>Weather:</gradient> <weather>  <gradient:#FFA751:#FFE259>]|[=]|[=]|[=]|[=]|[=]|[=]|[</gradient>",
            ),
        var i18n: I18n = I18n(),
    ) {
        @Serializable
        data class I18n(
            var weatherThundering: String = "<red>\uD83C\uDF29<reset>",
            var weatherStorm: String = "<yellow>\uD83C\uDF26<reset>",
            var weatherClear: String = "<green>\uD83C\uDF24<reset>",
        )
    }

    @Serializable
    data class TreesModule(
        var copyBiomes: Boolean = false,
        var copyEntities: Boolean = false,
        var ignoreAirBlocks: Boolean = true,
        var ignoreStructureVoidBlocks: Boolean = true,
        var treeMask: Set<Material> =
            setOf(
                Material.AZALEA,
                Material.WEEPING_VINES,
                Material.CORNFLOWER,
                Material.CLOSED_EYEBLOSSOM,
                Material.PINK_TULIP,
                Material.OPEN_EYEBLOSSOM,
                Material.WHITE_TULIP,
                Material.SNOW,
                Material.FERN,
                Material.AZALEA_LEAVES,
                Material.SUNFLOWER,
                Material.PEONY,
                Material.PINK_PETALS,
                Material.LILAC,
                Material.LARGE_FERN,
                Material.VINE,
                Material.CAVE_VINES_PLANT,
                Material.TORCHFLOWER,
                Material.RED_TULIP,
                Material.ORANGE_TULIP,
                Material.KELP,
                Material.AIR,
                Material.FLOWERING_AZALEA,
                Material.AZURE_BLUET,
                Material.MOSS_BLOCK,
                Material.PITCHER_PLANT,
                Material.WEEPING_VINES_PLANT,
                Material.TALL_SEAGRASS,
                Material.TWISTING_VINES,
                Material.BLUE_ORCHID,
                Material.CAVE_VINES,
                Material.ROSE_BUSH,
                Material.SPORE_BLOSSOM,
                Material.FLOWERING_AZALEA_LEAVES,
                Material.POPPY,
                Material.TWISTING_VINES_PLANT,
                Material.DANDELION,
                Material.DEAD_BUSH,
                Material.LILY_OF_THE_VALLEY,
                Material.KELP_PLANT,
                Material.SHORT_GRASS,
                Material.CHORUS_FLOWER,
                Material.ALLIUM,
                Material.MANGROVE_PROPAGULE,
                Material.CHERRY_LEAVES,
                Material.SUGAR_CANE,
                Material.SEAGRASS,
                Material.MOSS_CARPET,
                Material.WITHER_ROSE,
                Material.TALL_GRASS,
                Material.OXEYE_DAISY,
            ),
    )
}
