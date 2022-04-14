package gg.astromc.battlebox.game.teams

import gg.astromc.battlebox.game.maps.configuration.TeamConfiguration
import gg.astromc.battlebox.game.players.PlayerContainer
import gg.astromc.battlebox.game.players.PlayerState
import gg.astromc.battlebox.game.players.reset
import gg.astromc.battlebox.game.teams.strategies.PlayerTeamAssignmentStrategy
import gg.astromc.battlebox.game.teams.strategies.TeamAssignmentStrategy

object TeamAssigner {
    fun balanceTeams(teams: List<TeamConfiguration>, players: List<PlayerContainer>) {
        val playersWhoArePlaying = players.filter { it.battleboxPlayer.playerState == PlayerState.PLAYING }
        var teamIndex = 0

        for (playerContainer in playersWhoArePlaying) {
            val teamConfiguration = if (++teamIndex < teams.size) {
                teams[teamIndex]
            } else {
                teamIndex = 0
                teams[teamIndex]
            }

            assignPlayerTo(playerContainer, PlayerTeamAssignmentStrategy(teamConfiguration))
        }
    }

    fun assignPlayerTo(playerContainer: PlayerContainer, teamAssignmentStrategy: TeamAssignmentStrategy?) {
        playerContainer.player.reset()
        playerContainer.battleboxPlayer.reset()
        teamAssignmentStrategy?.assignPlayerToTeam(playerContainer)
    }
}