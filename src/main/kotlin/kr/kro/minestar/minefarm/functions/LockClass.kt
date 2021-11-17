package kr.kro.minestar.minefarm.functions

import kr.kro.minestar.minefarm.Main
import kr.kro.minestar.minefarm.data.IslandData
import kr.kro.minestar.minefarm.data.PlayerData
import kr.kro.minestar.utility.toPlayer
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import java.io.File

class LockClass {
    val prefix = Main.prefix

    fun getIsland(loc: Location): Int {
        if (loc.world != Bukkit.getWorld("island")) return -1
        var x = loc.blockX
        val z = loc.blockZ
        var countX = 0
        val countZ = 0
        while (true) {
            if (x <= 999) break
            x -= 1000
            ++countX
        }
        val f = File(Main.pl.dataFolder.toString() + "/island", "$countX.yml")
        return if (!f.exists()) -1 else countX
    }

    fun playerDamageByPlayerLock(e: EntityDamageByEntityEvent) {
        if (e.damager.isOp && (e.damager as Player).gameMode == GameMode.CREATIVE) return
        if (e.entity.location.world != Bukkit.getWorld("island")) {
            e.isCancelled = true
            return
        }
        if (e.damager !is Player) return
        if (e.entity !is Player) return
        if (getIsland(e.entity.location) < 0) {
            e.isCancelled = true
            return
        }
        val isData = IslandData(getIsland(e.entity.location))
        if (isData.lockSetting[IslandData.LockSetting.LOCK_PVP]!!) return
        e.isCancelled = true
    }

    fun blockBreakLock(e: BlockBreakEvent) {
        val p: Player = e.player
        if (p.isOp && p.gameMode == GameMode.CREATIVE) return
        if (p.world.name != "island") {
            e.isCancelled = true
            return
        }
        val block: Block = e.block
        val loc = block.location
        val pData = PlayerData(p)
        if (pData.islandCode < 0) {
            e.isCancelled = true
            return "$prefix §c자신의 섬에서만 가능합니다.".toPlayer(p)
        }
        val isData = IslandData(pData.islandCode)
        val bb = getLock(loc, isData)
        if (bb[0] && bb[1]) return
        else if (!bb[0]) "$prefix §c자신의 섬에서만 가능합니다.".toPlayer(p)
        else if (bb[0] && !bb[1]) "$prefix §c해당 위치는 확장되지 않았습니다.".toPlayer(p)
        e.isCancelled = true
    }

    fun blockPlaceLock(e: BlockPlaceEvent) {
        val p: Player = e.player
        if (p.isOp && p.gameMode == GameMode.CREATIVE) return
        if (p.world.name != "island") {
            e.isCancelled = true
            return
        }
        val block: Block = e.block
        if (e.block.type.toString().contains("REDSTONE")) {
            e.isCancelled = true
            return
        }
        val loc = block.location
        val pData = PlayerData(p)
        if (pData.islandCode < 0) {
            e.isCancelled = true
            "$prefix §c자신의 섬에서만 가능합니다.".toPlayer(p)
            return
        }
        val isData = IslandData(pData.islandCode)
        val bb = getLock(loc, isData)
        if (bb[0] && bb[1]) return
        else if (!bb[0]) "$prefix §c자신의 섬에서만 가능합니다.".toPlayer(p)
        else if (bb[0] && !bb[1]) "$prefix §c해당 위치는 확장되지 않았습니다.".toPlayer(p)
        e.isCancelled = true
    }

    fun getLock(loc: Location, islandData: IslandData): BooleanArray {
        var b1 = false
        var b2 = false
        val pos1: Location = islandData.center.clone().add(500.0, 0.0, 500.0).also { it.y = 255.0 }
        val pos2: Location = islandData.center.clone().add(-500.0, 0.0, -500.0).also { it.y = 0.0 }
        val x1 = intArrayOf(pos1.blockX, pos2.blockX)
        val z1 = intArrayOf(pos1.blockZ, pos2.blockZ)
        if (x1[0] <= loc.blockX && loc.blockX <= x1[1]) if (z1[0] <= loc.blockZ && loc.blockZ <= z1[1]) b1 = true
        val center: Location = islandData.center
        val radius: Int = islandData.radius
        val rPos1 = center.clone().add(-radius.toDouble(), 0.0, -radius.toDouble())
        val rPos2 = center.clone().add(radius.toDouble(), 0.0, radius.toDouble())
        rPos1.y = 0.0
        rPos2.y = 255.0
        val x2 = intArrayOf(rPos1.blockX, rPos2.blockX)
        val z2 = intArrayOf(rPos1.blockZ, rPos2.blockZ)
        if (x2[0] <= loc.blockX && loc.blockX <= x2[1]) if (z2[0] <= loc.blockZ && loc.blockZ <= z2[1]) b2 = true
        return booleanArrayOf(b1, b2)
    }

    fun interactBlockLock(e: PlayerInteractEvent) {
        val p: Player = e.player
        if (p.isOp && p.gameMode == GameMode.CREATIVE) return
        if (!(((e.clickedBlock!!.type != Material.ANVIL) && (e.clickedBlock!!.type != Material.GRINDSTONE)))) {
            e.isCancelled = true
            return
        }
        val loc: Location = e.interactionPoint!!
        if (loc.world != Bukkit.getWorld("island")) return
        if (getIsland(loc) < 0) return
        val isData = IslandData(getIsland(loc))
        val pData = PlayerData(p)
        if (pData.islandCode > 0) if (isData.member.contains(p.toString())) return
        val bb = getLock(loc, isData)
        if (bb[0] && bb[1]) return
        val block: Block = e.clickedBlock!!
        if (block.type.toString().contains("BUTTON")) if (isData.lockSetting[IslandData.LockSetting.LOCK_BUTTON]!!) return
        if (block.type.toString().contains("PRESSURE_PLATE")) if (isData.lockSetting[IslandData.LockSetting.LOCK_PRESSURE_PLATE]!!) return
        if (block.type.toString().contains("TRAPDOOR")) if (isData.lockSetting[IslandData.LockSetting.LOCK_TRAPDOOR]!!) return
        if (block.type.toString().contains("DOOR")) if (isData.lockSetting[IslandData.LockSetting.LOCK_DOOR]!!) return
        if (block.type.toString().contains("FENCE_GATE")) if (isData.lockSetting[IslandData.LockSetting.LOCK_FENCE_GATE]!!) return
        e.isCancelled = true
    }

    fun interactPressurePlateLock(e: PlayerInteractEvent) {
        val p: Player = e.player
        if (p.isOp && p.gameMode == GameMode.CREATIVE) return
        val loc: Location = p.location
        if (loc.world != Bukkit.getWorld("island")) return
        if (getIsland(loc) < 0) return
        val isData = IslandData(getIsland(loc))
        val pData = PlayerData(p)
        if (pData.islandCode > 0) if (isData.member.contains(p.toString())) return
        val bb = getLock(loc, isData)
        if (bb[0] && bb[1]) return
        val block: Block = e.clickedBlock!!
        if (block.type.toString().contains("PRESSURE_PLATE")) if (isData.lockSetting[IslandData.LockSetting.LOCK_PRESSURE_PLATE]!!) return
        e.isCancelled = true
    }
}