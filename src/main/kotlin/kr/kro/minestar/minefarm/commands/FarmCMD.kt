package kr.kro.minestar.minefarm.commands

import kr.kro.minestar.minefarm.Main.Companion.prefix
import kr.kro.minestar.minefarm.functions.PlayerClass
import kr.kro.minestar.minefarm.functions.farm.FarmClass
import kr.kro.minestar.minefarm.functions.farm.FarmControl
import kr.kro.minestar.utility.string.toPlayer
import kr.kro.minestar.utility.string.toServer
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.ArrayList

object FarmCMD : CommandExecutor, TabCompleter {
    private val args0 = listOf("create", "invite", "accept", "reject", "kick", "leave", "member", "chat", "givehost", "reset")
    override fun onCommand(player: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (player !is Player) return false
        if (args.isEmpty()) {
            PlayerClass.tpMyFarm(player)
            return false
        }
        when (args[0]) {
            "test" -> {
                FarmClass.offset.toString().toServer()
            }
            args0[0] -> FarmClass.createFarm(player).script.toPlayer(player)

            args0[1] -> {
                if (args.size != 2) "$prefix §c/is ${args.first()} <PlayerName>".toPlayer(player).also { return false }
                FarmControl.inviteFarm(player, args[1]).script.toPlayer(player)
            }
            args0[2] -> FarmControl.inviteAccept(player).script.toPlayer(player)
            args0[3] -> FarmControl.inviteReject(player).script.toPlayer(player)
            args0[4] -> {
                if (args.size != 2) "$prefix §c/is ${args.first()} <PlayerName>".toPlayer(player).also { return false }
                FarmControl.kickMember(player, args[1]).script.toPlayer(player)
            }
            args0[5] -> FarmControl.leaveFarm(player).script.toPlayer(player)
            args0[6] -> {
                if (args.size == 1) FarmControl.getMembers(player).also { return false }
                FarmControl.getMembers(player, args[1])
            }
            args0[7] -> PlayerClass.toggleChat(player)

            args0[8] -> {
                if (args.size != 2) "$prefix §c/is ${args.first()} <PlayerName>".toPlayer(player).also { return false }
                FarmControl.giveHost(player, args.last()).script.toPlayer(player)
            }
            args0[9] -> FarmControl.farmReSet(player).script.toPlayer(player)
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

            args0[4], args0[8] -> {
                if (args.size != 2) return list
                val memberList: MutableList<String> = mutableListOf()
                val uuidList = PlayerClass.playerFarm[player]?.member() ?: return list

                for (uuid in uuidList) {
                    val name = Bukkit.getOfflinePlayer(UUID.fromString(uuid)).name ?: continue
                    memberList.add(name)
                }
                for (member in memberList) if (member.contains(args.last())) list.add(member)
            }
        }
        return list
    }
}