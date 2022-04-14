package gg.astromc.battlebox.game.kits

import gg.astromc.battlebox.game.maps.configuration.KitConfiguration
import gg.astromc.battlebox.game.maps.configuration.KitSelectionConfiguration
import kotlinx.serialization.Serializable

@Serializable
data class KitSelectorData(
    val isSelected: Boolean = false,
    val kitConfiguration: KitConfiguration,
    val kitSelectionConfiguration: KitSelectionConfiguration
)