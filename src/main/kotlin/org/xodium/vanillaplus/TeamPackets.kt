package org.xodium.vanillaplus

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.scoreboard.NameTagVisibility
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.xodium.vanillaplus.modules.TeamConfig


object TeamPackets {
    private val LOGGER: Logger? = LoggerFactory.getLogger(TeamPackets::class.java)

    fun createTeam(player: Player?, team: TeamConfig) {
        sendTeamPacket(player, team, WrapperPlayServerTeams.TeamMode.CREATE)
    }

    fun sendTeam(player: Player?, team: TeamConfig) {
        sendTeamPacket(player, team, WrapperPlayServerTeams.TeamMode.UPDATE)
    }

    fun removeTeam(player: Player?, team: TeamConfig) {
        sendTeamPacket(player, team, WrapperPlayServerTeams.TeamMode.REMOVE)
    }

    fun addPlayerToTeam(player: Player?, team: TeamConfig, playerName: String?) {
        sendEntityPacket(player, team, playerName, WrapperPlayServerTeams.TeamMode.ADD_ENTITIES)
    }

    fun removePlayerFromTeam(player: Player?, team: TeamConfig, playerName: String?) {
        sendEntityPacket(player, team, playerName, WrapperPlayServerTeams.TeamMode.REMOVE_ENTITIES)
    }

    fun addPlayersToTeam(player: Player?, team: TeamConfig, playerNames: Array<String?>?) {
        sendEntitiesPacket(player, team, playerNames, WrapperPlayServerTeams.TeamMode.ADD_ENTITIES)
    }

    fun removePlayersFromTeam(player: Player?, team: TeamConfig, playerNames: Array<String?>?) {
        sendEntitiesPacket(player, team, playerNames, WrapperPlayServerTeams.TeamMode.REMOVE_ENTITIES)
    }

    private fun sendTeamPacket(player: Player?, team: TeamConfig, mode: WrapperPlayServerTeams.TeamMode?) {
        val displayName: Component? = MessageUtils.toRgb(team.tag)
        val prefix: Component? = MessageUtils.toRgb(team.prefix)
        val suffix: Component? = MessageUtils.toRgb(team.suffix)
        val visibility: NameTagVisibility? = WrapperPlayServerTeams.NameTagVisibility.NEVER
        val teamInfo: WrapperPlayServerTeams.ScoreBoardTeamInfo = ScoreBoardTeamInfo(
            displayName, prefix, suffix, visibility,
            WrapperPlayServerTeams.CollisionRule.NEVER, null,
            WrapperPlayServerTeams.OptionData.FRIENDLY_FIRE
        )
        val packet: WrapperPlayServerTeams = WrapperPlayServerTeams(team.name, mode, teamInfo)
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet)
    }

    private fun sendEntitiesPacket(
        player: Player?,
        team: TeamConfig,
        playerNames: Array<String?>?,
        mode: WrapperPlayServerTeams.TeamMode?
    ) {
        val packet: WrapperPlayServerTeams =
            WrapperPlayServerTeams(team.name, mode, null as WrapperPlayServerTeams.ScoreBoardTeamInfo?, playerNames)
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet)
    }

    private fun sendEntityPacket(
        player: Player?,
        team: TeamConfig,
        playerName: String?,
        mode: WrapperPlayServerTeams.TeamMode?
    ) {
        val packet: WrapperPlayServerTeams =
            WrapperPlayServerTeams(team.name, mode, null as WrapperPlayServerTeams.ScoreBoardTeamInfo?, playerName)
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet)
    }

    fun sendPlayerInfoUpdates(viewer: Player?, config: TeamConfig, userProfiles: MutableCollection<UserProfile?>) {
        val playerInfos: Array<WrapperPlayServerPlayerInfoUpdate.PlayerInfo?> =
            userProfiles.stream().map<Any?> { userProfile: UserProfile? ->
                val playerInfo: WrapperPlayServerPlayerInfoUpdate.PlayerInfo = PlayerInfo(userProfile.getUUID())
                playerInfo.setDisplayName(MessageUtils.toRgb(config.prefix + config.tag + userProfile.getName() + config.suffix))
                playerInfo
            }.toArray<WrapperPlayServerPlayerInfoUpdate.PlayerInfo?> { _Dummy_.__Array__() }
        val packet: WrapperPlayServerPlayerInfoUpdate =
            WrapperPlayServerPlayerInfoUpdate(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME, playerInfos)

        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet)
    }

    fun sendPlayerInfoUpdate(viewers: MutableCollection<out Player?>, config: TeamConfig, target: Player) {
        val playerInfo: WrapperPlayServerPlayerInfoUpdate.PlayerInfo = PlayerInfo(target.uniqueId)
        playerInfo.setDisplayName(MessageUtils.toRgb(config.prefix + config.tag + target.name + config.suffix))

        val packet: WrapperPlayServerPlayerInfoUpdate =
            WrapperPlayServerPlayerInfoUpdate(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME, playerInfo)
        val playerManager: Unit /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */? =
            PacketEvents.getAPI().getPlayerManager()

        viewers.forEach { viewer: Player? ->
            playerManager.sendPacket(viewer, packet)
        }
    }
}


