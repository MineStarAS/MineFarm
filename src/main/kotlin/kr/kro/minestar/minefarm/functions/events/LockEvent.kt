package kr.kro.minestar.minefarm.functions.events

import kr.kro.minestar.minefarm.Main.Companion.farmWorld
import kr.kro.minestar.minefarm.Main.Companion.prefix
import kr.kro.minestar.minefarm.data.Lock
import kr.kro.minestar.minefarm.functions.farm.FarmClass
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

    fun isFarmMember(player: Player, loc: Location): BooleanScript {
        val farm = FarmClass.getFarm(loc) ?: return false.addScript("$prefix §c자신의 섬에서만 가능합니다.")
        val center = farm.center()
        val radius = FarmClass.radius
        if (!center.isInsideToCube(loc, radius)) return false.addScript("$prefix §c자신의 섬에서만 가능합니다.")
        if (!farm.member().contains(player.uniqueId.toString())) return false.addScript("$prefix §c자신의 섬에서만 가능합니다.")
        return true.addScript()
    }

    @EventHandler
    fun playerDamageByPlayerLock(e: EntityDamageByEntityEvent) {
        if (e.isCancelled) return
        if (e.damager !is Player) return
        if (e.entity !is Player) return
        if (e.damager.isOp && (e.damager as Player).gameMode == GameMode.CREATIVE) return
        val loc = e.entity.location
        val farm = FarmClass.getFarm(FarmClass.getCode(loc)) ?: return
        if (!farm.getLock(Lock.PVP)) return
        e.isCancelled = true
    }

    @EventHandler
    fun blockBreakLock(e: BlockBreakEvent) {
        if (e.isCancelled) return
        if (e.block.world != farmWorld) return
        val player = e.player
        if (player.isOp && player.gameMode == GameMode.CREATIVE) return
        val lock = isFarmMember(player, e.block.location)
        if (!lock.boolean) {
            e.isCancelled = true
            lock.script.toPlayer(player)
        }
    }

    @EventHandler
    fun blockPlaceLock(e: BlockPlaceEvent) {
        if (e.isCancelled) return
        val player = e.player
        if (player.isOp && player.gameMode == GameMode.CREATIVE) return
        if (e.block.world != farmWorld) return
        val block = e.block
        if (block.type.toString().contains("REDSTONE") || block.type.toString().contains("LAVA")) {
            e.isCancelled = true
            return "$prefix §c사용 금지 아이템 입니다.".toPlayer(player)
        }
        val lock = isFarmMember(player, e.block.location)
        if (!lock.boolean) {
            e.isCancelled = true
            lock.script.toPlayer(player)
        }
    }

    @EventHandler
    fun interactBlockLock(e: PlayerInteractEvent) {
        if (e.isCancelled) return
        if (e.clickedBlock?.world != farmWorld) return
        val player: Player = e.player
        if (player.isOp && player.gameMode == GameMode.CREATIVE) return

        val block = e.clickedBlock ?: return
        val banItem = listOf(Material.LAVA_BUCKET)
        val chest = listOf(Material.CHEST, Material.TRAPPED_CHEST, Material.ENDER_CHEST, Material.BARREL)
        when (e.action) {
            Action.LEFT_CLICK_BLOCK,
            Action.RIGHT_CLICK_BLOCK -> {
                if (banItem.contains(player.inventory.itemInMainHand.type)) {
                    e.isCancelled = true
                    return "$prefix §c사용 금지 아이템 입니다.".toPlayer(player)
                }

                val loc: Location = e.interactionPoint ?: return
                if (chest.contains(block.type)){
                    val lock = isFarmMember(player,loc)
                    if (!lock.boolean) {
                        lock.script.toPlayer(player)
                        e.isCancelled = true
                    }
                    return
                }
                val code = FarmClass.getCode(loc)
                val farm = FarmClass.getFarm(code)
                if (farm == null) {
                    e.isCancelled = true
                    return
                }
                for (lock in Lock.values()) if (block.type.toString().contains("$lock")) if (!farm.getLock(lock)) return
            }
            Action.PHYSICAL -> {
                val loc: Location = block.location
                val code = FarmClass.getCode(loc)
                val farm = FarmClass.getFarm(code)
                if (farm == null) {
                    e.isCancelled = true
                    return
                }
                if (block.type.toString().contains("PRESSURE_PLATE")) if (farm.getLock(Lock.PRESSURE_PLATE)) return
                e.isCancelled = true
            }
        }
    }
}