package kr.kro.minestar.pack.functions

import kr.kro.minestar.pack.Main
import kr.kro.minestar.pack.data.IslandData
import kr.kro.minestar.pack.data.PlayerData
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
import org.bukkit.scheduler.BukkitTask
import java.io.File
import java.util.*

class IslandClass {
    private val prefix: String = Main.prefix
    private val inviteTimer: MutableMap<Player, BukkitTask> = HashMap()
    private val invitePlayer: MutableMap<Player, Player> = HashMap()
    private val ranking: MutableMap<Int?, Int?> = HashMap()

    fun tpMyIsland(p: Player): Boolean {
        val pData = PlayerData(p)
        if (pData.islandCode < 0) {
            p.sendMessage("$prefix §c섬이 없습니다.")
            return false
        }
        val isData = IslandData(pData.islandCode)
        p.teleport(isData.spawn)
        return true
    }

    fun createIsland(p: Player) {
        val pData = PlayerData(p)
        if (pData.islandCode > 0) return "$prefix §c이미 섬이 있습니다.".toPlayer(p)
        DataClass().createIslandFile(p)
        p.sendMessage(prefix + "섬으로 이동합니다.")
        val data = PlayerData(p)
        val isData = IslandData(data.islandCode)
//        utilCode.setStructure(isData.getCenter())
        p.teleport(isData.spawn)
    }

    fun inviteIsland(p: Player, name: String) {
        val tPlayer: Player = Bukkit.getPlayer(name) ?: return "$prefix §c존재하지 않는 플레이어 이거나, 접속 중이지 않은 플레이어입니다.".toPlayer(p)
        if (invitePlayer.containsKey(tPlayer) || inviteTimer.containsKey(tPlayer)) return "$prefix  §c대기 중인 초대가 있습니다.".toPlayer(p)
        val pData = PlayerData(p)
        if (pData.islandCode < 0) return "$prefix §c섬이 없습니다.".toPlayer(p)
        val isData = IslandData(pData.islandCode)
        if (isData.member.size >= 5) return "$prefix §c섬 인원이 가득 차 있습니다.".toPlayer(p)
        val targetPlayerData = PlayerData(tPlayer)
        val targetIsData = IslandData(targetPlayerData.islandCode)
        if (pData.islandCode < 0) return "$prefix §c섬이 없습니다.".toPlayer(p)
        if (tPlayer == p) return "$prefix §c자신을 초대할 수 없습니다.".toPlayer(p)
        if (isData.member.contains(tPlayer.toString())) return "$prefix §c이미 섬원입니다.".toPlayer(p)
        if (targetPlayerData.islandCode > 0) if (targetIsData.member.isNotEmpty()) return "$prefix §c초대할 대상이 소속된 섬이 없거나, 대상의 섬에 다른 섬원이 없어야합니다.".toPlayer(p)
        invitePlayer[tPlayer] = p
        inviteTimer[tPlayer] = Bukkit.getScheduler().runTaskLater(Main.pl, Runnable {
            "$prefix §c초대가 만료되었습니다.".toPlayer(p)
            "$prefix §c초대가 만료되었습니다.".toPlayer(tPlayer)
            invitePlayer.remove(tPlayer)
            inviteTimer.remove(tPlayer)
        }, (20 * 120).toLong())
        "$prefix §e${tPlayer.name} §f님에게 초대를 보냈습니다.".toPlayer(p)
        "$prefix §e ${p.name} §f님으로 부터 섬 초대가 왔습니다.".toPlayer(tPlayer)
        "§a( 수락 - /is accept )".toPlayer(tPlayer)
        "§c( 거절 - /is reject )".toPlayer(tPlayer)
    }

