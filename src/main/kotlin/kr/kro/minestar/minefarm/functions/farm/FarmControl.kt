package kr.kro.minestar.minefarm.functions.farm

import kr.kro.minestar.minefarm.Main
import kr.kro.minestar.minefarm.Main.Companion.pl
import kr.kro.minestar.minefarm.Main.Companion.prefix
import kr.kro.minestar.minefarm.data.Lock
import kr.kro.minestar.minefarm.data.PlayerData
import kr.kro.minestar.minefarm.functions.PlayerClass
import kr.kro.minestar.utility.bool.BooleanScript
import kr.kro.minestar.utility.bool.addScript
import kr.kro.minestar.utility.string.toPlayer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.util.*


object FarmControl {
    private val inviteTimer: MutableMap<Player, BukkitTask> = HashMap()
    private val invitePlayer: MutableMap<Player, Player> = HashMap()

    private val reAsk = hashMapOf<Player, BukkitTask>()

    var maxMember = 5
    fun loadMaxMember() {
        maxMember = Main.pl.config.getInt("maxFarmMember")
    }

    fun inviteFarm(player: Player, name: String): BooleanScript {
        val invPlayer = Bukkit.getPlayer(name) ?: return false.addScript("$prefix §c존재하지 않는 플레이어 이거나, 접속 중이지 않은 플레이어입니다.")
        val invPlayerData = PlayerClass.playerData[invPlayer]!!
        if (invPlayer == player) return false.addScript("$prefix §c자신을 초대할 수 없습니다.")
        if (invitePlayer.containsKey(invPlayer)) return false.addScript("$prefix §e${invPlayer.name} §c님에게 이미 대기 중인 초대가 있습니다.")
        if (inviteTimer.containsKey(invPlayer)) return false.addScript("$prefix §e${invPlayer.name} §c님에게 이미 대기 중인 초대가 있습니다.")
        if (invPlayerData.farmCode() != null) return false.addScript("$prefix §c해당 플레이어가 섬에 가입되어 있지 않아야 합니다.")

        val farm = PlayerClass.playerFarm[player]
        farm ?: return false.addScript("$prefix §c섬이 없습니다.")
        if (farm.member().size >= maxMember) return false.addScript("$prefix §c섬 인원이 가득 차 있습니다.")
        if (farm.member().contains(invPlayer.uniqueId.toString())) return false.addScript("$prefix §c이미 섬원입니다.")
        invitePlayer[invPlayer] = player
        inviteTimer[invPlayer] = Bukkit.getScheduler().runTaskLater(Main.pl, Runnable {
            "$prefix §c초대가 만료되었습니다.".toPlayer(player)
            "$prefix §c초대가 만료되었습니다.".toPlayer(invPlayer)
            invitePlayer.remove(invPlayer)
            inviteTimer.remove(invPlayer)
        }, 20 * 120)
        "$prefix §e ${player.name} §f님으로부터 섬 초대가 왔습니다.".toPlayer(invPlayer)
        "§a( 수락 - /is accept )".toPlayer(invPlayer)
        "§c( 거절 - /is reject )".toPlayer(invPlayer)
        return true.addScript("$prefix §e${invPlayer.name} §f님에게 초대를 보냈습니다.")
    }

    fun inviteAccept(player: Player): BooleanScript {
        if (!invitePlayer.containsKey(player)) return false.addScript("$prefix §c대기 중인 초대가 없습니다.")
        if (!inviteTimer.containsKey(player)) return false.addScript("$prefix §c대기 중인 초대가 없습니다.")
        inviteTimer[player]?.cancel()
        inviteTimer.remove(player)

        val invPlayer = invitePlayer[player] ?: return false.addScript("$prefix §c초대한 플레이어가 서버에 접속해 있지 않습니다.")
        val playerData = PlayerClass.playerData[player].also {
            if (it == null) {
                PlayerClass.kickNullPlayerData(player)
                return false.addScript("$prefix §c플레이어 데이터가 누락 됐습니다.")
            }
        }
        val farm = PlayerClass.playerFarm[invPlayer] ?: return false.addScript("$prefix §c초대한 플레이어가 섬에 가입되어 있지 않습니다.")

        playerData!!.setFarm(farm.code)
        farm.addMember(player.uniqueId)
        PlayerClass.playerFarm[player] = farm


        "$prefix 초대를 수락하여 §e${invPlayer.name} §f님 섬에 가입하였습니다.".toPlayer(invPlayer)
        PlayerClass.tpMyFarm(player)
        invitePlayer.remove(player)
        return true.addScript("$prefix 초대를 수락하여 §e${invPlayer.name} §f님 섬에 가입하였습니다.")
    }

