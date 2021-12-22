package kr.kro.minestar.minefarm.data

import kr.kro.minestar.minefarm.Main.Companion.pl
import kr.kro.minestar.minefarm.functions.island.IslandClass
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*

class Island(file: File) {
    val code: String = file.name
    override fun toString(): String = code

    private var islandLevel: Int
    private var name: String
    private var leaderUUID: String
    private var member: List<String>

    private val center: Location
    private var spawn: Location
    private var radius: Int

    private var lockPVP: Boolean
    private var lockButton: Boolean
    private var lockPressurePlate: Boolean
    private var lockDoor: Boolean
    private var lockTrapdoor: Boolean
    private var lockFenceGate: Boolean

    fun level() = islandLevel
    fun name() = name
    fun leaderUUID() = leaderUUID
    fun member() = member

    fun center() = center
    fun spawn() = spawn
    fun radius() = radius

    fun getLock(lock: Lock): Boolean {
        return when (lock) {
            Lock.PVP -> lockPVP
            Lock.BUTTON -> lockButton
            Lock.PRESSURE_PLATE -> lockPressurePlate
            Lock.DOOR -> lockDoor
            Lock.TRAPDOOR -> lockTrapdoor
            Lock.FENCE_GATE -> lockFenceGate
        }
    }

    init {
        val data = YamlConfiguration.loadConfiguration(file)
        name = data.getString("ISLAND_NAME") ?: "null"
        islandLevel = data.getInt("ISLAND_LEVEL")
        leaderUUID = data.getString("ISLAND_LEADER_UUID") ?: "null"
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

        IslandClass.getIsland(code) ?: IslandClass.addIsland(code, this)
    }

    fun file(): File = File("${pl.dataFolder}/islands", code)
    fun data(): YamlConfiguration = YamlConfiguration.loadConfiguration(file())

    fun setIslandName(name: String) {
        val data = data()
        data["NAME"] = name
        this.name = name
        data.save(file())
    }

    fun addLevel(value: Int) {
        val data = data()
        data["ISLAND_LEVEL"] = islandLevel + value
        islandLevel = data.getInt("ISLAND_LEVEL")
        data.save(file())
    }

    fun setLeaderUUID(uuid: UUID) {
        val data = data()
        data["ISLAND_LEADER_UUID"] = uuid.toString()
        leaderUUID = uuid.toString()
        data.save(file())
    }

    fun toggleLock(lock: Lock) {
        val data = data()
        when (lock) {
            Lock.PVP -> lockPVP = !lockPVP
            Lock.BUTTON -> lockButton = !lockButton
            Lock.PRESSURE_PLATE -> lockPressurePlate = !lockPressurePlate
            Lock.DOOR -> lockDoor = !lockDoor
            Lock.TRAPDOOR -> lockTrapdoor = !lockTrapdoor
            Lock.FENCE_GATE -> lockFenceGate = !lockFenceGate
        }
        data[lock.toString()] = name
        data.save(file())
    }

    fun addMember(uuid: UUID): Boolean {
        val data = data()
        val list = data.getStringList("ISLAND_MEMBER")
        if (list.contains(uuid.toString())) return false
        list.add(uuid.toString())
        data["ISLAND_MEMBER"] = list
        member = list
        data.save(file())
        return true
    }

    fun removeMember(uuid: UUID): Boolean {
        val data = data()
        val list = data.getStringList("ISLAND_MEMBER")
        list.remove(uuid.toString())
        data["ISLAND_MEMBER"] = list
        member = list
        data.save(file())
        return true
    }
}