package love.chihuyu.data

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import love.chihuyu.utils.ItemUtil
import love.chihuyu.utils.StorageUtil
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.UUID

class StorageInfo(val name: String, val owner: UUID, val items: MutableList<ItemStack>, val members: MutableSet<UUID>) {

    fun toJson(): JsonObject {
        val storageJson = JsonObject()
        val playerArray = JsonArray()

        members.forEach {
            playerArray.add(it.toString())
        }

        storageJson.addProperty("name", name)
        storageJson.addProperty("owner", owner.toString())

        val items = mutableListOf<ItemStack>()
        StorageUtil.getExactStorageInventory(owner, name)?.second?.forEach { items.addAll(it.contents.filterNotNull()
            .filter { item -> (item.type != Material.LIME_WOOL && item.itemMeta?.hasEnchants() != true) || (item.type != Material.RED_WOOL && item.itemMeta?.hasEnchants() != true) }) }
        storageJson.addProperty("items", ItemUtil.itemStackArrayToBase64(items.toTypedArray()))

        storageJson.add("members", playerArray)

        return storageJson
    }

    fun fromJson(): Triple<UUID, MutableList<ItemStack>, MutableSet<UUID>> {
        return Triple(owner, items, members)
    }
}