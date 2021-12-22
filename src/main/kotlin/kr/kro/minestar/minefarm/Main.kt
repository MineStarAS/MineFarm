package kr.kro.minestar.minefarm

import kr.kro.minestar.minefarm.commands.IslandCMD
import kr.kro.minestar.minefarm.functions.EnableClass
import org.bukkit.World
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    companion object {
        lateinit var pl: Main
        const val prefix = "§f§7[§9MineFarm§7]§f"
        lateinit var islandWorld: World
    }

    override fun onEnable() {
        pl = this
        logger.info("$prefix §aEnable")
        getCommand("is")?.setExecutor(IslandCMD)

        saveResource("config.yml", false)
        saveResource("default.schem", false)
        EnableClass.enable()
    }

    override fun onDisable() {
    }
}