package gg.astromc.battlebox.game.kits

import gg.astromc.battlebox.game.BattleboxGame
import gg.astromc.battlebox.game.maps.configuration.KitConfiguration
import gg.astromc.battlebox.game.maps.helpers.WoolObjectiveArea
import gg.astromc.battlebox.game.players.PlayerContainer
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemMeta
import net.minestom.server.item.ItemMetaBuilder
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.item.metadata.LeatherArmorMeta
import world.cepi.kstom.item.item
import world.cepi.kstom.item.withMeta

// TODO: Handle wool placeOn and destroy!
object KitGiver {
    fun giveKitTo(battleboxGame: BattleboxGame, playerContainer: PlayerContainer, kitConfiguration: KitConfiguration) {
        val player = playerContainer.player
        val battleboxPlayer = playerContainer.battleboxPlayer

        val teamConfiguration = battleboxPlayer.teamContainer?.teamConfiguration ?: return

        val mapConfiguration = battleboxGame.currentArena.mapConfiguration

        val canModify = mapConfiguration
            .teams
            .mapNotNull { Block.fromNamespaceId(it.teamColor.woolMaterial.namespace()) }
            .toMutableSet()

        val objectiveMaterialFromConfig =
            WoolObjectiveArea.getObjectiveMaterialFromConfig(mapConfiguration)

        canModify.add(objectiveMaterialFromConfig)

        player.inventory.clear()

        kitConfiguration.contents.forEach { originalItemStack ->
            val itemStack = item(originalItemStack.material, originalItemStack.amount) {
                canPlaceOn(canModify)
                canDestroy(canModify)
            }
            player.inventory.addItemStack(itemStack)
        }

        player.inventory.boots = ItemStack.of(Material.LEATHER_BOOTS)
            .withMeta(LeatherArmorMeta::class.java) { it.color(teamConfiguration.teamColor.color) }

        val woolItemStack = item(teamConfiguration.teamColor.woolMaterial, 64) {
            canPlaceOn(canModify.also {
                it.addAll(mapConfiguration.mapObjective.surroundingAreaMaterials.mapNotNull {
                    Block.fromNamespaceId(it.namespace())
                })
            })
            canDestroy(canModify)
        }

        player.inventory.addItemStack(woolItemStack)
        battleboxPlayer.currentKit = kitConfiguration.name
    }
}