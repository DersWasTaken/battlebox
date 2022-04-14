package gg.astromc.battlebox.game.teams.strategies

import gg.astromc.battlebox.game.teams.BattleboxTeamContainer
import gg.astromc.battlebox.game.players.PlayerContainer
import gg.astromc.battlebox.game.players.PlayerState
import gg.astromc.battlebox.game.players.refreshPlayer
import gg.astromc.gamelib.Team
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.entity.GameMode
import net.minestom.server.network.packet.server.play.TeamsPacket
import world.cepi.kstom.Manager
import world.cepi.kstom.adventure.asMini

object SpectatorTeamAssignmentStrategy: TeamAssignmentStrategy {
    private val spectatorTeamContainer = BattleboxTeamContainer(
        teamConfiguration = null,
        team = Team(
            teamName = "SPECTATOR",
            color = NamedTextColor.GRAY,
            collisionRule = TeamsPacket.CollisionRule.NEVER,
            nameTagVisibility = TeamsPacket.NameTagVisibility.NEVER,
            friendlyFire = false,
            canSeeInvisiblePlayers = false
        )
    )

    override fun assignPlayerToTeam(playerContainer: PlayerContainer) {
        val player = playerContainer.player
        val battleboxPlayer = playerContainer.battleboxPlayer
        battleboxPlayer.playerState = PlayerState.SPECTATING

        playerContainer.refreshPlayer()

        player.gameMode = GameMode.SPECTATOR
        player.isInvisible = true

        spectatorTeamContainer.team.scoreboardTeam.prefix = "<gray>SPECTATOR <dark_gray>| ".asMini()

        player.team = spectatorTeamContainer.team.scoreboardTeam
        battleboxPlayer.teamContainer = spectatorTeamContainer
    }
}