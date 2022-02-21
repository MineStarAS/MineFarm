package kr.kro.minestar.minefarm

import kr.kro.minestar.minefarm.commands.FarmCMD
import kr.kro.minestar.minefarm.functions.EnableClass
import kr.kro.minestar.minefarm.functions.WorldClass
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class Main : JavaPlugin() {
    companion object {
        lateinit var pl: Main
        const val prefix = "§f§7[§9MineFarm§7]§f"
        lateinit var farmWorld: World
    }

    override fun onEnable() {
        pl = this
        logger.info("$prefix §aEnable")
        getCommand("is")?.setExecutor(FarmCMD)
        saveResource("config.yml", false)
        EnableClass.enable()
        WorldClass.worldSetting(farmWorld)
    }

    override fun onDisable() {
    }
}