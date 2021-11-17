package kr.kro.minestar.minefarm.data

import kr.kro.minestar.minefarm.Main
import net.kyori.adventure.text.Component
import org.bukkit.OfflinePlayer
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.util.*

class PlayerData {
    private var f: File
    private var data: YamlConfiguration
    val name: String
    val uuid: UUID
    val firstJoinTime: String
    var lastJoinTime: String
    var islandCode: Int
    var farmChat: Boolean

    constructor(p: Player) {
        f = File(Main.pl.dataFolder.toString() + "/player", "${p.uniqueId}.yml")
        data = YamlConfiguration.loadConfiguration(f)
        if (!f.exists()) p.kick(Component.text("§c플레이어 데이터가 삭제되었습니다."))
        firstJoinTime = data.getString("FIRST_JOIN_TIME").toString()
        lastJoinTime = data.getString("LAST_JOIN_TIME").toString()
        name = data.getString("NAME").toString()
        uuid = UUID.fromString(data.getString("UUID"))
        islandCode = data.getInt("ISLAND")
        farmChat = data.getBoolean("FARM_CHAT")
    }

    constructor(uuid: UUID) {
        f = File(Main.pl.dataFolder.toString() + "/player", "$uuid.yml")
        data = YamlConfiguration.loadConfiguration(f)
        firstJoinTime = data.getString("FIRST_JOIN_TIME").toString()
        lastJoinTime = data.getString("LAST_JOIN_TIME").toString()
        name = data.getString("NAME").toString()
        this.uuid = UUID.fromString(data.getString("UUID"))
        islandCode = data.getInt("ISLAND")
        farmChat = data.getBoolean("FARM_CHAT")
    }

    constructor(offlinePlayer: OfflinePlayer) {
        f = File(Main.pl.dataFolder.toString() + "/player", "${offlinePlayer.uniqueId}.yml")
        data = YamlConfiguration.loadConfiguration(f)
        firstJoinTime = data.getString("FIRST_JOIN_TIME").toString()
        lastJoinTime = data.getString("LAST_JOIN_TIME").toString()
        name = data.getString("NAME").toString()
        this.uuid = UUID.fromString(data.getString("UUID"))
        islandCode = data.getInt("ISLAND")
        farmChat = data.getBoolean("FARM_CHAT")
    }

    fun setIsCode(code: Int) {
        data["ISLAND"] = code
        islandCode = code
        data.save(f)
    }

    fun toggleChat(): Boolean {
        data["FARM_CHAT"] = !farmChat
        farmChat = !farmChat
        data.save(f)
        return farmChat
    }
}