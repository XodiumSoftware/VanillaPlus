/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.hooks.ChestSortHook
import org.xodium.vanillaplus.interfaces.ModuleInterface

/**
 * TODO
 */
class InvUnloadModule : ModuleInterface {
    override fun enabled(): Boolean = Config.InvUnloadModule.ENABLED

    init {
        if (enabled()) {
            ChestSortHook.sort(TODO())
            ChestSortHook.shouldSort(TODO())
        }
    }
}