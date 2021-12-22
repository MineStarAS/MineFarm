package kr.kro.minestar.minefarm.data

import kr.kro.minestar.minefarm.Main
import kr.kro.minestar.minefarm.Main.Companion.pl
import kr.kro.minestar.minefarm.functions.PlayerClass
import kr.kro.minestar.minefarm.functions.island.IslandClass
import org.bukkit.OfflinePlayer
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PlayerData {
    private val name: String?
    private val uuid: UUID

    private val firstJoinTime: String?
    private val lastJoinTime: String?

    private var farmCode: String?
    private var farmChat: Boolean

    fun name() = name
    fun uuid() = uuid
    fun firstJoinTime() = firstJoinTime
    fun lastJoinTime() = lastJoinTime
    fun farmCode() = farmCode
    fun farmChat() = farmChat

    constructor(player: Player) {
        val file = File("${pl.dataFolder}/player", "${player.uniqueId}.yml")
        val data = YamlConfiguration.loadConfiguration(file)

        if (!file.exists()) {
            data["NAME"] = player.name
            data["UUID"] = player.uniqueId.toString()

            data["FIRST_JOIN_TIME"] = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
            data["LAST_JOIN_TIME"] = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())

            data["FARM_CODE"] = null
            data["FARM_CHAT"] = false

            data.save(file)
        }

        name = data.getString("NAME")
        uuid = UUID.fromString(data.getString("UUID"))
        firstJoinTime = data.getString("FIRST_JOIN_TIME")
        lastJoinTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        data["LAST_JOIN_TIME"] = lastJoinTime
        farmCode = data.getString("FARM_CODE")
        farmChat = data.getBoolean("FARM_CHAT")
        data.save(file)
        PlayerClass.playerData[player] = this
        islandCheck(player)
    }

    constructor(player: OfflinePlayer) {
        val file = File("${pl.dataFolder}/player", "${player.uniqueId}.yml")
        val data = YamlConfiguration.loadConfiguration(file)

        name = data.getString("NAME")
        uuid = UUID.fromString(data.getString("UUID"))
        firstJoinTime = data.getString("FIRST_JOIN_TIME")
        lastJoinTime = data.getString("LAST_JOIN_TIME")
        farmCode = data.getString("FARM_CODE")
        farmChat = data.getBoolean("FARM_CHAT")
        data.save(file)
    }

    fun islandCheck(player: Player) {
        farmCode ?: return
        val island = IslandClass.getIsland(farmCode!!) ?: return setFarm(null)
        if (!island.member().contains(player.uniqueId.toString())) return setFarm(null)
        PlayerClass.playerIsland[player] = island
    }

    fun setFarm(code: String?) {
        val file = File("${pl.dataFolder}/player", "$uuid.yml")
        val data = YamlConfiguration.loadConfiguration(file)
        farmCode = code
        data["FARM_CODE"] = code
        data.save(file)
    }

    fun toggleChat(): Boolean {
        val file = File("${pl.dataFolder}/player", "$uuid.yml")
        val data = YamlConfiguration.loadConfiguration(file)
        data["FARM_CHAT"] = !farmChat
        farmChat = !farmChat
        data.save(file)
        return farmChat
    }
}