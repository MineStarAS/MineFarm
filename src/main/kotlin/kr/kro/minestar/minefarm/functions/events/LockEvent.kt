package kr.kro.minestar.minefarm.functions.events

import kr.kro.minestar.minefarm.Main.Companion.islandWorld
import kr.kro.minestar.minefarm.Main.Companion.prefix
import kr.kro.minestar.minefarm.data.Lock
import kr.kro.minestar.minefarm.functions.PlayerClass.playerIsland
import kr.kro.minestar.minefarm.functions.island.IslandClass
import kr.kro.minestar.utility.bool.BooleanScript
import kr.kro.minestar.utility.bool.addScript
import kr.kro.minestar.utility.location.isInsideToCube
import kr.kro.minestar.utility.string.toPlayer
import kr.kro.minestar.utility.string.toServer
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent

object LockEvent : Listener {

    fun isInside(player: Player, loc: Location): BooleanScript {
        val island = playerIsland[player] ?: return false.addScript("$prefix §c자신의 섬에서만 가능합니다.")
        val center = island.center()
        val radius = island.radius()
        if (!center.isInsideToCube(loc, IslandClass.offset)) return false.addScript("$prefix §c자신의 섬에서만 가능합니다.")
        if (!center.isInsideToCube(loc, radius)) return false.addScript("$prefix §c해당 위치는 확장되지 않았습니다.")
        return true.addScript("")
    }

    @EventHandler
    fun playerDamageByPlayerLock(e: EntityDamageByEntityEvent) {
        if (e.isCancelled) return
        if (e.damager !is Player) return
        if (e.entity !is Player) return
        if (e.damager.isOp && (e.damager as Player).gameMode == GameMode.CREATIVE) return
        val loc = e.entity.location
        val island = IslandClass.getIsland(IslandClass.getCode(loc)) ?: return
        if (!island.getLock(Lock.PVP)) return
        e.isCancelled = true
    }

    @EventHandler
    fun blockBreakLock(e: BlockBreakEvent) {
        if (e.isCancelled) return
        if (e.block.world != islandWorld) return
        val player: Player = e.player
        if (player.isOp && player.gameMode == GameMode.CREATIVE) return
        e.isCancelled = true
        val lock = isInside(player, e.block.location)
        e.isCancelled = lock.boolean
        lock.script.toPlayer(player)
    }

    @EventHandler
    fun blockPlaceLock(e: BlockPlaceEvent) {
        if (e.isCancelled) return
        val player: Player = e.player
        if (player.isOp && player.gameMode == GameMode.CREATIVE) return
        if (e.block.world != islandWorld) return
        val block = e.block
        if (block.type.toString().contains("REDSTONE") || block.type.toString().contains("LAVA")) {
            e.isCancelled = true
            return "$prefix §c사용 금지 아이템 입니다.".toPlayer(player)
        }
        val lock = isInside(player, e.block.location)
        if (!lock.boolean) {
            e.isCancelled = true
            lock.script.toPlayer(player)
        }
    }

//    @EventHandler
//    fun interactBlockLock(e: PlayerInteractEvent) {
//        if (e.isCancelled) return
//        val p: Player = e.player
//        if (p.isOp && p.gameMode == GameMode.CREATIVE) return
//
//        val block = e.clickedBlock
//        val banBlock = listOf(Material.ANVIL, Material.GRINDSTONE)
//        val banItem = listOf(Material.LAVA_BUCKET)
//        when (e.action) {
//            Action.LEFT_CLICK_BLOCK,
//            Action.RIGHT_CLICK_BLOCK -> {
//                if (banBlock.contains(block!!.type)) {
//                    e.isCancelled = true
//                    return "$prefix §c사용 금지 아이템 입니다.".toPlayer(p)
//                }
//                if (banItem.contains(p.inventory.itemInMainHand.type)) {
//                    e.isCancelled = true
//                    return "$prefix §c사용 금지 아이템 입니다.".toPlayer(p)
//                }
//                val loc: Location = e.interactionPoint ?: return
//                val code = IslandClass.getCode(loc)
//                val island = IslandClass.getIsland(code) ?: return
//                for (lock in Lock.values()) if (block.type.toString().contains("$lock")) if (!island.getLock(lock)) return
//                e.isCancelled = true
//                return
//            }
//            Action.PHYSICAL -> {
//                val loc: Location = e.interactionPoint ?: return
//                val code = IslandClass.getCode(loc)
//                val island = IslandClass.getIsland(code) ?: return
//                if (block!!.type.toString().contains("PRESSURE_PLATE")) if (island.getLock(Lock.PRESSURE_PLATE)) return
//                e.isCancelled = true
//            }
//        }
//    }
}