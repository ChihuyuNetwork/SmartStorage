package love.chihuyu.listeners

import love.chihuyu.storage.StorageManager
import love.chihuyu.wrappers.SqliteWrapper
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent

object CloseSmartStorageEventListener : Listener {

    @EventHandler
    fun onClose(event: InventoryCloseEvent) {
        val inventory = event.inventory
        val storage =
            StorageManager.inventories.firstNotNullOfOrNull { (id, inventories) ->
                if (inventories.contains(inventory)) StorageManager.storages.firstOrNull { it.id == id } else null
            } ?: return


        SqliteWrapper.save(storage)
//        event.player.sendMessage("${ChatColor.GREEN}[SmartStorage] Storage saved.")
    }
}