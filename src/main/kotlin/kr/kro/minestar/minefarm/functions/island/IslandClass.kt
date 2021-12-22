package kr.kro.minestar.minefarm.functions.island

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.session.ClipboardHolder
import kr.kro.minestar.minefarm.Main
import kr.kro.minestar.minefarm.Main.Companion.islandWorld
import kr.kro.minestar.minefarm.Main.Companion.pl
import kr.kro.minestar.minefarm.Main.Companion.prefix
import kr.kro.minestar.minefarm.data.Island
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
import java.io.FileInputStream
import java.util.*
import kotlin.math.absoluteValue

object IslandClass {
    private var first = 0
    private var second = 0

    val folder = File("${pl.dataFolder}/islands")

    var offset = 1000
    fun loadOffset() {
        offset = pl.config.getInt("islandOffset")
    }

    private val islandList: HashMap<String, Island> = hashMapOf()
    fun addIsland(code: String, island: Island) {
        islandList[code] = island
    }

    fun getIsland(code: String) = islandList[code]

    fun loadIslands() {
        if (!folder.exists()) folder.mkdir()
        for (file in folder.listFiles()) Island(file)
    }

    fun createIsland(player: Player): BooleanScript {
        val playerData = PlayerClass.playerData[player] ?: return false.addScript("")
        if (playerData.farmCode() != null) return false.addScript("")
        val code = getEmptyIsland()
        val file = File(folder, code)
        val data = YamlConfiguration()

        data["ISLAND_NAME"] = "§e${player.name} §f의 섬"
        data["ISLAND_LEVEL"] = 0
        data["ISLAND_LEADER_UUID"] = player.uniqueId.toString()
        data["ISLAND_MEMBER"] = listOf(player.uniqueId.toString())

        val split = code.remove(".yml").split(',')
        val c1 = split[0].toInt() * offset.toDouble()
        val c2 = split[1].toInt() * offset.toDouble()
        val loc = Location(islandWorld, c1, 60.0, c2)
        data["CENTER"] = loc
        data["SPAWN"] = loc.clone().add(Axis.Y, 1)
        data["RADIUS"] = pl.config.getInt("islandRadius")

        data["LOCK_PVP"] = true
        data["LOCK_BUTTON"] = true
        data["LOCK_PRESSURE_PLATE"] = true
        data["LOCK_DOOR"] = true
        data["LOCK_TRAPDOOR"] = true
        data["LOCK_FENCE_GATE"] = true

        data.save(file)
        val island = Island(file)
        islandList[island.code] = island
        PlayerClass.playerIsland[player] = island
        playerData.setFarm(island.code)
        pasteIsland(island)
        PlayerClass.tpMyIsland(player)

        return true.addScript("$prefix 섬을 생성 하였습니다.")
    }


    fun pasteIsland(island: Island) {
        val loc = island.center()
        val file = File(pl.dataFolder, "default.schem").also {
            if (!it.exists()) Main.pl.saveResource("default.schem", true)
        }
        val format = ClipboardFormats.findByFile(file)
        val clipboard = format!!.getReader(FileInputStream(file)).read()
        val world = BukkitAdapter.adapt(loc.world)
        val editSession = WorldEdit.getInstance().editSessionFactory.getEditSession(world, -1)
        val operation = ClipboardHolder(clipboard)
            .createPaste(editSession)
            .to(BlockVector3.at(loc.blockX, loc.blockY, loc.blockZ))
            .ignoreAirBlocks(false)
            .build()
        Operations.complete(operation)
        editSession.flushSession()
    }

    fun getEmptyIsland(): String {
        if (!File("${pl.dataFolder}/islands", "$first,$second.yml").exists()) return "$first,$second.yml"
        if (!File("${pl.dataFolder}/islands", "${-first},${-second}.yml").exists()) return "${-first},${-second}.yml"
        if (!File("${pl.dataFolder}/islands", "${-first},$second.yml").exists()) return "${-first},$second.yml"
        if (!File("${pl.dataFolder}/islands", "$first,${-second}.yml").exists()) return "$first,${-second}.yml"
        ++first
        --second
        if (second < 0) {
            first = 0
            second = getLastSecond()
        }
        return getEmptyIsland()
    }

    fun setLastIsland() {
        val files = folder.listFiles().sortFileList()
        if (files.isEmpty()) return
        val split = files[files.size - 1].name.remove(".yml").split(',')
        first = split[0].toInt().absoluteValue
        second = split[1].toInt().absoluteValue
    }

    fun getLastSecond(): Int {
        val files = folder.listFiles()
        val list = mutableListOf<String>()
        var int = 0
        for (file in files) list.add(file.name)
        val string = list.toString()
        while (true) {
            if (!string.contains("$int.yml")) break
            ++int
        }
        return int
    }

    fun getCode(loc: Location): String {
        if (loc.world != islandWorld) return ""

        val x = loc.blockX
        val z = loc.blockZ
        val countX = x / offset
        val countZ = z / offset

        return "$countX,$countZ.yml"
    }

    /**
     * Ranking
     */
    private var ranking: HashMap<String, Int> = hashMapOf()
    private var rankingList: MutableList<Map.Entry<String, Int>> = mutableListOf()

    fun rankingInput() {
        ranking = hashMapOf()
        for (island in islandList.values) ranking[island.code] = island.level()
    }

    fun resetIslandRanking() {
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
        val island = getIsland(rankingList[ranking].key)!!
        val lore: MutableList<String> = ArrayList()
        lore.add("§f§7[§a섬 레벨§7] : §9" + island.level())
        lore.add(" ")
        lore.add("§f§8::섬 멤버::")
        for (uuid in island.member()) {
            val p = Bukkit.getOfflinePlayer(UUID.fromString(uuid))
            lore.add("§f§8" + p.name)
        }
        return Slot(line, slot, material.item().setDisplay("§9${ranking + 1} §f위 : §e${island.name()}").also { it.lore = lore })
    }
}