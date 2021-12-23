package kr.kro.minestar.minefarm.functions.farm

import kr.kro.minestar.minefarm.Main.Companion.farmWorld
import kr.kro.minestar.minefarm.Main.Companion.pl
import kr.kro.minestar.minefarm.Main.Companion.prefix
import kr.kro.minestar.minefarm.data.Farm
import kr.kro.minestar.minefarm.functions.PlayerClass
import kr.kro.minestar.utility.array.sortFileList
import kr.kro.minestar.utility.bool.BooleanScript
import kr.kro.minestar.utility.bool.addScript
import kr.kro.minestar.utility.item.Slot
import kr.kro.minestar.utility.item.setDisplay
import kr.kro.minestar.utility.location.Axis
import kr.kro.minestar.utility.location.add
import kr.kro.minestar.utility.material.item
import kr.kro.minestar.utility.string.remove
import kr.kro.minestar.utility.string.toServer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
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
        val loc = Location(farmWorld, c1, 60.0, c2)
        data["CENTER"] = loc
        data["SPAWN"] = loc.clone().add(Axis.Y, 1)
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

        val x = loc.blockX
        val z = loc.blockZ
        val countX = x / offset
        val countZ = z / offset

        return "$countX,$countZ.farm"
    }

    /**
     * Ranking
     */
    private var ranking: HashMap<String, Int> = hashMapOf()
    private var rankingList: MutableList<Map.Entry<String, Int>> = mutableListOf()

    fun rankingInput() {
        ranking = hashMapOf()
        for (farm in farmList.values) ranking[farm.code] = farm.level()
    }

    fun resetFarmRanking() {
        rankingList = ArrayList<Map.Entry<String, Int>>(ranking.entries)
        rankingList.sortWith(Comparator { (_, value), (_, value1) -> value1.compareTo(value) })
        for ((key, value) in rankingList) "$key : $value".toServer()

    }

    fun getRanking(code: String): Int? {
        for (map in rankingList) if (map.key == code) return rankingList.indexOf(map)
        return null
    }

    fun getRankingItem(ranking: Int): Slot? {
        val material: Material
        val line: Int
        val slot: Int
        when (ranking) {
            0 -> {
                material = Material.GOLD_INGOT
                line = 0
                slot = 4
            }
            1 -> {
                material = Material.IRON_INGOT
                line = 1
                slot = 2
            }
            2 -> {
                material = Material.COPPER_INGOT
                line = 1
                slot = 6
            }
            3 -> {
                material = Material.RED_DYE
                line = 2
                slot = 1
            }
            4 -> {
                material = Material.ORANGE_DYE
                line = 2
                slot = 2
            }
            5 -> {
                material = Material.YELLOW_DYE
                line = 2
                slot = 3
            }
            6 -> {
                material = Material.GREEN_DYE
                line = 2
                slot = 4
            }
            7 -> {
                material = Material.BLUE_DYE
                line = 2
                slot = 5
            }
            8 -> {
                material = Material.CYAN_DYE
                line = 2
                slot = 6
            }
            9 -> {
                material = Material.PURPLE_DYE
                line = 2
                slot = 7
            }
            else -> return null
        }

        if (rankingList.toTypedArray().size <= ranking) return Slot(line, slot, material.item().setDisplay("§9${ranking + 1} §f위 : §8 없음"))
        val farm = getFarm(rankingList[ranking].key)!!
        val lore: MutableList<String> = ArrayList()
        lore.add("§f§7[§a섬 레벨§7] : §9" + farm.level())
        lore.add(" ")
        lore.add("§f§8::섬 멤버::")
        for (uuid in farm.member()) {
            val p = Bukkit.getOfflinePlayer(UUID.fromString(uuid))
            lore.add("§f§8" + p.name)
        }
        return Slot(line, slot, material.item().setDisplay("§9${ranking + 1} §f위 : §e${farm.name()}").also { it.lore = lore })
    }
}