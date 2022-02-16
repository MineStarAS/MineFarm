package kr.kro.minestar.minefarm.data

import kr.kro.minestar.minefarm.Main.Companion.pl
import kr.kro.minestar.minefarm.functions.PlayerClass
import kr.kro.minestar.minefarm.functions.farm.FarmClass
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

    fun name() = name
    fun uuid() = uuid
    fun firstJoinTime() = firstJoinTime
    fun lastJoinTime() = lastJoinTime
    fun farmCode() = farmCode

    constructor(player: Player) {
        val file = File("${pl.dataFolder}/player", "${player.uniqueId}.player")
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
        data.save(file)
        PlayerClass.playerData[player] = this
        farmCheck(player)
    }

    constructor(player: OfflinePlayer) {
        val file = File("${pl.dataFolder}/player", "${player.uniqueId}.player")
        val data = YamlConfiguration.loadConfiguration(file)

        name = data.getString("NAME")
        uuid = UUID.fromString(data.getString("UUID"))
        firstJoinTime = data.getString("FIRST_JOIN_TIME")
        lastJoinTime = data.getString("LAST_JOIN_TIME")
        farmCode = data.getString("FARM_CODE")
        data.save(file)
    }

    fun farmCheck(player: Player) {
        farmCode ?: return
        val farm = FarmClass.getFarm(farmCode!!) ?: return setFarm(null)
        if (!farm.member().contains(player.uniqueId.toString())) return setFarm(null)
        PlayerClass.playerFarm[player] = farm
    }

    fun setFarm(code: String?) {
        val file = File("${pl.dataFolder}/player", "$uuid.player")
        val data = YamlConfiguration.loadConfiguration(file)
        farmCode = code
        data["FARM_CODE"] = code
        data.save(file)
    }
}