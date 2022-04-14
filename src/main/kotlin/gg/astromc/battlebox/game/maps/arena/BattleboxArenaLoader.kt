package gg.astromc.battlebox.game.maps.arena

import gg.astromc.slimeloader.loader.SlimeLoader
import gg.astromc.slimeloader.source.FileSlimeSource
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.minestom.server.utils.NamespaceID
import net.minestom.server.world.DimensionType
import world.cepi.kstom.Manager
import world.cepi.kstom.Manager.dimensionType
import java.io.File
import java.util.concurrent.ThreadLocalRandom

// TODO: Loading from the workshop
class BattleboxArenaLoader(mapListDirectory: File) {
    private val mapCache = mutableListOf<SlimeMapData>()

    init {
        mapListDirectory.listFiles()
            ?.filter { it.isDirectory }
            ?.forEach { mapDirectory ->
                val contents = mapDirectory.listFiles() ?: return@forEach
                val slimeFile = contents.firstOrNull { it.extension == "slime" } ?: return@forEach
                val configurationFile = contents.firstOrNull { it.extension == "json" } ?: return@forEach

                val slimeMapData = SlimeMapData(
                    filePath = slimeFile.path,
                    mapConfiguration = Json.decodeFromString(configurationFile.readText())
                )

                mapCache.add(slimeMapData)
            }
    }

    fun createRandomArena(): BattleboxArena? {
        val current = ThreadLocalRandom.current()
        val randomMap = mapCache[current.nextInt(0, mapCache.size)]
        return createArena(randomMap.mapConfiguration.name)
    }

    fun createArena(mapName: String): BattleboxArena? {
        val slimeMapData = mapCache.firstOrNull { it.mapConfiguration.name.equals(mapName, ignoreCase = true) } ?: return null

        val filePath = slimeMapData.filePath
        val mapConfiguration = slimeMapData.mapConfiguration

        val slimeSource = FileSlimeSource(File(filePath))
        println("Loading map from: $filePath")

        val miniGameDimensionID = NamespaceID.from("battlebox", mapConfiguration.name.lowercase())
        val miniGameDimension = if (dimensionType.isRegistered(miniGameDimensionID)) {
            dimensionType.getDimension(miniGameDimensionID)!!
        } else {
            val newDimension = DimensionType
                .builder(miniGameDimensionID)
                .ambientLight(1.0f)
                .build()
            dimensionType.addDimension(newDimension)
            newDimension
        }

        val instance = Manager.instance.createInstanceContainer(miniGameDimension).apply {
            chunkLoader = SlimeLoader(this, slimeSource)
        }

        return BattleboxArena(
            instance = instance,
            mapConfiguration = mapConfiguration
        )
    }
}