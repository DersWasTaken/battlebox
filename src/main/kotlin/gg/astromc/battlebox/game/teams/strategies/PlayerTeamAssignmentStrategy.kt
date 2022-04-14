package gg.astromc.battlebox.game.teams.strategies

import gg.astromc.battlebox.game.maps.configuration.TeamConfiguration
import gg.astromc.battlebox.game.players.BattleboxGamePlayers
import gg.astromc.battlebox.game.players.PlayerContainer
import gg.astromc.battlebox.game.players.PlayerState
import gg.astromc.battlebox.game.teams.BattleboxTeamContainer
import gg.astromc.battlebox.game.teams.TeamCache
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.network.packet.server.play.ScoreboardObjectivePacket
import net.minestom.server.scoreboard.BelowNameTag
import net.minestom.server.scoreboard.TabList
import world.cepi.kstom.Manager
import world.cepi.kstom.adventure.asMini

val healthSidebar = BelowNameTag("HEALTH", Component.text("❤", NamedTextColor.DARK_RED))

//TODO: Use Player Statistics?
val playerKills = mutableMapOf<Player, Int>()

class PlayerTeamAssignmentStrategy(private val teamConfiguration: TeamConfiguration): TeamAssignmentStrategy {
    override fun assignPlayerToTeam(playerContainer: PlayerContainer) {
        val player = playerContainer.player
        player.isInvisible = false

        val battleboxPlayer = playerContainer.battleboxPlayer
        battleboxPlayer.playerState = PlayerState.PLAYING

        player.gameMode = GameMode.ADVENTURE

        val team = TeamCache.getTeam(teamConfiguration)

        player.team = team.scoreboardTeam

        healthSidebar.addViewer(player)
        healthSidebar.updateScore(player, 20)

        playerKills[player] = 0

        player.displayName = Component.text(team.teamName.toUpperCase(), TextColor.color(team.color))
            .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
            .append(Component.text(player.username, TextColor.color(team.color)))
            .append("<dark_gray> |<yellow> Kills: ${playerKills[player]!!}".asMini())

        battleboxPlayer.teamContainer = BattleboxTeamContainer(
            team = team,
            teamConfiguration = teamConfiguration
        )
    }
}