    fun inviteReject(player: Player): BooleanScript {
        if (!invitePlayer.containsKey(player) || !inviteTimer.containsKey(player)) return false.addScript("$prefix §c대기 중인 초대가 없습니다.")
        inviteTimer[player]!!.cancel()
        inviteTimer.remove(player)
        val iPlayer: Player = invitePlayer[player]!!
        "$prefix §e${player.name} §c님이 초대를 거절하였습니다.".toPlayer(iPlayer)
        invitePlayer.remove(player)
        return true.addScript("$prefix §c초대를 거절하였습니다.")
    }

    fun kickMember(player: Player, name: String): BooleanScript {
        val target = Bukkit.getPlayer(name) ?: Bukkit.getOfflinePlayer(name)
        val targetData = PlayerClass.playerData[target] ?: PlayerData(target)
        val farm = PlayerClass.playerFarm[player] ?: return false.addScript("$prefix §c섬이 없습니다.")
        val playerUUID = player.uniqueId
        val targetUUID = target.uniqueId

        if (farm.leaderUUID() != playerUUID.toString()) return false.addScript("$prefix §c섬장만 사용 가능합니다.")
        if (farm.leaderUUID() == targetUUID.toString()) return false.addScript("$prefix §c섬장은 추방할 수 없습니다.")
        if (!farm.member().contains(targetUUID.toString())) return false.addScript("$prefix §c섬원이 아닙니다.")

        farm.removeMember(targetUUID)
        targetData.setFarm(null)
        PlayerClass.playerFarm.remove(target)

        for (s in farm.member()) {
            if (s == player.uniqueId.toString()) continue
            val member = Bukkit.getPlayer(UUID.fromString(s))
            if (member != null) "$prefix §e${target.name} §f님이 섬에서 §c추방§f 되었습니다.".toPlayer(member)
        }
        if (target.player != null) "$prefix §c섬에서 추방되었습니다.".toPlayer(target.player!!)
        return true.addScript("$prefix §e${target.name} §f님이 섬에서 §c추방§f 되었습니다.")
    }

    fun leaveFarm(player: Player): BooleanScript {
        val playerData = PlayerClass.playerData[player].also {
            if (it == null) {
                PlayerClass.kickNullPlayerData(player)
                return false.addScript("$prefix §c플레이어 데이터가 누락 됐습니다.")
            }
        }
        val farm = PlayerClass.playerFarm[player]
        farm ?: return false.addScript("$prefix §c섬이 없습니다.")
        if (player.uniqueId.toString() == farm.leaderUUID()) {
            if (farm.member().size > 1) return false.addScript("$prefix §c섬장은 섬에 다른 멤버가 없을 경우에만 나갈 수 있습니다.")
            playerData!!.setFarm(null)
            FarmClass.deleteFarm(farm)
            return true.addScript("$prefix §c섬에서 나갔습니다.")
        }
        farm.removeMember(player.uniqueId)
        playerData!!.setFarm(null)
        for (s in farm.member()) {
            val member = Bukkit.getPlayer(s)
            member ?: continue
            "$prefix §e${player.name} §f님이 섬에서 나갔습니다.".toPlayer(member)
        }
        return true.addScript("$prefix §c섬에서 나갔습니다.")
    }

    fun giveHost(player: Player, name: String): BooleanScript {
        val target = Bukkit.getPlayer(name) ?: return false.addScript("$prefix §c존재하지 않는 플레이어 이거나, 접속 중이지 않은 플레이어입니다.")
        val farm = PlayerClass.playerFarm[player]
        farm ?: return false.addScript("$prefix §c섬이 없습니다.")
        val playerUUID = player.uniqueId
        val targetUUID = target.uniqueId

        if (farm.leaderUUID() != playerUUID.toString()) return false.addScript("$prefix §c섬장만 사용 가능합니다.")
        if (farm.leaderUUID() == targetUUID.toString()) return false.addScript("$prefix §c자신에게 넘길 수 없습니다.")
        if (!farm.member().contains(targetUUID.toString())) return false.addScript("$prefix §c섬원이 아닙니다.")
        farm.setLeaderUUID(targetUUID)
        for (uuid in farm.member()) {
            val p = Bukkit.getPlayer(UUID.fromString(uuid)) ?: continue
            if (player != p) "$prefix §e${target.name} §f님이 섬장으로 임명되었습니다.".toPlayer(p)

        }
        return true.addScript("§a정상적으로 §e${target.name} §a님이 섬장으로 임명되었습니다.")
    }

