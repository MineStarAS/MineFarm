package kr.kro.minestar.minefarm.data

import kr.kro.minestar.minefarm.Main
import kr.kro.minestar.utility.toServer
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*

data class IslandData(val code: Int) {
    private val f: File = File(Main.pl.dataFolder.toString() + "/island", "$code.yml")
    private val data: YamlConfiguration = YamlConfiguration.loadConfiguration(f)

    var islandLevel: Int
    var name: String
    var leaderUUID: String
    var member: List<String>
    var center: Location
    var spawn: Location
    var radius:Int
    val lockSetting: HashMap<LockSetting, Boolean> = HashMap()

    init {
        name = data.getString("ISLAND_NAME")!!
        islandLevel = data.getInt("ISLAND_LEVEL")
        leaderUUID = data.getString("ISLAND_LEADER_UUID")!!
        member = data.getStringList("ISLAND_MEMBER")
        center = data.getLocation("CENTER")!!
        spawn = data.getLocation("SPAWN")!!
        radius = data.getInt("RADIUS")
    }


    fun setIslandName(name: String) {
        data["NAME"] = name
        this.name = name
        data.save(f)
    }

    fun addLevel(value: Int) {
        data["ISLAND_LEVEL"] = islandLevel + value
        islandLevel = data.getInt("ISLAND_LEVEL")
        data.save(f)
    }


    fun setLeaderUUID(uuid: UUID?) {
        data["ISLAND_LEADER_UUID"] = uuid.toString()
        leaderUUID = uuid.toString()
        data.save(f)
    }


    enum class LockSetting {
        LOCK_PVP,
        LOCK_BUTTON,
        LOCK_PRESSURE_PLATE,
        LOCK_DOOR,
        LOCK_TRAPDOOR,
        LOCK_FENCE_GATE,
    }
}