package kr.kro.minestar.minefarm

import kr.kro.minestar.minefarm.commands.IslandCMD
import kr.kro.minestar.minefarm.events.AlwaysEvent
import kr.kro.minestar.minefarm.functions.IslandClass
import kr.kro.minestar.minefarm.functions.WorldClass
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    companion object {
        lateinit var pl: Main
        const val prefix = "§f§7[§9MineFarm§7]§f"
    }

    override fun onEnable() {
        pl = this
        logger.info("$prefix §aEnable")
        getCommand("is")?.setExecutor(IslandCMD())

        Bukkit.getPluginManager().registerEvents(AlwaysEvent(), this)

        WorldClass().createWorld()
        WorldClass().worldSetting(Bukkit.getWorld("world")!!)
        for (p in Bukkit.getOnlinePlayers()) if (p.gameMode != GameMode.CREATIVE) {
            p.allowFlight = false
            p.isFlying = false
        }
        IslandClass().resetIslandRanking()
    }

    override fun onDisable() {
    }
}