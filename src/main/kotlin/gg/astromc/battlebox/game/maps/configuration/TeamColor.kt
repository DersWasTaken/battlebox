package gg.astromc.battlebox.game.maps.configuration

import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.color.Color
import net.minestom.server.item.Material

// TODO: Add more teams.
enum class TeamColor(val woolMaterial: Material, val color: Color) {
    Red(Material.RED_WOOL, Color(255, 85, 85)),
    Green(Material.GREEN_WOOL, Color(85, 255, 85)),
    Blue(Material.BLUE_WOOL, Color(85, 85, 255));
}