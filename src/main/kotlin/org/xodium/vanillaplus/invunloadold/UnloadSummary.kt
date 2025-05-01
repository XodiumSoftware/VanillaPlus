/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.invunloadold

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.xodium.vanillaplus.utils.ExtUtils.mm

class UnloadSummary internal constructor() {
    private val unloads: MutableMap<Location, MutableMap<Material, Int>> = mutableMapOf()

    fun protocolUnload(loc: Location, mat: Material, amount: Int) {
        if (amount == 0) return
        val materialMap = unloads.getOrPut(loc) { mutableMapOf() }
        materialMap[mat] = materialMap.getOrDefault(mat, 0) + amount
    }

    private fun loc2str(loc: Location): Component {
        val x = loc.blockX
        val y = loc.blockY
        val z = loc.blockZ
        var name = loc.block.type.name
        val state = loc.world.getBlockAt(x, y, z).state
        if (state is Container && state.customName() != null) {
            name = state.customName().toString()
        }
        return """
            <light_purple><b>$name</b>   
            <green><b>X:</b></green> <white>$x</white> 
            <green><b>Y:</b></green> <white>$y</white> 
            <green><b>Z:</b></green> <white>$z</white>
        """.trimIndent().mm()
    }

    private fun amount2str(amount: Int): Component {
        return "<dark_purple>|</dark_purple><gray>${"%5d".format(amount)}x  </gray>".mm()
    }

    fun print(player: Player) {
        if (unloads.isNotEmpty()) player.sendMessage("<gray><b>Unload Summary:</b></gray>".mm())
        unloads.forEach { (loc, materials) ->
            player.sendMessage("<gray>--------------------</gray>".mm())
            player.sendMessage(loc2str(loc))
            materials.forEach { (mat, amount) ->
                player.sendMessage(
                    Component.join(
                        JoinConfiguration.noSeparators(),
                        amount2str(amount),
                        "<gold>${mat.name}</gold>".mm()
                    )
                )
            }
        }
    }
}
