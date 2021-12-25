package kr.kro.minestar.minefarm.functions

import kr.kro.minestar.minefarm.Main.Companion.pl
import kr.kro.minestar.minefarm.functions.events.AlwaysEvent
import kr.kro.minestar.minefarm.functions.events.LockEvent
import kr.kro.minestar.minefarm.functions.farm.FarmClass
import kr.kro.minestar.minefarm.functions.farm.FarmControl
import kr.kro.minestar.minefarm.functions.farm.FarmRank
import org.bukkit.Bukkit

object EnableClass {

    fun enable() {
        eventEnable()
        worldEnable()
        farmEnable()
        farmRankEnable()
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
        FarmClass.loadOffset()
        FarmClass.loadRadius()
        FarmControl.loadMaxMember()
    }

    private fun farmRankEnable(){
        FarmRank.init()
        FarmRank.rankingInput()
        FarmRank.resetFarmRanking()
    }

    private fun playerEnable() {
        PlayerClass.loadPlayers()
    }

    fun reloadConfig() {
        val data = pl.config

    }
}