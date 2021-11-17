package kr.kro.minestar.minefarm.functions

import org.bukkit.Bukkit
import org.bukkit.WorldCreator
import org.bukkit.World
import org.bukkit.WorldType
import org.bukkit.Difficulty
import org.bukkit.GameRule

class WorldClass {
    fun createWorld() {
        if (Bukkit.getWorld("island") != null) return
        val wc = WorldCreator("island")
        wc.environment(World.Environment.NORMAL)
        wc.type(WorldType.FLAT)
        wc.generatorSettings("{\"structures\": {\"structures\": {}}, \"layers\": [{\"block\": \"air\", \"height\": 1}], \"biome\":\"plains\"}")
        wc.generateStructures(false)
        wc.createWorld()
        worldSetting(Bukkit.getWorld("island")!!)
    }

    fun worldSetting(world: World) {
        world.difficulty = Difficulty.PEACEFUL
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
        world.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false)
        world.setGameRule(GameRule.DISABLE_ELYTRA_MOVEMENT_CHECK, false)
        world.setGameRule(GameRule.DISABLE_RAIDS, false)
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        world.setGameRule(GameRule.DO_ENTITY_DROPS, true)
        world.setGameRule(GameRule.DO_FIRE_TICK, false)
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
        world.setGameRule(GameRule.DO_INSOMNIA, false)
        world.setGameRule(GameRule.DO_LIMITED_CRAFTING, false)
        world.setGameRule(GameRule.DO_MOB_LOOT, true)
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false)
        world.setGameRule(GameRule.DO_PATROL_SPAWNING, false)
        world.setGameRule(GameRule.DO_TILE_DROPS, true)
        world.setGameRule(GameRule.DO_TRADER_SPAWNING, false)
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        world.setGameRule(GameRule.DROWNING_DAMAGE, false)
        world.setGameRule(GameRule.FALL_DAMAGE, false)
        world.setGameRule(GameRule.FIRE_DAMAGE, false)
        world.setGameRule(GameRule.FORGIVE_DEAD_PLAYERS, false)
        world.setGameRule(GameRule.FREEZE_DAMAGE, false)
        world.setGameRule(GameRule.KEEP_INVENTORY, true)
        world.setGameRule(GameRule.LOG_ADMIN_COMMANDS, false)
        world.setGameRule(GameRule.MAX_COMMAND_CHAIN_LENGTH, 65536)
        world.setGameRule(GameRule.MAX_ENTITY_CRAMMING, 24)
        world.setGameRule(GameRule.MOB_GRIEFING, false)
        world.setGameRule(GameRule.NATURAL_REGENERATION, true)
        world.setGameRule(GameRule.PLAYERS_SLEEPING_PERCENTAGE, 100)
        world.setGameRule(GameRule.RANDOM_TICK_SPEED, 3)
        world.setGameRule(GameRule.REDUCED_DEBUG_INFO, false)
        world.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, true)
        world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false)
        world.setGameRule(GameRule.SPAWN_RADIUS, 0)
        world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, true)
        world.setGameRule(GameRule.UNIVERSAL_ANGER, false)
        world.time = 6000
    }
}