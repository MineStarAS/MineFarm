package kr.kro.minestar.minefarm.commands

import kr.kro.minestar.minefarm.Main.Companion.prefix
import kr.kro.minestar.minefarm.functions.PlayerClass
import kr.kro.minestar.minefarm.functions.farm.FarmClass
import kr.kro.minestar.minefarm.functions.farm.FarmControl
import kr.kro.minestar.minefarm.functions.farm.FarmRank
import kr.kro.minestar.utility.string.toPlayer
import kr.kro.minestar.utility.string.toServer
import kr.kro.minestar.utility.unit.setFalse
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.*

object FarmCMD : CommandExecutor, TabCompleter {
    private enum class Arg { create, invite, accept, reject, kick, leave, member, chat, givehost, reset, lock, rank }

    private val lock = listOf("pvp", "button", "plate", "door", "trapdoor", "fencegate")
    override fun onCommand(player: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (player !is Player) return false
        if (args.isEmpty()) {
            PlayerClass.tpMyFarm(player)
            return false
        }
        when (args.first()) {
            "test" -> {
                FarmClass.offset.toString().toServer()
            }
            Arg.create.name -> FarmClass.createFarm(player).script.toPlayer(player)
            Arg.invite.name -> {
                if (args.size != 2) return "$prefix §c/is ${args.first()} <PlayerName>".toPlayer(player).setFalse()
                FarmControl.inviteFarm(player, args[1]).script.toPlayer(player)
            }
            Arg.accept.name -> FarmControl.inviteAccept(player).script.toPlayer(player)
            Arg.reject.name -> FarmControl.inviteReject(player).script.toPlayer(player)
            Arg.kick.name -> {
                if (args.size != 2) return "$prefix §c/is ${args.first()} <PlayerName>".toPlayer(player).setFalse()
                FarmControl.kickMember(player, args[1]).script.toPlayer(player)
            }
            Arg.leave.name -> FarmControl.leaveFarm(player).script.toPlayer(player)
            Arg.member.name -> {
                if (args.size == 1) return FarmControl.getMembers(player).setFalse()
                FarmControl.getMembers(player, args[1])
            }
            Arg.chat.name -> PlayerClass.toggleChat(player)
            Arg.givehost.name -> {
                if (args.size != 2) return "$prefix §c/is ${args.first()} <PlayerName>".toPlayer(player).setFalse()
                FarmControl.giveHost(player, args.last()).script.toPlayer(player)
            }
            Arg.reset.name -> FarmControl.farmReSet(player).script.toPlayer(player)
            Arg.lock.name -> {
                if (args.size != 2) return "$prefix §c/is ${args.first()} <LockOption>".toPlayer(player).setFalse()
                FarmControl.farmSetLock(player, args.last()).script.toPlayer(player)
            }
            Arg.rank.name -> FarmRank.openGUI(player)
        }
        return false
    }

    override fun onTabComplete(player: CommandSender, command: Command, alias: String, args: Array<String>): List<String> {
        if (player !is Player) return listOf()
        val list: MutableList<String> = ArrayList()
        val players: MutableList<String> = ArrayList()
        val arg = mutableListOf<String>()
        for (v in enumValues<Arg>()) arg.add(v.name)
        for (p in Bukkit.getOnlinePlayers()) players.add(p.name)
        if (args.size == 1) for (s in arg) if (s.contains(args.last())) list.add(s)
        if (args.size > 1) when (args.first()) {
            Arg.invite.name, Arg.member.name -> if (args.size == 2) for (s in players) if (s.contains(args.last())) list.add(s)
            Arg.kick.name, Arg.givehost.name -> {
                if (args.size != 2) return list
                val memberList: MutableList<String> = mutableListOf()
                val uuidList = PlayerClass.playerFarm[player]?.member() ?: return list

                for (uuid in uuidList) {
                    val name = Bukkit.getOfflinePlayer(UUID.fromString(uuid)).name ?: continue
                    memberList.add(name)
                }
                for (member in memberList) if (member.contains(args.last())) list.add(member)
            }
            Arg.lock.name -> if (args.size == 2) for (s in lock) if (s.contains(args.last())) list.add(s)
        }
        return list
    }
}