package gg.astromc.battlebox.game.players

import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import world.cepi.kstom.Manager


data class PlayerContainer(
    var player: Player,
    val battleboxPlayer: BattleboxPlayer
)

fun Player.reset() {
    this.isInvisible = false
    this.clearEffects()
    this.gameMode = GameMode.ADVENTURE
    this.inventory.clear()
    this.heal()

//    this.showPlayer()
}

//fun Player.showPlayer() {
//    val skin = skin
//    val prop =
//        if (skin != null) listOf(AddPlayer.Property("textures", skin.textures(), skin.signature())) else emptyList()
//
//    val addPlayer = PlayerInfoPacket(
//        PlayerInfoPacket.Action.ADD_PLAYER,
//        AddPlayer(uuid, username, prop, gameMode, latency, displayName)
//    )
//    val spawnPacket = entityType.registry().spawnType().getSpawnPacket(this)
//    val velocityPacket = EntityVelocityPacket(getEntityId(),
//        velocity.mul((8000f / MinecraftServer.TICK_PER_SECOND).toDouble()))
//    val metaData = metadataPacket
//    val equipment = equipmentsPacket
//
//    if (team != null) {
//        team.createTeamsCreationPacket()
//    }
//    val packet = EntityHeadLookPacket(entityId, position.yaw())
//
//    Manager.connection.onlinePlayers.forEach {
//        if(it.uuid != uuid) {
//            it.sendPacket(addPlayer)
//            it.sendPacket(spawnPacket)
//            it.sendPacket(velocityPacket)
//            it.sendPacket(metaData)
//            it.sendPacket(equipment)
//            it.sendPacket(packet)
//            if (team != null) {
//                it.sendPacket(team.createTeamsCreationPacket())
//            }
//        }
//    }
//}
//
//fun Player.hidePlayer() {
//    val packet = PlayerInfoPacket(
//        PlayerInfoPacket.Action.REMOVE_PLAYER,
//        PlayerInfoPacket.RemovePlayer(this.uuid)
//    )
//
//    MinecraftServer.getSchedulerManager().buildTask {
//        Manager.connection.onlinePlayers.forEach {
//            val connection = it.playerConnection
//            connection.sendPacket(packet)
//        } }.delay(20, TimeUnit.SERVER_TICK).schedule()
//}

fun PlayerContainer.refreshPlayer() {
    player = Manager.connection.getPlayer(player.uuid)!!
}

fun BattleboxPlayer.reset() {
    this.currentKit = null
    this.teamContainer = null
}