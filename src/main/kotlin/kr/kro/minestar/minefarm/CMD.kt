package kr.kro.minestar.minefarm

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class CMD : CommandExecutor, TabCompleter {
    private val args0 = listOf("cmd1", "cmd2", "cmd3")
    override fun onCommand(p: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (p !is Player) return false
        if (args.isEmpty()) {
            p.sendMessage("null")
        } else {
            when (args[0]) {
                "cmd1" -> TODO()
                "cmd2" -> TODO()
                "cmd3" -> TODO()
            }
        }
        return false
    }

    override fun onTabComplete(p: CommandSender, cmd: Command, alias: String, args: Array<out String>): MutableList<String> {
        val list = mutableListOf<String>()
        if (args.size == 1) for (s in args0) if (s.contains(args[0])) list.add(s)
        if (args.size > 1) when (args[0]) {
            "cmd1" -> TODO()
            "cmd2" -> TODO()
            "cmd3" -> TODO()
        }

        return list
    }

}