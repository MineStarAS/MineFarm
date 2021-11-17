@file:Suppress("DEPRECATION")

package kr.kro.minestar.minefarm.events

import kr.kro.minestar.minefarm.Main
import kr.kro.minestar.minefarm.data.IslandData
import kr.kro.minestar.minefarm.data.PlayerData
import kr.kro.minestar.minefarm.functions.DataClass
import kr.kro.minestar.minefarm.functions.IslandClass
import kr.kro.minestar.utility.toPlayer
import kr.kro.minestar.utility.toServer
import net.kyori.adventure.text.Component
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.Inventory
import java.util.*

class AlwaysEvent : Listener {

    val prefix = Main.prefix

    @EventHandler
    fun serverJoin(e: PlayerJoinEvent) {
        val p = e.player
        if (!DataClass().createPlayerData(p)) firstJoin(p) //first join
        e.joinMessage(Component.text("§f[§a접속§f] §e" + p.name))
        IslandClass().kickPlayerJoin(p)
    }

    fun firstJoin(p: Player) {
        val inv: Inventory = p.inventory
        inv.clear()
        p.gameMode = GameMode.ADVENTURE
//        WarpClass().warp(p, "tutorial")
//        VirtualChestClass().createVirtualChest(p)
//        DataClass().createLevelData(p)
        "$prefix §e${p.name} §d님이 처음으로 접속하였습니다.".toServer()
    }

    @EventHandler
    fun chat(e: PlayerChatEvent) {
        val p = e.player
        val pData = PlayerData(p)
        var msg = e.message.replace("&", "§")
        val tcName = TextComponent("§e${p.name}")
        tcName.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("§e" + p.name))
        tcName.clickEvent = ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/m " + p.name)
        var tcMsg = TextComponent(" §f: $msg")
        e.isCancelled = true
        if (pData.farmChat) { //팜채팅
            if (pData.islandCode < 0) return "$prefix §c팜이 없습니다.".toPlayer(p)
            val isData = IslandData(pData.islandCode)
            val member: List<String> = isData.member
            val players: MutableList<Player> = ArrayList()
            for (s in member) {
                val pp = Bukkit.getPlayer(UUID.fromString(s))
                if (pp != null) players.add(pp)
            }
            val tcFarmChat = TextComponent("§a[팜] §f")
            val text = arrayOf(tcFarmChat, tcName, tcMsg)
            for (pp in players) pp.spigot().sendMessage(*text)
            return
        }
        if (msg[0] == '!') { //전체채팅
            val arr = msg.toCharArray()
            arr[0] = ' '
            msg = String(arr)
            tcMsg = TextComponent(" §f:$msg")
            val prefix = TextComponent("§a[전체] ")
            val text = arrayOf(prefix, tcName, tcMsg)
            Bukkit.broadcast(*text)
            return
        }
        val prefix = TextComponent("§8[지역] ")
        val text = arrayOf(prefix, tcName, tcMsg)
        val players = p.location.getNearbyPlayers(200.0)
        if (players.isEmpty()) "$prefix §c근처에 다른 플레이어가 없습니다.".toPlayer(p)
        else for (pp in players) pp.spigot().sendMessage(*text)
    }

}