    fun inviteAccept(p: Player) {
        if (!invitePlayer.containsKey(p) || !inviteTimer.containsKey(p)) return "$prefix §c대기 중인 초대가 없습니다.".toPlayer(p)
        inviteTimer[p]!!.cancel()
        inviteTimer.remove(p)
        val tPlayer: Player = invitePlayer[p]!!
        val pData = PlayerData(p)
        val ivPlayerData = PlayerData(tPlayer)
        val ivIsData = IslandData(ivPlayerData.islandCode)
        pData.islandCode = ivIsData.code
//        ivIsData.addMember(p)
        "$prefix 초대를 수락하여 §e${tPlayer.name} §f님 섬에 가입하였습니다.".toPlayer(p)
        "$prefix 초대를 수락하여 §e${tPlayer.name} §f님 섬에 가입하였습니다.".toPlayer(tPlayer)
        Bukkit.getScheduler().runTask(Main.pl, Runnable { tpMyIsland(p) })
        invitePlayer.remove(p)
    }

    fun inviteReject(p: Player) {
        if (!invitePlayer.containsKey(p) || !inviteTimer.containsKey(p)) return "$prefix §c대기 중인 초대가 없습니다.".toPlayer(p)
        inviteTimer[p]!!.cancel()
        inviteTimer.remove(p)
        val iPlayer: Player = invitePlayer[p]!!
        p.sendMessage("$prefix §c초대를 거절하였습니다.")
        iPlayer.sendMessage(prefix + "§e" + p.name + "§c님이 초대를 거절하었습니다.")
        invitePlayer.remove(p)
    }

    fun kickMember(p: Player, name: String) {
        val pData = PlayerData(p).also { if (it.islandCode < 0) return "$prefix §c섬이 없습니다.".toPlayer(p) }
        val isData = IslandData(pData.islandCode)
        val playerUUID: String = pData.uuid.toString()
        val targetUUID = Bukkit.getOfflinePlayer(name).uniqueId.toString()
        if (isData.leaderUUID != playerUUID) return "$prefix §c섬장만 사용 가능합니다.".toPlayer(p)
        if (isData.leaderUUID != targetUUID) return "$prefix §c섬장은 추방할 수 없습니다.".toPlayer(p)
        if (!isData.member.contains(targetUUID)) return "$prefix §c섬원이 아닙니다.".toPlayer(p)
        val targetPlayerData = PlayerData(UUID.fromString(playerUUID))
        val target = Bukkit.getPlayer(UUID.fromString(targetUUID))
        if (target != null) {
            targetPlayerData.islandCode = -1
            "$prefix §c섬에서 추방되었습니다.".toPlayer(target)
        } else targetPlayerData.islandCode = -999
//        isData.removeMember(targetUUID)
        for (s in isData.member) {
            val pp: Player? = Bukkit.getPlayer(UUID.fromString(s))
            if (pp != null) "$prefix §e${p.name} §f님이 섬에서 §c추방§f 되었습니다.".toPlayer(pp)
        }
    }

    fun kickPlayerJoin(p: Player) {
        val pData = PlayerData(p)
        if (pData.islandCode <= -999) {
            pData.islandCode = -1
            "$prefix §c섬에서 추방되었습니다.".toPlayer(p)
        }
    }

    fun leaveIsland(p: Player) {
        val pData = PlayerData(p)
        if (pData.islandCode < 0) "$prefix §c섬이 없습니다.".toPlayer(p)
        val isData = IslandData(pData.islandCode)
        if (p.uniqueId.toString() == isData.leaderUUID) return "$prefix §c섬장은 섬에 다른 멤버가 있으면 나갈 수 없습니다.".toPlayer(p)
        pData.islandCode = -1
//        isData.removeMember(p)
        for (s in isData.member) {
            val pp: Player? = Bukkit.getPlayer(s)
            if (pp != null) "$prefix §e${p.name} §f님이 섬에서 나갔습니다.".toPlayer(pp)
        }
        "$prefix 섬에서 나갔습니다.".toPlayer(p)
    }

    fun getMembers(p: Player) {
        val pData = PlayerData(p)
        if (pData.islandCode < 0) return "$prefix §c섬이 없습니다.".toPlayer(p)
        val isData = IslandData(pData.islandCode)
        "§e::섬 멤버 리스트::".toPlayer(p)
        " ".toPlayer(p)
        for (s in isData.member) {
            if (s == isData.leaderUUID) "${Bukkit.getOfflinePlayer(s).name} - 섬장".toPlayer(p)
            else Bukkit.getOfflinePlayer(s).name!!.toPlayer(p)
        }
    }

