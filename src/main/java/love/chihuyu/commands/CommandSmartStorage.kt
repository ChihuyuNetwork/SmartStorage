package love.chihuyu.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import love.chihuyu.SmartStorage.Companion.plugin
import love.chihuyu.wrappers.SqliteWrapper
import org.bukkit.ChatColor
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object CommandSmartStorage {

    private fun saveCommand(): CommandAPICommand = CommandAPICommand("save")
        .withPermission(CommandPermission.OP)
        .executesPlayer(PlayerCommandExecutor { sender, _ ->
            try {
                if (!plugin.config.getBoolean("mute-saved-alert")) plugin.server.broadcastMessage("${ChatColor.GREEN}[SmartStorage] Saving data...")

                val file = File(plugin.dataFolder, "smart-storage.db")
                val tmpFileName = "smart-storage-tmp-${SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Date())}.db"
                val tmpFile = File(plugin.dataFolder, tmpFileName)
                file.copyTo(tmpFile, overwrite = true)
                SqliteWrapper.saveAll()
                tmpFile.delete()

                if (!plugin.config.getBoolean("mute-saved-alert")) plugin.server.broadcastMessage("${ChatColor.GREEN}[SmartStorage] Save completed successfully!")
            } catch (e: Exception) {
                e.printStackTrace()
                plugin.server.broadcastMessage("${ChatColor.GREEN}[SmartStorage] ${ChatColor.RED}Failed to save data, please contact the administrator!")
            }
        })

    val main: CommandAPICommand = CommandAPICommand("smartstorage")
        .withPermission("smartstorage.smartstorage")
        .withSubcommands(saveCommand())

}