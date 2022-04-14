package gg.astromc.battlebox.game.teams.strategies

import gg.astromc.battlebox.game.players.PlayerContainer
import gg.astromc.battlebox.game.players.PlayerState
import gg.astromc.battlebox.game.teams.BattleboxTeamContainer
import gg.astromc.gamelib.Team
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.entity.GameMode
import net.minestom.server.network.packet.server.play.TeamsPacket
import world.cepi.kstom.adventure.asMini

object NoTeamAssignmentStrategy : TeamAssignmentStrategy {
    private val noTeamContainer = BattleboxTeamContainer(
        teamConfiguration = null,
        team = Team(
            teamName = "NO-TEAM",
            color = NamedTextColor.GRAY,
            collisionRule = TeamsPacket.CollisionRule.NEVER,
            nameTagVisibility = TeamsPacket.NameTagVisibility.ALWAYS,
            friendlyFire = false,
            canSeeInvisiblePlayers = false
        )
    )

    override fun assignPlayerToTeam(playerContainer: PlayerContainer) {
        val player = playerContainer.player
        val battleboxPlayer = playerContainer.battleboxPlayer
        battleboxPlayer.playerState = PlayerState.PLAYING

        player.gameMode = GameMode.ADVENTURE
        player.isInvisible = false

        noTeamContainer.team.scoreboardTeam.prefix = "<gray>NO<dark_gray>-<gray>TEAM <dark_gray>| ".asMini()

        player.team = noTeamContainer.team.scoreboardTeam
        battleboxPlayer.teamContainer = noTeamContainer
    }
}