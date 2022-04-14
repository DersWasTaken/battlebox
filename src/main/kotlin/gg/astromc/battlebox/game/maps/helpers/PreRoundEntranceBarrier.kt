package gg.astromc.battlebox.game.maps.helpers

import gg.astromc.battlebox.game.maps.arena.BattleboxArena
import gg.astromc.battlebox.game.maps.configuration.BarrierConfiguration
import gg.astromc.battlebox.game.maps.configuration.MapConfiguration
import gg.astromc.battlebox.game.utils.PositionUtils
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block

object PreRoundEntranceBarrier {
    fun placeBarriers(battleboxArena: BattleboxArena) {
        modifyBarrier(battleboxArena.instance, battleboxArena.mapConfiguration) { barrierConfiguration ->
            Block.fromNamespaceId(barrierConfiguration.barrierMaterial.namespace())
        }
    }

    fun removeBarriers(battleboxArena: BattleboxArena) {
        modifyBarrier(battleboxArena.instance, battleboxArena.mapConfiguration) { Block.AIR }
    }

    private fun modifyBarrier(
        instance: Instance, mapConfiguration: MapConfiguration,
        blockFunction: (BarrierConfiguration) -> Block?
    ) {
        mapConfiguration.barriers.forEach { barrier ->
            val blockToSet = blockFunction(barrier) ?: Block.AIR
            val pointsBetween = PositionUtils.getPointsBetween(barrier.barrierStart, barrier.barrierEnd)

            pointsBetween.forEach {
                instance.setBlock(it, blockToSet)
            }
        }
    }
}