package love.chihuyu.listeners

import love.chihuyu.storage.*
import love.chihuyu.storage.StorageType.*
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

object ClickPaginationButtonEventListener : Listener {

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val item = event.currentItem ?: return

        if (!item.isPaginationButton()) return
        event.isCancelled = true

        val inventory = event.clickedInventory ?: return
        val player = event.whoClicked as? Player ?: return


        val storage =
            StorageManager.inventories.firstNotNullOfOrNull { (id, inventories) ->
                if (inventories.contains(inventory)) StorageManager.storages.firstOrNull { it.id == id } else null
            } ?: return
        val inventories = StorageManager.inventories[storage.id] ?: return

        val currentIndex = inventories.indexOf(inventory)
        val newInventoryName = when (storage.storageType) {
            SERVER -> "Server Storage  Page ${currentIndex + 2}"
            PERSONAL -> "Personal Storage  Page ${currentIndex + 2}"
            GROUP -> "${storage.storageName}  Page ${currentIndex + 2}"
        }

        when (item.type) {
            // previous page
            Material.RED_WOOL -> {
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f)
                player.openInventory(
                    if (currentIndex == 0) inventories.last() else inventories[currentIndex - 1]
                )
            }
            // next page
            Material.LIME_WOOL -> {
                if (currentIndex.inc() == inventories.size) {
                    if (inventory.contents.filterNotNull().size <= 2) {
                        player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f)
                        player.sendMessage("${ChatColor.GREEN}[SmartStorage] ${ChatColor.RESET}Hint: new page will not created if there is no item in the current page.")
                        player.openInventory(inventories.first())
                        return
                    }

                    val newPage = Bukkit.createInventory(null, 54, newInventoryName).apply {
                        setItem(52, previousPageButton)
                        setItem(53, nextPageButton)
                    }
                    inventories.add(newPage)
                }

                player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f)
                player.openInventory(inventories[currentIndex.inc()])
            }

            else -> return
        }
    }
}
