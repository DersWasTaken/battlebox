package gg.astromc.battlebox.game.kits

import gg.astromc.battlebox.game.BattleboxGame
import gg.astromc.battlebox.game.maps.arena.BattleboxArena
import gg.astromc.battlebox.game.maps.configuration.KitConfiguration
import gg.astromc.battlebox.game.players.BattleboxGamePlayers
import gg.astromc.battlebox.game.players.PlayerContainer
import gg.astromc.battlebox.game.players.PlayerState
import gg.astromc.battlebox.game.utils.PositionUtils
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.EntityCreature
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.metadata.other.ItemFrameMeta
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import world.cepi.kstom.nbt.TagJson
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class KitSelector(private val battleboxArena: BattleboxArena) {
    private val kitSelectorDataTag = TagJson<KitSelectorData>("kit_selector_data")

    fun initSelection() {
        val instance = battleboxArena.instance
        val mapConfig = battleboxArena.mapConfiguration

        mapConfig.teams.forEach { teamConfiguration ->
            teamConfiguration.kitSelectionList.forEach inner@ { kitSelectionConfiguration ->
                val selectionButtonPos = kitSelectionConfiguration.selectionButtonPos
                val kitConfiguration = mapConfig.kits.firstOrNull { it.name == kitSelectionConfiguration.kitName } ?: return@inner

                val itemFrameEntity = EntityCreature(EntityType.ITEM_FRAME)
                val itemFrameMeta = itemFrameEntity.entityMeta as? ItemFrameMeta
                itemFrameMeta?.item = ItemStack.of(kitSelectionConfiguration.itemFrameDisplay)
                itemFrameEntity.setInstance(instance, kitSelectionConfiguration.itemFrameLocation)

                val blocks = PositionUtils.getPointsBetween(kitSelectionConfiguration.backgroundStart,kitSelectionConfiguration.backgroundStart)
                blocks.forEach {
                    instance.setBlock(it, Block.GREEN_TERRACOTTA)
                }

                val kitSelectorData = KitSelectorData(
                    kitConfiguration = kitConfiguration,
                    kitSelectionConfiguration = kitSelectionConfiguration,
                    isSelected = false
                )

                val button = instance.getBlock(selectionButtonPos).withTag(kitSelectorDataTag, kitSelectorData)
                instance.setBlock(selectionButtonPos, button)
            }
        }
    }

    // TODO: Change background!
    // TODO: This method returns the selected kit ........ make it more clear?
    fun handleKitButtonPress(battleboxGame: BattleboxGame, instance: Instance, pos: Pos, playerContainer: PlayerContainer): KitConfiguration? {
        val block = instance.getBlock(pos)
        val kitSelectorData = block.getTag(kitSelectorDataTag) ?: return null

        if (kitSelectorData.isSelected) return null

        KitGiver.giveKitTo(battleboxGame, playerContainer, kitSelectorData.kitConfiguration)

        val updatedKitSelectorData = kitSelectorData.copy(
            isSelected = true
        )

        val blocks = PositionUtils.getPointsBetween(kitSelectorData.kitSelectionConfiguration.backgroundStart,kitSelectorData.kitSelectionConfiguration.backgroundStart)
        blocks.forEach {
            instance.setBlock(it, Block.RED_TERRACOTTA)
        }

        instance.setBlock(pos, block.withTag(kitSelectorDataTag, updatedKitSelectorData))
        return updatedKitSelectorData.kitConfiguration
    }

    // TODO: I use addPlayer { currentKit = ... } elsewhere... this should probably be moved to KitGiver
    fun selectRandomKits(battleboxGame: BattleboxGame) {
        val mapConfiguration = battleboxGame.currentArena.mapConfiguration
        val teamMap = BattleboxGamePlayers.getAll()
            .filter { it.battleboxPlayer.teamContainer != null && it.battleboxPlayer.playerState == PlayerState.PLAYING }
            .groupBy { it.battleboxPlayer.teamContainer!! }

        teamMap.forEach { (teamContainer, players) ->
            val teamConfiguration = teamContainer.teamConfiguration ?: return@forEach
            val kitSelectionList = teamConfiguration.kitSelectionList
            val threadLocalRandom = ThreadLocalRandom.current()

            val availableKits = kitSelectionList
                .filter { kitSelection -> players.none { it.battleboxPlayer.currentKit == kitSelection.kitName } }
                .toMutableList()

            val playersWithoutKit = players
                .filter { it.battleboxPlayer.currentKit == null }

            playersWithoutKit.forEach outer@ { playerContainer ->
                val randomKit = availableKits[threadLocalRandom.nextInt(availableKits.size)]
                val kitConfig = mapConfiguration.kits.firstOrNull { it.name == randomKit.kitName } ?: return@outer
                KitGiver.giveKitTo(battleboxGame, playerContainer, kitConfig)
                availableKits.remove(randomKit)
            }
        }
    }
}