package kr.kro.minestar.minefarm.functions

import kr.kro.minestar.minefarm.Main.Companion.pl
import kr.kro.minestar.minefarm.functions.events.AlwaysEvent
import kr.kro.minestar.minefarm.functions.events.LockEvent
import kr.kro.minestar.minefarm.functions.farm.FarmClass
import kr.kro.minestar.minefarm.functions.farm.FarmControl
import org.bukkit.Bukkit

object EnableClass {

    fun enable() {
        eventEnable()
        worldEnable()
        farmEnable()
        playerEnable()
    }

    private fun eventEnable() {
        Bukkit.getPluginManager().registerEvents(AlwaysEvent, pl)
        Bukkit.getPluginManager().registerEvents(LockEvent, pl)
    }

    private fun worldEnable() {
        WorldClass.farmWorldEnable()
    }

    private fun farmEnable() {
        FarmClass.loadFarms()
        FarmClass.setLastFarm()
        FarmClass.rankingInput()
        FarmClass.resetFarmRanking()
        FarmClass.loadOffset()
        FarmClass.loadRadius()
        FarmControl.loadMaxMember()
    }

    private fun playerEnable() {
        PlayerClass.loadPlayers()
    }

    fun reloadConfig() {
        val data = pl.config

    }
}