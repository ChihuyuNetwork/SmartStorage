package love.chihuyu.storage

import org.bukkit.Bukkit.getOfflinePlayer
import org.bukkit.Material.*
import org.bukkit.enchantments.Enchantment.MENDING
import org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

private val customModelDataPair = mapOf(
    "PaginationButton" to 4170,
    "StorageHead" to 4175
)

val nextPageButton = ItemStack(LIME_WOOL).apply {
    addUnsafeEnchantment(MENDING, 1)
    itemMeta = itemMeta?.apply {
        setDisplayName("Next Page")
        addItemFlags(HIDE_ENCHANTS)
        setCustomModelData(customModelDataPair["PaginationButton"])
    }
}

val previousPageButton = ItemStack(RED_WOOL).apply {
    addUnsafeEnchantment(MENDING, 1)
    itemMeta = itemMeta?.apply {
        setDisplayName("Previous Page")
        addItemFlags(HIDE_ENCHANTS)
        setCustomModelData(customModelDataPair["PaginationButton"])
    }
}

fun ItemStack.isPaginationButton() =
    (type == LIME_WOOL || type == RED_WOOL)
            && itemMeta?.hasCustomModelData() == true
            && itemMeta!!.customModelData == customModelDataPair["PaginationButton"]

fun ItemStack.isStorageHead() =
    type == PLAYER_HEAD
            && itemMeta?.hasCustomModelData() == true
            && itemMeta!!.customModelData == customModelDataPair["StorageHead"]

fun getStorageHead(storage: SmartStorage): ItemStack =
    ItemStack(PLAYER_HEAD).apply {
        addUnsafeEnchantment(MENDING, 1)
        itemMeta = itemMeta?.apply {
            setDisplayName(storage.storageName)
            addItemFlags(HIDE_ENCHANTS)
            setCustomModelData(customModelDataPair["StorageHead"])
            (this as SkullMeta).owningPlayer = getOfflinePlayer(storage.ownerUUID!!)
        }
    }
