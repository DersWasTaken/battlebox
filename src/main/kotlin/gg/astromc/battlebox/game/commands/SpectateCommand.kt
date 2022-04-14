package gg.astromc.battlebox.game.commands

import gg.astromc.battlebox.game.BattleboxGame
import gg.astromc.battlebox.game.players.BattleboxGamePlayers
import gg.astromc.battlebox.game.teams.TeamAssigner
import gg.astromc.battlebox.game.teams.strategies.NoTeamAssignmentStrategy
import gg.astromc.battlebox.game.teams.strategies.SpectatorTeamAssignmentStrategy
import world.cepi.kstom.command.arguments.literal
import world.cepi.kstom.command.kommand.Kommand

// TODO: Perms
class SpectateCommand(battleboxGame: BattleboxGame): Kommand({
    onlyPlayers

    val join by literal
    val leave by literal

    syntax(join) {
        val playerContainer = BattleboxGamePlayers.get(player.uuid) ?: return@syntax
        TeamAssigner.assignPlayerTo(playerContainer, SpectatorTeamAssignmentStrategy)
    }

    syntax(leave) {
        val playerContainer = BattleboxGamePlayers.get(player.uuid) ?: return@syntax
        TeamAssigner.assignPlayerTo(playerContainer, NoTeamAssignmentStrategy)
    }
}, "spectate")