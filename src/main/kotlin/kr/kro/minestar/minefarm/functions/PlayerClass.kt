package kr.kro.minestar.minefarm.functions

import kr.kro.minestar.minefarm.Main.Companion.prefix
import kr.kro.minestar.minefarm.data.Farm
import kr.kro.minestar.minefarm.data.PlayerData
import kr.kro.minestar.utility.string.toPlayer
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object PlayerClass {
    val playerData: HashMap<Player, PlayerData> = hashMapOf()
    val playerFarm: HashMap<Player, Farm> = hashMapOf()

    fun loadPlayers() {
        for (player in Bukkit.getOnlinePlayers()) PlayerData(player)
    }

    fun tpMyFarm(player: Player): Boolean {
        val farm = playerFarm[player]
        farm ?: "$prefix §c섬이 없습니다.".toPlayer(player).also { return false }
        return player.teleport(farm!!.spawn())
    }

    fun kickNullPlayerData(player: Player) = player.kickPlayer("$prefix 플레이어 데이터에 문제가 생겨 재접속 해주시기 바랍니다.")
}