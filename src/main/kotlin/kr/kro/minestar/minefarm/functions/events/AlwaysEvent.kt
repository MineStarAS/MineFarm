package kr.kro.minestar.minefarm.functions.events

import kr.kro.minestar.minefarm.Main.Companion.farmWorld
import kr.kro.minestar.minefarm.data.PlayerData
import kr.kro.minestar.minefarm.functions.PlayerClass
import kr.kro.minestar.utility.string.toServer
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent

object AlwaysEvent : Listener {

    @EventHandler
    fun serverJoin(e: PlayerJoinEvent) = PlayerData(e.player)

    @EventHandler
    fun worldMove(e: PlayerChangedWorldEvent) {
        if (e.player.isOp && e.player.gameMode == GameMode.CREATIVE) return
        val world = e.player.world
        if (world == farmWorld) e.player.gameMode = GameMode.SURVIVAL
        else e.player.gameMode = GameMode.ADVENTURE
    }
}