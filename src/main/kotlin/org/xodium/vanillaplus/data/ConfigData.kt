@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.xodium.vanillaplus.modules.*

/** Configuration data for the plugin. */
@Serializable
internal data class ConfigData(
    @SerialName("ArrowModule")
    var arrowModule: ArrowModule.Config = ArrowModule.Config(),
    @SerialName("BooksModule")
    var booksModule: BooksModule.Config = BooksModule.Config(),
    @SerialName("ChatModule")
    var chatModule: ChatModule.Config = ChatModule.Config(),
    @SerialName("DimensionsModule")
    var dimensionsModule: DimensionsModule.Config = DimensionsModule.Config(),
    @SerialName("EntityModule")
    var entityModule: EntityModule.Config = EntityModule.Config(),
    @SerialName("InvModule")
    var invModule: InvModule.Config = InvModule.Config(),
    @SerialName("LocatorModule")
    var locatorModule: LocatorModule.Config = LocatorModule.Config(),
    @SerialName("MotdModule")
    var motdModule: MotdModule.Config = MotdModule.Config(),
    @SerialName("OpenableModule")
    var openableModule: OpenableModule.Config = OpenableModule.Config(),
    @SerialName("PetModule")
    var petModule: PetModule.Config = PetModule.Config(),
    @SerialName("PlayerModule")
    var playerModule: PlayerModule.Config = PlayerModule.Config(),
    @SerialName("ScoreBoardModule")
    var scoreboardModule: ScoreBoardModule.Config = ScoreBoardModule.Config(),
    @SerialName("SignModule")
    var signModule: SignModule.Config = SignModule.Config(),
    @SerialName("SitModule")
    var sitModule: SitModule.Config = SitModule.Config(),
    @SerialName("TabListModule")
    var tabListModule: TabListModule.Config = TabListModule.Config(),
    @SerialName("TreesModule")
    var treesModule: TreesModule.Config = TreesModule.Config(),
)
