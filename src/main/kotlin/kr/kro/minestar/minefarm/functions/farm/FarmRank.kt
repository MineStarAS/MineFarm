package kr.kro.minestar.minefarm.functions.farm

import kr.kro.minestar.minefarm.Main.Companion.pl
import kr.kro.minestar.minefarm.data.Farm
import kr.kro.minestar.utility.item.Slot
import kr.kro.minestar.utility.item.addLore
import kr.kro.minestar.utility.item.clearLore
import kr.kro.minestar.utility.item.setDisplay
import kr.kro.minestar.utility.material.item
import kr.kro.minestar.utility.string.toServer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import java.util.*

object FarmRank : Listener {
    private var ranking: HashMap<Farm, Int> = hashMapOf()
    private var rankingList: MutableList<Map.Entry<Farm, Int>> = mutableListOf()
    val gui = Bukkit.createInventory(null, 9 * 3, "팜 랭킹")
    val slots = listOf(
        Slot(0, 4, Material.GOLD_INGOT.item()),
        Slot(1, 2, Material.IRON_INGOT.item()),
        Slot(1, 6, Material.COPPER_INGOT.item()),
        Slot(2, 1, Material.RED_DYE.item()),
        Slot(2, 2, Material.ORANGE_DYE.item()),
        Slot(2, 3, Material.YELLOW_DYE.item()),
        Slot(2, 4, Material.GREEN_DYE.item()),
        Slot(2, 5, Material.BLUE_DYE.item()),
        Slot(2, 6, Material.CYAN_DYE.item()),
        Slot(2, 7, Material.PURPLE_DYE.item()),
    )

    fun init() = Bukkit.getPluginManager().registerEvents(this, pl)

    fun rankingInput() {
        ranking = hashMapOf()
        for (farm in FarmClass.farmList()) ranking[farm] = farm.level()
    }

    fun resetFarmRanking() {
        rankingList = ArrayList<Map.Entry<Farm, Int>>(ranking.entries)
        rankingList.sortWith(Comparator { (_, value), (_, value1) -> value1.compareTo(value) })
        for ((key, value) in rankingList) "$key : $value".toServer()
        setSlotItem()
    }

    fun getRanking(code: String): Int? {
        for (map in rankingList) if (map.key.code == code) return rankingList.indexOf(map)
        return null
    }

    @EventHandler
    fun click(e: InventoryClickEvent) {
        if (e.inventory == gui) e.isCancelled = true
    }

    fun openGUI(player: Player) {
        player.openInventory(gui)
    }

    fun displaying () {
        gui.clear()
        for (slot in slots) gui.setItem(slot.get, slot.item)
    }


    fun setSlotItem() {
        for (int in 0..9) {
            val item = slots[int].item
            val farm = if (rankingList.size > int) rankingList[int].key
            else null
            val rank = int + 1
            val farmName = farm?.name() ?: "없음"
            val display: String = when (int) {
                0 -> "§e$rank 위 : $farmName"
                1 -> "§b$rank 위 : $farmName"
                2 -> "§a$rank 위 : $farmName"
                else -> "§9$rank 위 : $farmName"
            }
            item.setDisplay(display)
            item.clearLore()
            if (farm != null) {
                item.addLore(" ")
                item.addLore("§a◇ 섬 레벨 : ${farm.level()}")
                item.addLore(" ")
                item.addLore("§e◇ 팜 멤버")
                item.addLore(Bukkit.getOfflinePlayer(UUID.fromString(farm.leaderUUID())).name!!)
                for (uuid in farm.member()) if (uuid != farm.leaderUUID()) item.addLore(Bukkit.getOfflinePlayer(UUID.fromString(uuid)).name!!)
            }
        }
        displaying ()
    }
}