package gg.astromc.battlebox.game.players.info

import net.kyori.adventure.text.Component
import net.minestom.server.Viewable
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player
import net.minestom.server.entity.metadata.other.AreaEffectCloudMeta
import net.minestom.server.instance.Instance
import net.minestom.server.utils.validate.Check


// This is more performant than 'ArmorStand'... this has been PR'd to the Minestom repo.
// Will update when it's merged to master.
class AreaEffectCloudHologram(
    instance: Instance,
    spawnPosition: Pos,
    text: Component,
    autoViewable: Boolean = true
) : Viewable {
    private val offsetY = -0.5f
    val entity: Entity = Entity(EntityType.AREA_EFFECT_CLOUD)

    var text: Component = text
        set(textValue) {
            checkRemoved()
            field = textValue
            entity.customName = textValue
        }

    var isRemoved = false
        private set

    var position: Pos
        private set

    init {
        val areaEffectCloudMeta = entity.entityMeta as AreaEffectCloudMeta
        areaEffectCloudMeta.setNotifyAboutChanges(false)
        areaEffectCloudMeta.isHasNoGravity = true
        areaEffectCloudMeta.customName = Component.empty()
        areaEffectCloudMeta.isCustomNameVisible = true
        areaEffectCloudMeta.radius = 0f
        areaEffectCloudMeta.setNotifyAboutChanges(true)
        entity.setInstance(instance, spawnPosition.add(0.0, offsetY.toDouble(), 0.0))
        entity.isAutoViewable = autoViewable
        position = spawnPosition
        this.text = text
    }

    fun setPosition(position: Pos) {
        checkRemoved()
        this.position = position.add(0.0, offsetY.toDouble(), 0.0)
        entity.teleport(this.position)
    }

    fun remove() {
        isRemoved = true
        entity.remove()
    }

    override fun addViewer(player: Player): Boolean = entity.addViewer(player)
    override fun removeViewer(player: Player): Boolean = entity.removeViewer(player)
    override fun getViewers(): Set<Player> = entity.viewers

    private fun checkRemoved() {
        Check.stateCondition(isRemoved, "You cannot interact with a removed Hologram")
    }
}