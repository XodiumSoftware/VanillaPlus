@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.goals

import com.destroystokyo.paper.entity.ai.Goal
import com.destroystokyo.paper.entity.ai.GoalKey
import com.destroystokyo.paper.entity.ai.GoalType
import org.bukkit.NamespacedKey
import org.bukkit.entity.Zombie
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.FormationMemberData
import org.xodium.vanillaplus.managers.FormationManager
import java.util.*

/** Goal that drives a horde formation from the Warlord's perspective each tick. */
internal class FormationLeaderGoal(
    private val warlord: Zombie,
    private val formation: MutableList<FormationMemberData>,
) : Goal<Zombie> {
    override fun getKey(): GoalKey<Zombie> = GoalKey.of(Zombie::class.java, NamespacedKey(instance, "formation_leader"))

    override fun getTypes(): EnumSet<GoalType> = EnumSet.of(GoalType.MOVE, GoalType.LOOK, GoalType.TARGET)

    override fun shouldActivate(): Boolean = !warlord.isDead && warlord.isValid

    override fun tick() = FormationManager.tick(warlord, formation)
}
