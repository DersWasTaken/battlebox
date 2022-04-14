package gg.astromc.battlebox.game.teams.strategies

import gg.astromc.battlebox.game.players.PlayerContainer

interface TeamAssignmentStrategy {
    fun assignPlayerToTeam(playerContainer: PlayerContainer)
}