package kr.kro.minestar.minefarm.data

import kr.kro.minestar.minefarm.Main
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
    var radius: Int

    var lockPVP: Boolean
    var lockButton: Boolean
    var lockPressurePlate: Boolean
    var lockDoor: Boolean
    var lockTrapdoor: Boolean
    var lockFenceGate: Boolean

    init {
        name = data.getString("ISLAND_NAME")!!
        islandLevel = data.getInt("ISLAND_LEVEL")
        leaderUUID = data.getString("ISLAND_LEADER_UUID")!!
        member = data.getStringList("ISLAND_MEMBER")
        center = data.getLocation("CENTER")!!
        spawn = data.getLocation("SPAWN")!!
        radius = data.getInt("RADIUS")

        lockPVP = data.getBoolean("LOCK_PVP")
        lockButton = data.getBoolean("LOCK_BUTTON")
        lockPressurePlate = data.getBoolean("LOCK_PRESSURE_PLATE")
        lockDoor = data.getBoolean("LOCK_DOOR")
        lockTrapdoor = data.getBoolean("LOCK_TRAPDOOR")
        lockFenceGate = data.getBoolean("LOCK_FENCE_GATE")
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

    fun toggleLock(lock: Lock) {
        when (lock){
            Lock.LOCK_PVP -> lockPVP = !lockPVP
            Lock.LOCK_BUTTON -> lockButton = !lockButton
            Lock.LOCK_PRESSURE_PLATE -> lockPressurePlate = !lockPressurePlate
            Lock.LOCK_DOOR -> lockDoor = !lockDoor
            Lock.LOCK_TRAPDOOR -> lockTrapdoor = !lockTrapdoor
            Lock.LOCK_FENCE_GATE -> lockFenceGate = !lockFenceGate
        }
        data[lock.toString()] = name
        data.save(f)
    }

    enum class Lock {
        LOCK_PVP,
        LOCK_BUTTON,
        LOCK_PRESSURE_PLATE,
        LOCK_DOOR,
        LOCK_TRAPDOOR,
        LOCK_FENCE_GATE,
    }
}