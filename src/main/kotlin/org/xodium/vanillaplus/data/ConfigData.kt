@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.data

import kotlinx.serialization.Serializable
import org.xodium.vanillaplus.modules.*

/** Configuration data for the plugin. */
@Serializable
internal data class ConfigData(
    var booksModule: BooksModule.Config = BooksModule.Config(),
    var chatModule: ChatModule.Config = ChatModule.Config(),
    var dimensionsModule: DimensionsModule.Config = DimensionsModule.Config(),
    var entityModule: EntityModule.Config = EntityModule.Config(),
    var inventoryModule: InventoryModule.Config = InventoryModule.Config(),
    var locatorModule: LocatorModule.Config = LocatorModule.Config(),
    var motdModule: MotdModule.Config = MotdModule.Config(),
    var openableModule: OpenableModule.Config = OpenableModule.Config(),
    var playerModule: PlayerModule.Config = PlayerModule.Config(),
    var scoreboardModule: ScoreBoardModule.Config = ScoreBoardModule.Config(),
    var signModule: SignModule.Config = SignModule.Config(),
    var sitModule: SitModule.Config = SitModule.Config(),
    var tabListModule: TabListModule.Config = TabListModule.Config(),
    var tameableModule: TameableModule.Config = TameableModule.Config(),
    var treesModule: TreesModule.Config = TreesModule.Config(),
)
