package kr.kro.minestar.minefarm.functions.island

import kr.kro.minestar.minefarm.Main
import kr.kro.minestar.minefarm.Main.Companion.prefix
import kr.kro.minestar.minefarm.data.PlayerData
import kr.kro.minestar.minefarm.functions.PlayerClass
import kr.kro.minestar.utility.bool.BooleanScript
import kr.kro.minestar.utility.bool.addScript
import kr.kro.minestar.utility.string.toPlayer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.util.*


object IslandControl {
    private val inviteTimer: MutableMap<Player, BukkitTask> = HashMap()
    private val invitePlayer: MutableMap<Player, Player> = HashMap()

    var maxMember = 5
    fun loadMaxMember() {
        maxMember = Main.pl.config.getInt("maxIslandMember")
    }

    fun inviteIsland(player: Player, name: String): BooleanScript {
        val invPlayer = Bukkit.getPlayer(name) ?: return false.addScript("$prefix §c존재하지 않는 플레이어 이거나, 접속 중이지 않은 플레이어입니다.")
        val invPlayerData = PlayerClass.playerData[invPlayer]!!
        if (invPlayer == player) return false.addScript("$prefix §c자신을 초대할 수 없습니다.")
        if (invitePlayer.containsKey(invPlayer)) return false.addScript("$prefix §c대기 중인 초대가 있습니다.")
        if (inviteTimer.containsKey(invPlayer)) return false.addScript("$prefix §c대기 중인 초대가 있습니다.")

        val island = PlayerClass.playerIsland[player]
        island ?: return false.addScript("$prefix §c섬이 없습니다.")
        if (island.member().size >= maxMember) return false.addScript("$prefix §c섬 인원이 가득 차 있습니다.")
        if (island.member().contains(invPlayer.uniqueId.toString())) return false.addScript("$prefix §c이미 섬원입니다.")
        if (invPlayerData.farmCode() != null) return false.addScript("$prefix §c초대할 대상이 소속된 섬이 없어야 합니다.")
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
        val island = PlayerClass.playerIsland[invPlayer] ?: return false.addScript("$prefix §c초대한 플레이어가 섬에 가입되어 있지 않습니다.")

        playerData!!.setFarm(island.code)
        island.addMember(player.uniqueId)
        PlayerClass.playerIsland[player] = island


        "$prefix 초대를 수락하여 §e${invPlayer.name} §f님 섬에 가입하였습니다.".toPlayer(invPlayer)
        PlayerClass.tpMyIsland(player)
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
        val island = PlayerClass.playerIsland[player] ?: return false.addScript("$prefix §c섬이 없습니다.")
        val playerUUID = player.uniqueId
        val targetUUID = target.uniqueId

        if (island.leaderUUID() != playerUUID.toString()) return false.addScript("$prefix §c섬장만 사용 가능합니다.")
        if (island.leaderUUID() == targetUUID.toString()) return false.addScript("$prefix §c섬장은 추방할 수 없습니다.")
        if (!island.member().contains(targetUUID.toString())) return false.addScript("$prefix §c섬원이 아닙니다.")

        island.removeMember(targetUUID)
        targetData.setFarm(null)
        PlayerClass.playerIsland.remove(target)

        for (s in island.member()) {
            if (s == player.uniqueId.toString()) continue
            val member = Bukkit.getPlayer(UUID.fromString(s))
            if (member != null) "$prefix §e${target.name} §f님이 섬에서 §c추방§f 되었습니다.".toPlayer(member)
        }
        if (target.player != null) "$prefix §c섬에서 추방되었습니다.".toPlayer(target.player!!)
        return true.addScript("$prefix §e${target.name} §f님이 섬에서 §c추방§f 되었습니다.")
    }

    fun leaveIsland(player: Player): BooleanScript {
        val playerData = PlayerClass.playerData[player].also {
            if (it == null) {
                PlayerClass.kickNullPlayerData(player)
                return false.addScript("$prefix §c플레이어 데이터가 누락 됐습니다.")
            }
        }
        val island = PlayerClass.playerIsland[player]
        island ?: return false.addScript("$prefix §c섬이 없습니다.")
        if (player.uniqueId.toString() == island.leaderUUID()) return false.addScript("$prefix §c섬장은 섬에서 나갈 수 없습니다.")
        island.removeMember(player.uniqueId)
        playerData!!.setFarm(null)
        for (s in island.member()) {
            val member = Bukkit.getPlayer(s)
            member ?: continue
            "$prefix §e${player.name} §f님이 섬에서 나갔습니다.".toPlayer(member)
        }
        return true.addScript("$prefix §c섬에서 나갔습니다.")
    }

    fun getMembers(player: Player) {
        val island = PlayerClass.playerIsland[player]
        island ?: return "$prefix §c섬이 없습니다.".toPlayer(player)
        "§e::섬 멤버 리스트::".toPlayer(player)
        " ".toPlayer(player)
        for (s in island.member())
            if (s == island.leaderUUID()) "${Bukkit.getOfflinePlayer(s).name} - 섬장".toPlayer(player)
            else Bukkit.getOfflinePlayer(s).name!!.toPlayer(player)
    }

    fun getMembers(p: Player, name: String) {
        val target = Bukkit.getPlayer(name) ?: return "$prefix §c존재하지 않는 플레이어 이거나, 접속 중이지 않은 플레이어입니다.".toPlayer(p)
        val island = PlayerClass.playerIsland[target]
        island ?: return "$prefix §c섬이 없습니다.".toPlayer(p)
        "§e::섬 멤버 리스트::".toPlayer(p)
        " ".toPlayer(p)
        for (s in island.member()) {
            if (s == island.leaderUUID()) "${Bukkit.getOfflinePlayer(s).name} - 섬장".toPlayer(p)
            else Bukkit.getOfflinePlayer(s).name!!.toPlayer(p)
        }
    }

    fun toggleChat(p: Player) {
        val pData = PlayerData(p)
        return if (pData.toggleChat()) "$prefix §a섬 채팅을 시작하였습니다.".toPlayer(p)
        else "$prefix §c섬 채팅을 종료하였습니다.".toPlayer(p)
    }
}