package love.chihuyu

import love.chihuyu.commands.CommandOpenStorage
import love.chihuyu.commands.CommandPrivateStorage
import love.chihuyu.commands.CommandPsedit
import love.chihuyu.data.StorageData
import love.chihuyu.listeners.ClickInventoryEventListener
import love.chihuyu.utils.StorageUtils
import love.chihuyu.utils.runTaskTimer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

class PrivateStorage : JavaPlugin(), Listener {

    companion object {
        lateinit var plugin: JavaPlugin
    }

    init {
        plugin = this
    }

    override fun onEnable() {
        server.pluginManager.registerEvents(ClickInventoryEventListener, this)

        StorageData.import()

        StorageData.openStorageItemStacks.chunked(52).forEachIndexed { index, itemStacks ->
            val displayName = "Open Storage  Page - ${index.inc()}"
            val inventory = Bukkit.createInventory(null, 54, displayName)

            inventory.addItem(*itemStacks.toTypedArray())
            StorageData.openStorageInvs.add(inventory)
        }

        StorageData.privateStorages.forEach {
            val inventories = StorageData.privateStorageFullDataMap.getOrPut(it) { mutableListOf() }

            it.itemStacks.chunked(52).forEachIndexed { index, itemStacks ->
                val displayName = "${it.storageName} Page - ${index.inc()}"
                val inventory = Bukkit.createInventory(null, 54, displayName)

                inventory.addItem(*itemStacks.toTypedArray())
                inventories.add(inventory)
            }
        }

        CommandPsedit.main.register()
        CommandPrivateStorage.main.register()
        CommandOpenStorage.main.register()

        // NOTE: 20 ticks = 1 second
        runTaskTimer(0, /* intervalTicks = */ 1200) {
            StorageData.save()
        }
    }

    override fun onDisable() {
        StorageData.save()
    }
}