    fun farmReSet(player: Player): BooleanScript {
        val farm = PlayerClass.playerFarm[player]
        farm ?: return false.addScript("$prefix §c섬이 없습니다.")
        if (farm.leaderUUID() != player.uniqueId.toString()) return false.addScript("$prefix §c섬장만 사용 가능합니다.")
        if (!farm.canReset()) return false.addScript("$prefix §c오늘은 이미 초기화 하였습니다.")
        if (!reAsk.containsKey(player)) {
            "$prefix 팜을 초기화 하실려면 명령어를 다시 한 번 사용해 주시기 바랍니다.".toPlayer(player)
            reAsk[player] = Bukkit.getScheduler().runTaskLater(pl, Runnable {
                "$prefix 팜 초기화가 취소되었습니다.".toPlayer(player)
                reAsk.remove(player)
            }, 200)
            return true.addScript("§7명령어 미입력 시, 10 초 후 자동으로 팜 초기화가 취소 됩니다.")
        }
        reAsk[player]!!.cancel()
        reAsk.remove(player)
        farm.setResetTime()
        val center = farm.center()
        val world = center.world
        val x1 = center.blockX - FarmClass.radius
        val x2 = center.blockX + FarmClass.radius
        val z1 = center.blockZ - FarmClass.radius
        val z2 = center.blockZ + FarmClass.radius
        for (y in 1..255) {
            val material: Material = when (y) {
                in 1..56 -> Material.STONE
                in 57..58 -> Material.DIRT
                59 -> Material.GRASS_BLOCK
                else -> Material.AIR
            }
            for (x in x1..x2)
                for (z in z1..z2) {
                    val block = Location(world, x.toDouble(), y.toDouble(), z.toDouble()).block
                    block.type = material
                }
        }
        return true.addScript("$prefix §a정상적으로 초기화 되었습니다.")
    }

    fun farmSetLock(player: Player, lock: Lock): BooleanScript {
        val farm = PlayerClass.playerFarm[player]
        farm ?: return false.addScript("$prefix §c섬이 없습니다.")
        if (farm.leaderUUID() != player.uniqueId.toString()) return false.addScript("$prefix §c섬장만 사용 가능합니다.")
        farm.toggleLock(lock)
        val c = if (farm.getLock(lock)) "§aTRUE"
        else "§cFALSE"
        return true.addScript("$prefix 팜의 §e${lock.ko} §f설정이 $c §f로 설정 되었습니다.")
    }

    fun farmSetLock(player: Player, lockName: String): BooleanScript {
        val farm = PlayerClass.playerFarm[player]
        farm ?: return false.addScript("$prefix §c섬이 없습니다.")
        if (farm.leaderUUID() != player.uniqueId.toString()) return false.addScript("$prefix §c섬장만 사용 가능합니다.")
        val lock: Lock = when (lockName) {
            "pvp" -> Lock.PVP
            "button" -> Lock.BUTTON
            "plate" -> Lock.PRESSURE_PLATE
            "door" -> Lock.DOOR
            "trapdoor" -> Lock.TRAPDOOR
            "fencegate" -> Lock.FENCE_GATE
            else -> return false.addScript("$prefix §c알 수 없는 설정입니다.")
        }
        farm.toggleLock(lock)
        val c = if (farm.getLock(lock)) "§aTRUE"
        else "§cFALSE"
        return true.addScript("$prefix 팜의 §e${lock.ko} §f설정이 $c §f로 설정 되었습니다.")
    }

    fun getMembers(player: Player) {
        val farm = PlayerClass.playerFarm[player]
        farm ?: return "$prefix §c섬이 없습니다.".toPlayer(player)
        "§e::섬 멤버 리스트::".toPlayer(player)
        " ".toPlayer(player)
        "${Bukkit.getOfflinePlayer(UUID.fromString(farm.leaderUUID())).name} - 섬장".toPlayer(player)
        for (s in farm.member()) if (s != farm.leaderUUID()) Bukkit.getOfflinePlayer(UUID.fromString(s)).name!!.toPlayer(player)
    }

    fun getMembers(player: Player, name: String) {
        val target = Bukkit.getPlayer(name) ?: return "$prefix §c존재하지 않는 플레이어 이거나, 접속 중이지 않은 플레이어입니다.".toPlayer(player)
        val farm = PlayerClass.playerFarm[target]
        farm ?: return "$prefix §c섬이 없습니다.".toPlayer(player)
        "§e::섬 멤버 리스트::".toPlayer(player)
        " ".toPlayer(player)
        "${Bukkit.getOfflinePlayer(UUID.fromString(farm.leaderUUID())).name} - 섬장".toPlayer(player)
        for (s in farm.member()) if (s != farm.leaderUUID()) Bukkit.getOfflinePlayer(UUID.fromString(s)).name!!.toPlayer(player)
    }
}