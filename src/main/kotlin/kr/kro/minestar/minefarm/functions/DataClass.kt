package kr.kro.minestar.minefarm.functions

import kr.kro.minestar.minefarm.Main
import kr.kro.minestar.minefarm.data.PlayerData
import kr.kro.minestar.utility.toPlayer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class DataClass {
    private val prefix: String = Main.prefix
    fun createIslandFile(p: Player) {
        val pData = PlayerData(p)
        if (pData.islandCode > 0) "$prefix §c이미 섬이 있습니다.".toPlayer(p).let { return }
        val folder = File(Main.pl.dataFolder.toString() + "/island").also {
            if (!Main.pl.dataFolder.exists()) Main.pl.dataFolder.mkdir()
            if (!it.exists()) it.mkdir()
        }
        val files: Array<File> = folder.listFiles()
        var i = 0
        if (folder.exists() && files.isNotEmpty()) i = files.size
        val file = File(Main.pl.dataFolder.toString() + "/island", "$i.yml")
        val data: YamlConfiguration = YamlConfiguration.loadConfiguration(file)
        val member: MutableList<String> = ArrayList()
        member.add(p.uniqueId.toString())
        data.set("ISLAND_CODE", i)
        data.set("ISLAND_NAME", p.name + "님의 섬")
        data.set("ISLAND_LEVEL", 0)
        data.set("ISLAND_LEADER_NAME", p.name)
        data.set("ISLAND_LEADER_UUID", p.uniqueId.toString())
        data.set("ISLAND_MEMBER", member.toTypedArray())
        val world = Bukkit.getWorld("island")
        val center = Location(world, (i * 1000 + 500).toDouble(), 60.0, 0.0)
        data.set("CENTER", center)
        data.set("SPAWN", center.clone().add(0.0, 1.0, 0.0))
        data.set("RADIUS", 25)
        data.set("LOCK_PVP", true)
        data.set("LOCK_BUTTON", true)
        data.set("LOCK_PRESSURE_PLATE", true)
        data.set("LOCK_DOOR", true)
        data.set("LOCK_TRAPDOOR", true)
        data.set("LOCK_FENCE_GATE", true)
        data.save(file)
        pData.setIsCode(i)
    }

    fun createPlayerData(p: Player): Boolean {
        val file = File(Main.pl.dataFolder.toString() + "/player", p.uniqueId.toString() + ".yml")
        val data: YamlConfiguration = YamlConfiguration.loadConfiguration(file)
        if (file.exists()) return true
        data.set("NAME", p.name)
        data.set("CUSTOM_NAME", "NULL")
        data.set("UUID", p.uniqueId.toString())
        data.set("ISLAND", -1)
        data.set("FARM_CHAT", false)
        data.save(file)
        return false
    }
}