    fun getMembers(p: Player, name: String) {
        val tPlayer: Player = Bukkit.getPlayer(name) ?: return "$prefix §c존재하지 않는 플레이어 이거나, 접속 중이지 않은 플레이어입니다.".toPlayer(p)
        val tData = PlayerData(tPlayer)
        if (tData.islandCode < 0) return "$prefix §c섬이 없습니다.".toPlayer(p)
        val isData = IslandData(tData.islandCode)
        "§e::섬 멤버 리스트::".toPlayer(p)
        " ".toPlayer(p)
        for (s in isData.member) {
            if (s == isData.leaderUUID) "${Bukkit.getOfflinePlayer(s).name} - 섬장".toPlayer(p)
            else Bukkit.getOfflinePlayer(s).name!!.toPlayer(p)
        }
    }

    fun toggleChat(p: Player) {
        val pData = PlayerData(p)
        if (pData.farmChat) {
            pData.farmChat = false
            return "$prefix §c섬 채팅을 종료하였습니다.".toPlayer(p)
        }
        pData.farmChat = true
        return "$prefix §a섬 채팅을 시작하였습니다.".toPlayer(p)
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
        val pos1: Location = islandData.getPos1()
        val pos2: Location = islandData.getPos2()
        val x1 = intArrayOf(pos1.blockX, pos2.blockX)
        val z1 = intArrayOf(pos1.blockZ, pos2.blockZ)
        if (x1[0] <= loc.blockX && loc.blockX <= x1[1]) if (z1[0] <= loc.blockZ && loc.blockZ <= z1[1]) b1 = true
        val center: Location = islandData.getCenter()
        val radius: Int = islandData.getRadius()
        val rPos1 = center.clone().add(-radius.toDouble(), 0.0, -radius.toDouble())
        val rPos2 = center.clone().add(radius.toDouble(), 0.0, radius.toDouble())
        rPos1.y = 0.0
        rPos2.y = 255.0
        val x2 = intArrayOf(rPos1.blockX, rPos2.blockX)
        val z2 = intArrayOf(rPos1.blockZ, rPos2.blockZ)
        if (x2[0] <= loc.blockX && loc.blockX <= x2[1]) if (z2[0] <= loc.blockZ && loc.blockZ <= z2[1]) b2 = true
        return booleanArrayOf(b1, b2)
    }

    fun resetIslandRanking() {
        val folder = File(Main.pl.dataFolder.toString() + "/island")
        val files: Array<File> = folder.listFiles()
        for (file in files) {
            val isData = IslandData(file.getName().replace(".yml", "").toInt())
            ranking[isData.code] = isData.islandLevel
        }
        rankingList = ArrayList<Map.Entry<Int?, Int?>>(ranking.entries)
        Collections.sort<Map.Entry<Int, Int>>(rankingList, Comparator<Map.Entry<Int?, Int>> { (_, value), (_, value1) -> value1.compareTo(value) })
        for ((key, value) in rankingList) {
            println(key.toString() + " : " + value)
        }
    }

    fun getRanking(islandCode: Int): Int {
        for (entry in rankingList!!) {
            if (entry.key == islandCode) return rankingList!!.indexOf(entry)
        }
        return -1
    }

    fun getRankingItem(ranking: Int): SlotItem? {
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
        if (rankingList!!.toTypedArray().size <= ranking) return SlotItem(line, slot, CreateItem.vanilla(material, "§9" + (ranking + 1) + " §f위 : §8 없음"))
        val isData = IslandData(rankingList!![ranking].key)
        val lore: MutableList<String> = ArrayList()
        lore.add("§f§7[§a섬 레벨§7] : §9" + isData.getIslandLevel())
        lore.add(" ")
        lore.add("§f§8::섬 멤버::")
        for (uuid in isData.member) {
            val p: OfflinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuid))
            lore.add("§f§8" + p.getName())
        }
        return SlotItem(line, slot, CreateItem.vanilla(material, "§9" + (ranking + 1) + " §f위 : §e" + isData.getIslandName(), lore))
    }

    companion object {
        var rankingList: List<Map.Entry<Int?, Int?>>? = null
    }
}