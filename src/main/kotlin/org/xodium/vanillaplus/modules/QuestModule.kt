/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import eu.endercentral.crazy_advancements.NameKey
import eu.endercentral.crazy_advancements.advancement.Advancement
import eu.endercentral.crazy_advancements.advancement.AdvancementDisplay
import eu.endercentral.crazy_advancements.advancement.AdvancementVisibility
import eu.endercentral.crazy_advancements.manager.AdvancementManager
import org.bukkit.Material
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface

class QuestModule : ModuleInterface<QuestModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

    init {
        if (enabled()) {
            val advancementManager = AdvancementManager(
                NameKey(
                    instance::class.simpleName!!.lowercase(),
                    QuestModule::class.simpleName!!.lowercase()
                ),
            )

            val rootDisplay = AdvancementDisplay(
                Material.GRASS_BLOCK,
                "Quests",
                "All your quests in one place",
                AdvancementDisplay.AdvancementFrame.TASK,
                AdvancementVisibility.ALWAYS
            )

            rootDisplay.backgroundTexture = "textures/gui/advancements/backgrounds/stone.png"

            val rootAdvancement = Advancement(null, NameKey("vanillaplus", "quests/root"), rootDisplay)
            advancementManager.addAdvancement(rootAdvancement)
        }
    }

    data class Config(
        override var enabled: Boolean = true
    ) : ModuleInterface.Config
}