package kr.kro.minestar.minefarm.commands

import kr.kro.minestar.minefarm.Main.Companion.prefix
import kr.kro.minestar.minefarm.functions.PlayerClass
import kr.kro.minestar.minefarm.functions.island.IslandClass
import kr.kro.minestar.minefarm.functions.island.IslandControl
import kr.kro.minestar.utility.string.toPlayer
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

object IslandCMD : CommandExecutor, TabCompleter {
    private val args0 = listOf("create", "invite", "accept", "reject", "kick", "leave", "member", "chat")
    override fun onCommand(player: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (player !is Player) return false
        if (args.isEmpty()) {
            PlayerClass.tpMyIsland(player)
            return false
        }
        when (args[0]) {
            "test" -> {
                for (int in 1..100) {
                    IslandClass.createIsland(player)
                    IslandControl.leaveIsland(player)
                }
            }
            args0[0] -> IslandClass.createIsland(player).script.toPlayer(player)

            args0[1] -> {
                if (args.size != 2) "$prefix §c/is invite <PlayerName>".toPlayer(player).also { return false }
                IslandControl.inviteIsland(player, args[1]).script.toPlayer(player)
            }
            args0[2] -> IslandControl.inviteAccept(player).script.toPlayer(player)
            args0[3] -> IslandControl.inviteReject(player).script.toPlayer(player)
            args0[4] -> {
                if (args.size != 2) player.sendMessage("$prefix/is kick <PlayerName>").also { return false }
                IslandControl.kickMember(player, args[1]).script.toPlayer(player)
            }
            args0[5] -> IslandControl.leaveIsland(player).script.toPlayer(player)
            args0[6] -> {
                if (args.size == 1) IslandControl.getMembers(player).also { return false }
                IslandControl.getMembers(player, args[1])
            }
            args0[7] -> IslandControl.toggleChat(player)
        }
        return false
    }

    override fun onTabComplete(player: CommandSender, command: Command, alias: String, args: Array<String>): List<String> {
        if (player !is Player) return listOf()
        val list: MutableList<String> = ArrayList()
        val players: MutableList<String> = ArrayList()
        for (p in Bukkit.getOnlinePlayers()) players.add(p.name)
        if (args.size == 1) for (s in args0) if (s.contains(args.last())) list.add(s)
        if (args.size > 1) when (args[0]) {
            args0[1], args0[6] -> if (args.size == 2) for (s in players) if (s.contains(args.last())) list.add(s)

            args0[4] -> {
                if (args.size == 2) {
                    val memberList: MutableList<String> = mutableListOf()
                    val uuidList = PlayerClass.playerIsland[player]?.member() ?: return list
                    for (uuid in uuidList) {
                        val name = Bukkit.getOfflinePlayer(uuid).name ?: continue
                        memberList.add(name)
                    }
                    for (member in memberList) if (member.contains(args.last())) list.add(member)
                }
            }
        }
        return list
    }
}