package gg.astromc.battlebox.game.maps.helpers

import gg.astromc.battlebox.game.BattleboxGame
import gg.astromc.battlebox.game.teams.BattleboxTeamContainer
import gg.astromc.battlebox.game.maps.arena.BattleboxArena
import gg.astromc.battlebox.game.maps.configuration.MapConfiguration
import gg.astromc.battlebox.game.utils.PositionUtils
import gg.astromc.gamelib.Team
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.instance.block.Block

object WoolObjectiveArea {
    fun hasCompletedObjective(battleboxArena: BattleboxArena, teamContainer: BattleboxTeamContainer): Boolean {
        val woolMaterialForTeam = teamContainer.teamConfiguration?.teamColor?.woolMaterial ?: return false
        return getPoints(battleboxArena)
            .map { battleboxArena.instance.getBlock(it) }
            .all { it.namespace() == woolMaterialForTeam.namespace() }
    }

    fun setObjectiveTo(battleboxArena: BattleboxArena, block: Block) {
        getPoints(battleboxArena).forEach {
            battleboxArena.instance.setBlock(it, block)
        }
    }

    fun getObjectiveMaterialFromConfig(mapConfiguration: MapConfiguration): Block {
        return Block.fromNamespaceId(mapConfiguration.mapObjective.objectiveMaterial.namespace()) ?: Block.WHITE_WOOL
    }

    // TODO: cache this stuff!
    private fun getPoints(battleboxArena: BattleboxArena): Sequence<Pos> {
        val mapConfiguration = battleboxArena.mapConfiguration

        val objectivePosFirst = mapConfiguration.mapObjective.objectivePosStart
        val objectivePosSecond = mapConfiguration.mapObjective.objectivePosEnd

        return PositionUtils.getPointsBetween(objectivePosFirst, objectivePosSecond)
    }

    fun isPositionWithin(battleboxGame: BattleboxGame, blockPosition: Point): Boolean {
        val fromPoint = Pos.fromPoint(blockPosition)
        return getPoints(battleboxGame.currentArena).any { it == fromPoint }
    }
}