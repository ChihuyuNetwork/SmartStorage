package love.chihuyu

import love.chihuyu.commands.*
import love.chihuyu.listeners.ClickPaginationButtonEventListener
import love.chihuyu.listeners.ClickStorageHeadEventListener
import love.chihuyu.listeners.CloseSmartStorageEventListener
import love.chihuyu.utils.runTaskTimer
import love.chihuyu.wrappers.SqliteWrapper
import org.bukkit.ChatColor
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SmartStorage : JavaPlugin(), Listener {

    companion object {
        lateinit var plugin: JavaPlugin
    }

    init {
        plugin = this
    }

    override fun onEnable() {
        SqliteWrapper.initialize()
        SqliteWrapper.import()

        CommandSmartStorage.main.register()
        CommandServerStorage.main.register()
        CommandServerStorage.withPages.register()
        CommandPersonalStorage.main.register()
        CommandPersonalStorage.withPages.register()
        CommandGroupStorage.main.register()
        CommandGroupStorageEdit.main.register()

        server.pluginManager.registerEvents(ClickStorageHeadEventListener, this)
        server.pluginManager.registerEvents(ClickPaginationButtonEventListener, this)
        server.pluginManager.registerEvents(CloseSmartStorageEventListener, this)

        // NOTE: 20 ticks = 1 second
        val hour = 20 * 60 * 60.toLong()
        runTaskTimer(/* delay = */ hour, /* intervalTicks = */ hour) {
            try {
                plugin.server.broadcastMessage("${ChatColor.GREEN}[SmartStorage] Saving data...")

                val file = File(plugin.dataFolder, "smart-storage.db")
                val tmpFileName = "smart-storage-tmp-${SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Date())}.db"
                val tmpFile = File(plugin.dataFolder, tmpFileName)
                file.copyTo(tmpFile, overwrite = true)
                SqliteWrapper.saveAll()
                tmpFile.delete()

                plugin.server.broadcastMessage("${ChatColor.GREEN}[SmartStorage] Save completed successfully!")
            } catch (e: Exception) {
                e.printStackTrace()
                plugin.server.broadcastMessage("${ChatColor.GREEN}[SmartStorage] ${ChatColor.RED}Failed to save data, please contact the administrator!")
            }
        }
    }

    override fun onDisable() {
        val file = File(plugin.dataFolder, "smart-storage.db")
        val snapshotFileName = "smart-storage-snapshot-${SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Date())}.db"
        val snapshotFile = File(plugin.dataFolder, snapshotFileName)
        file.copyTo(snapshotFile, overwrite = true)
    }
}