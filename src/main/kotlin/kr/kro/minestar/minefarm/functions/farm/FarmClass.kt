package kr.kro.minestar.minefarm.functions.farm

import kr.kro.minestar.minefarm.Main.Companion.farmWorld
import kr.kro.minestar.minefarm.Main.Companion.pl
import kr.kro.minestar.minefarm.Main.Companion.prefix
import kr.kro.minestar.minefarm.data.Farm
import kr.kro.minestar.minefarm.functions.PlayerClass
import kr.kro.minestar.utility.array.sortFileList
import kr.kro.minestar.utility.bool.BooleanScript
import kr.kro.minestar.utility.bool.addScript
import kr.kro.minestar.utility.location.Axis
import kr.kro.minestar.utility.location.addAxis
import kr.kro.minestar.utility.string.remove
import kr.kro.minestar.utility.string.toServer
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

object FarmClass {
    private var first = 0
    private var second = 0

    val folder = File("${pl.dataFolder}/farms")

    var offset = 107
    var radius = 50
    fun loadOffset() {
        offset = YamlConfiguration.loadConfiguration(File(pl.dataFolder, "config.yml")).getInt("farmOffset")
    }

    fun loadRadius() {
        radius = YamlConfiguration.loadConfiguration(File(pl.dataFolder, "config.yml")).getInt("farmRadius")
    }

    private val farmList: HashMap<String, Farm> = hashMapOf()
    fun addFarm(code: String, farm: Farm) {
        farmList[code] = farm
    }

    fun farmList() = farmList.values


    fun deleteFarm(farm: Farm) {
        File(folder, farm.code).delete()
        farmList.remove(farm.code)
    }

    fun getFarm(code: String) = farmList[code]
    fun getFarm(loc: Location) = farmList[getCode(loc)]

    fun loadFarms() {
        if (!folder.exists()) folder.mkdir()
        for (file in folder.listFiles()) Farm(file)
    }

    fun createFarm(player: Player): BooleanScript {
        val playerData = PlayerClass.playerData[player] ?: return false.addScript("")
        if (playerData.farmCode() != null) return false.addScript("")
        val code = getEmptyFarm()
        val file = File(folder, code)
        val data = YamlConfiguration()

        data["ISLAND_NAME"] = "§e${player.name} §f의 섬"
        data["ISLAND_LEVEL"] = 0
        data["ISLAND_LEADER_UUID"] = player.uniqueId.toString()
        data["ISLAND_MEMBER"] = listOf(player.uniqueId.toString())

        val split = code.remove(".farm").split(',')
        val c1 = split[0].toInt() * offset.toDouble()
        val c2 = split[1].toInt() * offset.toDouble()
        val loc = Location(farmWorld, c1, 40.0, c2)
        data["CENTER"] = loc
        data["SPAWN"] = loc.clone().addAxis(Axis.Y, 1)
        data["RADIUS"] = pl.config.getInt("farmRadius")

        data["LOCK_PVP"] = true
        data["LOCK_BUTTON"] = true
        data["LOCK_PRESSURE_PLATE"] = true
        data["LOCK_DOOR"] = true
        data["LOCK_TRAPDOOR"] = true
        data["LOCK_FENCE_GATE"] = true

        data["RESET_TIME"] = SimpleDateFormat("yyyy-MM-dd").format(Date())

        data.save(file)
        val farm = Farm(file)
        farmList[farm.code] = farm
        PlayerClass.playerFarm[player] = farm
        playerData.setFarm(farm.code)
//        pasteFarm(farm)
        PlayerClass.tpMyFarm(player)

        return true.addScript("$prefix 섬을 생성 하였습니다.")
    }

    fun pasteFarm(farm: Farm) {
//        val loc = farm.center()
//        val file = File(pl.dataFolder, "default.schem").also {
//            if (!it.exists()) Main.pl.saveResource("default.schem", true)
//        }
//        val format = ClipboardFormats.findByFile(file)
//        val clipboard = format!!.getReader(FileInputStream(file)).read()
//        val world = BukkitAdapter.adapt(loc.world)
//        val editSession = WorldEdit.getInstance().editSessionFactory.getEditSession(world, -1)
//        val operation = ClipboardHolder(clipboard)
//            .createPaste(editSession)
//            .to(BlockVector3.at(loc.blockX, loc.blockY, loc.blockZ))
//            .ignoreAirBlocks(false)
//            .build()
//        Operations.complete(operation)
//        editSession.flushSession()
    }

    fun getEmptyFarm(): String {
        if (!File("${pl.dataFolder}/farms", "$first,$second.farm").exists()) return "$first,$second.farm"
        if (!File("${pl.dataFolder}/farms", "${-first},${-second}.farm").exists()) return "${-first},${-second}.farm"
        if (!File("${pl.dataFolder}/farms", "${-first},$second.farm").exists()) return "${-first},$second.farm"
        if (!File("${pl.dataFolder}/farms", "$first,${-second}.farm").exists()) return "$first,${-second}.farm"
        ++first
        --second
        if (second < 0) {
            first = 0
            second = getLastSecond()
        }
        return getEmptyFarm()
    }

    fun getLastSecond(): Int {
        val files = folder.listFiles()
        val list = mutableListOf<String>()
        var int = 0
        for (file in files) list.add(file.name)
        val string = list.toString()
        while (true) {
            if (!string.contains("$int.farm")) break
            ++int
        }
        return int
    }

    fun setLastFarm() {
        val files = folder.listFiles().sortFileList()
        if (files.isEmpty()) return
        val split = files[files.size - 1].name.remove(".farm").split(',')
        first = split[0].toInt().absoluteValue
        second = split[1].toInt().absoluteValue
    }

    fun getCode(loc: Location): String {
        if (loc.world != farmWorld) return ""

        var x = loc.blockX
        var z = loc.blockZ

        if (x in -53..53 && z in -53..53) return "0,0.farm"

        if (x > 0) x += offset / 2
        else x -= offset / 2

        if (z > 0) z += offset / 2
        else z -= offset / 2

        val countX = x / offset
        val countZ = z / offset

        return "$countX,$countZ.farm"
    }
}