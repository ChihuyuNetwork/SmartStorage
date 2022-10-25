package love.chihuyu.storage

import love.chihuyu.storage.StorageType.*
import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory

object StorageManager {
    val storages = mutableListOf<SmartStorage>()
    val inventories = mutableMapOf</* storage.id = */ Int, MutableList<Inventory>>()

    fun add(storage: SmartStorage) {
        val inventories = this.inventories[storage.id] ?: mutableListOf()
        val itemStacks = listFromBase64(storage.rawItemStacks)
        val storageName = when (storage.storageType) {
            SERVER -> "Server Storage"
            PERSONAL -> "Personal Storage"
            GROUP -> storage.storageName
        }
        val pages = itemStacks.chunked(52).mapIndexed { index, page ->
            Bukkit.createInventory(null, 54, "$storageName  Page ${index.inc()}")
                .apply {
                    page.forEachIndexed {index, item -> setItem(index, item)}
                }
        }

        if (pages.isEmpty()) {
            inventories.add(Bukkit.createInventory(null, 54, "$storageName  Page 1"))
        } else {
            inventories.addAll(pages)
        }

        this.inventories[storage.id] = inventories
        this.storages.add(storage)
    }

    fun remove(storage: SmartStorage) {
        this.inventories.remove(storage.id)
        this.storages.removeIf { it.id == storage.id }
    }
}
