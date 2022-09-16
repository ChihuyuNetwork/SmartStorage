package love.chihuyu.utils

import love.chihuyu.PrivateStorage.Companion.plugin
import love.chihuyu.data.StorageData
import love.chihuyu.data.StorageInfo
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.UUID

object StorageUtil {

    fun updateStorageInfos() {
        StorageData.privateStorages.clear()
        StorageData.privateStorageInv.forEach { (info, invs) ->
            val items = mutableListOf<ItemStack>()
            invs.forEach { items.addAll(it.contents.filterNotNull()
                .filterNot { item -> (item.type == Material.LIME_WOOL && item.itemMeta?.hasEnchants() == true) || (item.type == Material.RED_WOOL && item.itemMeta?.hasEnchants() == true) }) }

            StorageData.privateStorages.add(
                StorageInfo(
                    info.name,
                    info.owner,
                    items,
                    info.members
                )
            )
        }
    }

    fun getExactStorage(owner: UUID, name: String): StorageInfo? {
        return StorageData.privateStorages.firstOrNull { it.owner == owner && it.name == name }
    }

    fun getExactStorageInventory(owner: UUID, name: String): Pair<StorageInfo, MutableList<Inventory>>? {
        return StorageData.privateStorageInv.toList().firstOrNull { it.first.owner == owner && it.first.name == name }
    }

    fun getJoinedStorages(player: UUID): Map<StorageInfo, MutableList<Inventory>> {
        return StorageData.privateStorageInv.filter { player in it.key.members }
    }

    fun getStoragesByName(player: UUID, name: String): Map<StorageInfo, MutableList<Inventory>> {
        return getJoinedStorages(player).filter { it.key.name == name }
    }

    fun getStoragesByOwner(owner: UUID): Map<StorageInfo, MutableList<Inventory>> {
        return StorageData.privateStorageInv.filter { it.key.owner == owner }
    }

    fun getDuplicatedStorages(name: String): List<Pair<StorageInfo, MutableList<Inventory>>> {
        return StorageData.privateStorageInv.toList().filter { it.first.name == name }
    }

    fun getStorageByInventory(inventory: Inventory): Pair<StorageInfo, MutableList<Inventory>>? {
        return StorageData.privateStorageInv.toList().firstOrNull { inventory in it.second }
    }

    fun removeEmptyInventory(owner: UUID, name: String) {
        val storages = getExactStorageInventory(owner, name)?.second ?: return
        storages.removeIf {
            if (storages.indexOf(it) != 0) {
                if (storages.indexOf(it) == storages.lastIndex) {
                    it.contents.filterNotNull().size <= 1
                } else {
                    it.contents.filterNotNull().size <= 2
                }
            } else {
                false
            }
        }
    }
}