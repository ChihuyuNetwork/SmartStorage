package love.chihuyu.storage

import java.util.*

data class SmartStorage(
    val id: Int,
    val storageType: StorageType,
    val storageName: String?,
    val ownerUUID: UUID?,
    val memberUUIDs: MutableSet<UUID>,
    val rawItemStacks: String, // base64 encoded ItemStack array
)