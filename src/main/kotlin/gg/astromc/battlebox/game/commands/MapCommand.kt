package gg.astromc.battlebox.game.commands

import gg.astromc.battlebox.game.BattleboxGame
import net.minestom.server.command.builder.arguments.ArgumentString
import world.cepi.kstom.command.kommand.Kommand

class MapCommand(battleboxGame: BattleboxGame): Kommand({
    onlyPlayers

    val mapNameArgument = ArgumentString("map_name")

    syntax(mapNameArgument) {
        if (!player.hasPermission("battlebox.setmap")) {
            return@syntax
        }

        val mapName = get(mapNameArgument)
        battleboxGame.setMap(mapName)
    }
}, "map")