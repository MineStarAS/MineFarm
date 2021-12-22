package kr.kro.minestar.minefarm.functions

import kr.kro.minestar.minefarm.Main.Companion.prefix
import kr.kro.minestar.minefarm.data.Island
import kr.kro.minestar.minefarm.data.PlayerData
import kr.kro.minestar.utility.string.toPlayer
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object PlayerClass {
    val playerData: HashMap<Player, PlayerData> = hashMapOf()
    val playerIsland: HashMap<Player, Island> = hashMapOf()

    fun loadPlayers() {
        for (player in Bukkit.getOnlinePlayers()) PlayerData(player)
    }

    fun tpMyIsland(player: Player): Boolean {
        val island = playerIsland[player]
        island ?: "$prefix §c섬이 없습니다.".toPlayer(player).also { return false }
        return player.teleport(island!!.spawn())
    }

    fun toggleChat(player: Player) {
        val playerData = playerData[player] ?: return kickNullPlayerData(player)
        return if (playerData.toggleChat()) "$prefix §a섬 채팅을 시작하였습니다.".toPlayer(player)
        else "$prefix §c섬 채팅을 종료하였습니다.".toPlayer(player)
    }

    fun kickNullPlayerData(player: Player) = player.kickPlayer("$prefix 플레이어 데이터에 문제가 생겨 재접속 해주시기 바랍니다.")
}