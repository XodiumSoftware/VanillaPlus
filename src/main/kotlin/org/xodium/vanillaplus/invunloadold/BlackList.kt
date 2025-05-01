/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.invunloadold

import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player

class BlackList {
    val mats: MutableList<Material>

    internal constructor(strings: MutableList<String>) {
        mats = ArrayList<Material>()
        for (s in strings) {
            val mat = Material.getMaterial(s)
            if (mat != null) mats.add(mat)
        }
    }

    internal constructor() {
        mats = ArrayList<Material>()
    }

    fun add(string: String) {
        val mat = Material.getMaterial(string)
        if (mat != null) {
            if (!mats.contains(mat)) {
                mats.add(mat)
            }
        }
    }

    fun add(mat: Material?) {
        mats.add(mat!!)
    }

    fun contains(mat: Material?): Boolean {
        return mats.contains(mat)
    }

    fun remove(mat: Material?) {
        if (mats.contains(mat)) mats.remove(mat)
    }

    fun toStringList(): MutableList<String?> {
        val list = ArrayList<String?>()

        for (mat in mats) {
            if (!list.contains(mat.name)) {
                list.add(mat.name)
            }
        }
        return list
    }

    fun print(p: Player, main: Main) {
        if (mats.size == 0) {
            p.sendMessage(main.messages.BL_EMPTY)
        }

        p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("blacklist-title")!!))

        /*p.sendMessage("This list will be nicer in the next version :P");
        p.sendMessage("Blacklist: ");
        StringBuilder slist = new StringBuilder();
        */
        for (mat in mats) {
            val text = TextComponent("")
            val link = createLink("[X] ", "/blacklist remove " + mat.name)
            val name = TextComponent(mat.name)
            name.color = net.md_5.bungee.api.ChatColor.GRAY
            text.addExtra(link)
            text.addExtra(name)
            p.spigot().sendMessage(text)
        }
    }

    private fun createLink(text: String?, link: String?): TextComponent {
        val tc = TextComponent(text)
        tc.isBold = true
        // TODO: Make color configurable
        tc.color = net.md_5.bungee.api.ChatColor.DARK_RED
        tc.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, link)
        return tc
    }
}
