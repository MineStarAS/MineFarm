package kr.kro.minestar.minefarm.functions.events

import kr.kro.minestar.minefarm.Main.Companion.farmWorld
import kr.kro.minestar.minefarm.data.PlayerData
import kr.kro.minestar.minefarm.functions.PlayerClass
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerJoinEvent

object AlwaysEvent : Listener {

    @EventHandler
    fun serverJoin(e: PlayerJoinEvent) = PlayerData(e.player)

    @EventHandler
    fun voidDamage(e: EntityDamageEvent) {
        val p = e.entity
        if (p !is Player) return
        if (e.cause != EntityDamageEvent.DamageCause.VOID) return
        if (p.world != farmWorld) return
        if (!PlayerClass.tpMyFarm(p)) p.teleport(Bukkit.getWorlds().first().spawnLocation)
        e.damage = 0.0
    }
}