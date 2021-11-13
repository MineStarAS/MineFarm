package kr.kro.minestar.pack

import kr.kro.minestar.pack.data.IslandData
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    companion object {
        lateinit var pl: Main
        const val prefix = "§f§7[MineFarm]§f"
    }

    override fun onEnable() {
        pl = this
        logger.info("$prefix §aEnable")
        getCommand("cmd")?.setExecutor(CMD())
    }

    override fun onDisable() {
    }
}