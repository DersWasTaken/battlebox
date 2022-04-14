package gg.astromc.battlebox.game.players.info

import gg.astromc.battlebox.game.BattleboxGame
import gg.astromc.battlebox.game.players.BattleboxPlayer
import gg.astromc.battlebox.game.players.PlayerContainer
import gg.astromc.battlebox.game.players.PlayerState
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.minestom.server.adventure.audience.PacketGroupingAudience
import net.minestom.server.entity.Player
import net.minestom.server.network.packet.server.play.PlayerListHeaderAndFooterPacket

// TODO: Find a way to modify what's above their head without this??
object PlayerInfoDisplay {
    fun updatePlayer(battleboxGame: BattleboxGame, playerContainer: PlayerContainer) {
        // TODO: sendHologram(battleboxGame.spectatorTeam, battleboxPlayer)

        if (playerContainer.battleboxPlayer.playerState == PlayerState.PLAYING) {
            sendTabList(playerContainer)
        }
    }

    private fun sendTabList(playerContainer: PlayerContainer) {
        val player = playerContainer.player
        val battleboxPlayer = playerContainer.battleboxPlayer

        val playerListHeaderAndFooterPacket = PlayerListHeaderAndFooterPacket(
            Component.empty(), getHologramComponent(battleboxPlayer))
        player.sendPacket(playerListHeaderAndFooterPacket)
    }

    private fun sendHologram(packetGroupingAudience: PacketGroupingAudience, battleboxPlayerContainer: PlayerContainer) {
        val battleboxPlayer = battleboxPlayerContainer.battleboxPlayer
        val player = battleboxPlayerContainer.player

        if (battleboxPlayer.playerState == PlayerState.SPECTATING) return
        val hologram = getAreaEffectCloudHologram(player, battleboxPlayer) ?: return

        if (player.hasPassenger()) {
            player.passengers.forEach { it.remove() }
        }

        val entity = hologram.entity
        player.addPassenger(entity)
        packetGroupingAudience.players.forEach { entity.addViewer(it) }
    }

    private fun getAreaEffectCloudHologram(player: Player, battleboxPlayer: BattleboxPlayer): AreaEffectCloudHologram? {
        val text = getHologramComponent(battleboxPlayer)

        return AreaEffectCloudHologram(
            instance = player.instance ?: return null,
            spawnPosition = player.position.add(0.0, 0.5, 0.0),
            text = text,
            autoViewable = false
        )
    }

    private fun getHologramComponent(battleboxPlayer: BattleboxPlayer): Component {
        return Component.join(
            JoinConfiguration.separator(Component.newline()),
            Component.text("Kills: ${battleboxPlayer.kills}"),
            Component.text("Deaths: ${battleboxPlayer.deaths}"),
            Component.text("Selected Kit: ${battleboxPlayer.currentKit ?: "None"}")
        )
    }
}