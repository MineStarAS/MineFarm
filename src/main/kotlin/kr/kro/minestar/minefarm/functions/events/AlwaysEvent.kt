@file:Suppress("DEPRECATION")

package kr.kro.minestar.minefarm.functions.events

import kr.kro.minestar.minefarm.Main.Companion.farmWorld
import kr.kro.minestar.minefarm.Main.Companion.prefix
import kr.kro.minestar.minefarm.data.PlayerData
import kr.kro.minestar.minefarm.functions.PlayerClass
import kr.kro.minestar.utility.string.toPlayer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import java.util.*

object AlwaysEvent : Listener {

    @EventHandler
    fun serverJoin(e: PlayerJoinEvent) = PlayerData(e.player)


    @EventHandler
    fun chat(e: PlayerChatEvent) {
        if (e.isCancelled) return
        val player = e.player
        val playerData = PlayerClass.playerData[player] ?: return PlayerClass.kickNullPlayerData(player)
        if (!playerData.farmChat()) return
        e.isCancelled = true
        val isLand = PlayerClass.playerFarm[player] ?: return "$prefix §c팜이 없습니다.".toPlayer(player)

        val msg = "§f§a[팜채팅] ${player.name} : ${e.message}"

        val member: List<String> = isLand.member()
        for (uuid in member) {
            val p = Bukkit.getPlayer(UUID.fromString(uuid)) ?: continue
            msg.toPlayer(p)
        }
    }

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