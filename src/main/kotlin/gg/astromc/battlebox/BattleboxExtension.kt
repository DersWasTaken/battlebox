package gg.astromc.battlebox

import gg.astromc.battlebox.game.BattleboxGame
import gg.astromc.battlebox.game.maps.arena.BattleboxArenaLoader
import io.github.bloepiloepi.pvp.PvpExtension
import net.minestom.server.MinecraftServer
import net.minestom.server.extensions.Extension
import net.minestom.server.extensions.Extension.LoadStatus.SUCCESS
import java.io.File

// TODO: How will i persist glow effects?
// TODO: How will i handle players leaving/joining?
// TODO: We should preload all the maps so switching between them is seamless.

/*
 * Split project up like this ?? :
 * teams/
 *   TeamConfiguration.kt
 *   TeamColor.kt
 *   TeamBalancer.kt
 * barrier/
 *    BarrierConfiguration.kt
 *    BarrierPlacement.kt
 */

// TODO: Need to find a global way to persist players and their inventories

class BattleboxExtension : Extension() {
    override fun initialize(): LoadStatus {
        logger.info("[Battlebox] Initialized!")

        // TODO: Create directory if it doesn't exist.
        dataDirectory().toFile().mkdirs()
        val mapListDirectory = File(dataDirectory().toFile(), "maps")
        val battleboxArenaLoader = BattleboxArenaLoader(mapListDirectory)
        val battleboxGame = BattleboxGame(this, battleboxArenaLoader)
        battleboxGame.startGame()

        // Initialize Pvp Extension
        PvpExtension.init()
        MinecraftServer.getGlobalEventHandler().addChild(PvpExtension.events())

        return SUCCESS
    }

    override fun terminate() {
        logger.info("[Battlebox] Terminated!")
    }
}
