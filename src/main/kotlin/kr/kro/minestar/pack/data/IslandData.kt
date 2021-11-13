package kr.kro.minestar.pack.data

import kr.kro.minestar.pack.Main
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*

data class IslandData(val code: Int) {
    private val f: File = File(Main.pl.dataFolder.toString() + "/island", "$code.yml")
    private val data: YamlConfiguration = YamlConfiguration.loadConfiguration(f)
    var islandLevel: Int = data.getInt("ISLAND_LEVEL")

    fun addLevel(value: Int) {
        data["ISLAND_LEVEL"] = islandLevel + value
        islandLevel = data.getInt("ISLAND_LEVEL")
        data.save(f)
    }

    var leaderUUID: String = data.getString("ISLAND_LEADER_UUID")!!

    fun setLeaderUUID(uuid: UUID?) {
        data["ISLAND_LEADER_UUID"] = uuid.toString()
        leaderUUID = uuid.toString()
        data.save(f)
    }

    var member: List<String> = data.getStringList("ISLAND_MEMBER")


    var center: Location = data.getLocation("CENTER")!!
    var spawn: Location = data.getLocation("SPAWN")!!
    var radius = data.getInt("RADIUS")

    val lockSetting: HashMap<LockSetting, Boolean> = HashMap()

    enum class LockSetting {
        LOCK_PVP,
        LOCK_BUTTON,
        LOCK_PRESSURE_PLATE,
        LOCK_DOOR,
        LOCK_TRAPDOOR,
        LOCK_FENCE_GATE,
    }
}