package kr.kro.minestar.minefarm.commands

import kr.kro.minestar.minefarm.JavaTest
import kr.kro.minestar.minefarm.Main
import kr.kro.minestar.minefarm.functions.IslandClass
import kr.kro.minestar.utility.toPlayer
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File

class IslandCMD : CommandExecutor, TabCompleter {
    private val clazz = IslandClass()
    private val prefix: String = Main.prefix
    private val args0 = listOf("create", "invite", "accept", "reject", "kick", "leave", "member", "chat")
    override fun onCommand(p: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (p !is Player) return false
        if (args.isEmpty()) {
            clazz.tpMyIsland(p)
        }
        if (args.isNotEmpty()) {
            when (args[0]) {
                "test" -> clazz.test(p)
                args0[0] -> clazz.createIsland(p).also { return false }

                args0[1] -> {
                    if (args.size != 2) "$prefix/is invite <PlayerName>".toPlayer(p).also { return false }
                    clazz.inviteIsland(p, args[1]).also { return false }
                }
                args0[2] -> clazz.inviteAccept(p).also { return false }
                args0[3] -> clazz.inviteReject(p).also { return false }
                args0[4] -> {
                    if (args.size != 2) p.sendMessage("$prefix/is kick <PlayerName>").also { return false }
                    clazz.kickMember(p, args[1]).also { return false }
                }
                args0[5] -> clazz.leaveIsland(p).also { return false }
                args0[6] -> {
                    if (args.size == 1) clazz.getMembers(p).also { return false }
                    clazz.getMembers(p, args[1]).also { return false }
                }
                args0[7] -> clazz.toggleChat(p).also { return false }
            }
        }
        return false
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String> {
        val list: MutableList<String> = ArrayList()
        val players: MutableList<String> = ArrayList()
        for (p in Bukkit.getOnlinePlayers()) players.add(p.name)
        if (args.size == 1) for (s in args0) if (s.contains(args[0])) list.add(s)
        if (args.size > 1) when (args[0]) {
            args0[1], args0[6] -> {
                if (args.size == 2) {
                    if (args[1] == "") list.add("<PlayerName>")
                    for (s in players) if (s.contains(args[1])) list.add(s)
                }
            }
            args0[4] -> {
                if (args.size == 2) {
                    val p: Player = sender as Player
                    val playerFile = File(Main.pl.dataFolder.toString() + "/player", p.uniqueId.toString() + ".yml")
                    val playerData = YamlConfiguration.loadConfiguration(playerFile)
                    val islandFile = File(Main.pl.dataFolder.toString() + "/island", playerData.getString("ISLAND") + ".yml")
                    val islandData = YamlConfiguration.loadConfiguration(islandFile)
                    val memberUUID: List<String> = islandData.getStringList("ISLAND_MEMBER")
                    val member: List<String> = ArrayList()
                    for (s in member) if (s.contains(args[1])) list.add(s)
                }
            }
        }
        return list
    }
}