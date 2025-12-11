@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.data

import kotlinx.serialization.Serializable
import org.xodium.vanillaplus.modules.*

/** Configuration data for the plugin. */
@Serializable
internal data class ConfigData(
    var arrowModule: ArrowModule.Config = ArrowModule.Config(),
    var booksModule: BooksModule.Config = BooksModule.Config(),
    var chatModule: ChatModule.Config = ChatModule.Config(),
    var dimensionsModule: DimensionsModule.Config = DimensionsModule.Config(),
    var entityModule: EntityModule.Config = EntityModule.Config(),
    var invModule: InvModule.Config = InvModule.Config(),
    var locatorModule: LocatorModule.Config = LocatorModule.Config(),
    var motdModule: MotdModule.Config = MotdModule.Config(),
    var openableModule: OpenableModule.Config = OpenableModule.Config(),
    var petModule: PetModule.Config = PetModule.Config(),
    var playerModule: PlayerModule.Config = PlayerModule.Config(),
    var scoreboardModule: ScoreBoardModule.Config = ScoreBoardModule.Config(),
    var signModule: SignModule.Config = SignModule.Config(),
    var sitModule: SitModule.Config = SitModule.Config(),
    var tabListModule: TabListModule.Config = TabListModule.Config(),
    var treesModule: TreesModule.Config = TreesModule.Config(),
)
