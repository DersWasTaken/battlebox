package gg.astromc.battlebox.game.utils

import net.minestom.server.coordinate.Pos
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object PositionUtils {
    fun getPointsBetween(first: Pos, second: Pos): Sequence<Pos> {
        return sequence {
            val minMaxPair = createMinMaxPair(first, second)

            for (x in minMaxPair.minPosition.blockX()..minMaxPair.maxPosition.blockX()) {
                for (y in minMaxPair.minPosition.blockY()..minMaxPair.maxPosition.blockY()) {
                    for (z in minMaxPair.minPosition.blockZ()..minMaxPair.maxPosition.blockZ()) {
                        yield(Pos(x.toDouble(), y.toDouble(), z.toDouble()))
                    }
                }
            }
        }
    }

    private fun createMinMaxPair(first: Pos, second: Pos): MinMaxPair {
        val minPosition = Pos(min(first.x, second.x), min(first.y, second.y), min(first.z, second.z))
        val maxPosition = Pos(max(first.x, second.x), max(first.y, second.y), max(first.z, second.z))

        return MinMaxPair(
            minPosition = minPosition,
            maxPosition = maxPosition
        )
    }
}

data class MinMaxPair(val minPosition: Pos, val maxPosition: Pos) {

}