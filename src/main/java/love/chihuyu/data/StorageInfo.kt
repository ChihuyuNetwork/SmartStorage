package love.chihuyu.data

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import love.chihuyu.utils.StorageUtils
import love.chihuyu.utils.StorageUtils.excludePageButton
import org.bukkit.inventory.ItemStack
import java.util.UUID

class StorageInfo(
    val ownerUUID: UUID,
    val storageName: String,
    val itemStacks: MutableList<ItemStack>,
    val memberUUIDs: MutableSet<UUID>,
) {

    fun toJson(): JsonObject {
        val json = JsonObject()

        val items = mutableListOf<ItemStack>()
        StorageUtils
            .getStorageInventories(ownerUUID, storageName)?.forEach {
                items.addAll(
                    it.contents.filterNotNull().excludePageButton()
                )
            }
        val uuidJsonArray = JsonArray(memberUUIDs.size)
        memberUUIDs.forEach { uuidJsonArray.add(it.toString()) }

        json.addProperty("name", storageName)
        json.addProperty("owner", ownerUUID.toString())
        json.addProperty("items", StorageUtils.arrayToBase64(items.toTypedArray()))
        json.add("members", uuidJsonArray)

        return json
    }
}