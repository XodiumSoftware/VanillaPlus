package org.xodium.vanillaplus.modules

import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.recipies.RottenFleshRecipe

/** Represents a module handling recipe mechanics within the system. */
internal class RecipeModule : ModuleInterface<RecipeModule.Config> {
    override val config: Config = Config()

    init {
        if (enabled()) {
            if (config.rottenFleshToLeatherEnabled) RottenFleshRecipe.register()
        }
    }

    data class Config(
        override var enabled: Boolean = true,
        var rottenFleshToLeatherEnabled: Boolean = true,
    ) : ModuleInterface.Config
}
