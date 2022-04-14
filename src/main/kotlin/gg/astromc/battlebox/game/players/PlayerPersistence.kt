package gg.astromc.battlebox.game.players

import gg.astromc.battlebox.game.teams.TeamAssigner
import gg.astromc.battlebox.game.teams.strategies.PlayerTeamAssignmentStrategy
import gg.astromc.battlebox.game.teams.strategies.SpectatorTeamAssignmentStrategy
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player

// TODO: Determine how I would let the user select a kit, and where i would teleport them, and what gamemode they should have!
object PlayerPersistence {
    fun persist(player: Player, callback: (PlayerContainer) -> Unit) {
        val playerContainer = BattleboxGamePlayers.get(player.uuid) ?: run {
            val newContainer = PlayerContainer(
                player = player,
                battleboxPlayer = BattleboxPlayer(identifier = player.uuid)
            )

            BattleboxGamePlayers.insert(player.uuid, newContainer)
            TeamAssigner.assignPlayerTo(newContainer, SpectatorTeamAssignmentStrategy)
            callback(newContainer)
            return
        }

        val battleboxPlayer = playerContainer.battleboxPlayer

        if (battleboxPlayer.playerState == PlayerState.SPECTATING) {
            TeamAssigner.assignPlayerTo(playerContainer, SpectatorTeamAssignmentStrategy)
        } else if (battleboxPlayer.playerState == PlayerState.PLAYING) {
            val teamAssignmentStrategy = battleboxPlayer.teamContainer?.teamConfiguration?.let { PlayerTeamAssignmentStrategy(it) }
            TeamAssigner.assignPlayerTo(playerContainer, teamAssignmentStrategy)
        } else {
            battleboxPlayer.playerState = PlayerState.SPECTATING
            TeamAssigner.assignPlayerTo(playerContainer, SpectatorTeamAssignmentStrategy)
        }

        callback(playerContainer)
    }
}