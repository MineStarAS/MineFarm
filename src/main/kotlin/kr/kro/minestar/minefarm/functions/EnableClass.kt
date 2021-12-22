package kr.kro.minestar.minefarm.functions

import kr.kro.minestar.minefarm.Main.Companion.pl
import kr.kro.minestar.minefarm.functions.events.AlwaysEvent
import kr.kro.minestar.minefarm.functions.events.LockEvent
import kr.kro.minestar.minefarm.functions.island.IslandClass
import kr.kro.minestar.minefarm.functions.island.IslandControl
import org.bukkit.Bukkit

object EnableClass {

    fun enable() {
        eventEnable()
        worldEnable()
        islandEnable()
        playerEnable()
    }

    private fun eventEnable() {
        Bukkit.getPluginManager().registerEvents(AlwaysEvent, pl)
        Bukkit.getPluginManager().registerEvents(LockEvent, pl)
    }

    private fun worldEnable() {
        WorldClass.islandWorldEnable()
    }

    private fun islandEnable() {
        IslandClass.loadIslands()
        IslandClass.setLastIsland()
        IslandClass.rankingInput()
        IslandClass.resetIslandRanking()
        IslandClass.loadOffset()
        IslandControl.loadMaxMember()
    }

    private fun playerEnable() {
        PlayerClass.loadPlayers()
    }

    fun reloadConfig(){
        val data = pl.config

    }
}