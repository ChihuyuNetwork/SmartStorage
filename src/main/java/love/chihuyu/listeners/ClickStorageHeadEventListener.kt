package love.chihuyu.listeners

import love.chihuyu.storage.*
import love.chihuyu.storage.StorageType.GROUP
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.meta.SkullMeta

object ClickStorageHeadEventListener : Listener {

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val item = event.currentItem ?: return
        if (!item.isStorageHead()) return

        event.isCancelled = true

        val itemMeta = item.itemMeta!!
        val storageName = itemMeta.displayName
        val ownerUUID = (itemMeta as SkullMeta).owningPlayer?.uniqueId

        val storage = StorageManager.storages.firstOrNull {
            it.storageType == GROUP
            && it.storageName.equals(storageName)
            && it.ownerUUID == ownerUUID
        }?: return
        val inventories = StorageManager.inventories[storage.id] ?: return

        inventories.forEach {
            it.setItem(52, previousPageButton)
            it.setItem(53, nextPageButton)
        }

        val player = event.whoClicked as Player
        player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f)
        player.openInventory(inventories[0])
    }
}
