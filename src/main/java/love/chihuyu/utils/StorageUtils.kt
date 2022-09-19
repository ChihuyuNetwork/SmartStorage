package love.chihuyu.utils

import love.chihuyu.data.StorageData
import love.chihuyu.data.StorageInfo
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.UUID

object StorageUtils {

    val nextPageButton = ItemStack(Material.LIME_WOOL).apply {
        this.addUnsafeEnchantment(Enchantment.MENDING, 1)
        this.itemMeta = this.itemMeta?.apply {
            this.setDisplayName("Next Page")
            this.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        }
    }

    val previousPageButton = ItemStack(Material.RED_WOOL).apply {
        this.addUnsafeEnchantment(Enchantment.MENDING, 1)
        this.itemMeta = this.itemMeta?.apply {
            this.setDisplayName("Previous Page")
            this.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        }
    }

    fun getStorageHead(info: StorageInfo): ItemStack {
        val head = ItemStack(Material.PLAYER_HEAD).apply {
            this.addUnsafeEnchantment(Enchantment.MENDING, 1)
            this.itemMeta = (this.itemMeta as SkullMeta).apply {
                this.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                this.setDisplayName(info.storageName)
                this.ownerProfile = Bukkit.getOfflinePlayer(info.ownerUUID).playerProfile
                this.setCustomModelData(417)
            }
        }

        return head
    }

    fun List<ItemStack>.excludePageButton(): List<ItemStack> {
        val buttonPattern = setOf(
            Material.LIME_WOOL,
            Material.RED_WOOL,
        )

        return this.filterNot {
            buttonPattern.contains(it.type) && it.itemMeta?.hasEnchants() == true
        }
    }

    fun getStorageInfo(owner: UUID, name: String): StorageInfo? {
        return StorageData
            .privateStorages
            .firstOrNull {
                it.ownerUUID == owner && it.storageName == name
            }
    }

    fun getStorageInventories(ownerUUID: UUID, name: String): MutableList<Inventory>? {
        return StorageData
            .privateStorageFullDataMap[getStorageInfo(ownerUUID, name)]
    }

    fun getJoinedStorages(player: UUID): Map<StorageInfo, MutableList<Inventory>> {
        return StorageData.privateStorageFullDataMap.filter { player in it.key.memberUUIDs }
    }

    fun getStoragesByOwner(owner: UUID): Map<StorageInfo, MutableList<Inventory>> {
        return StorageData.privateStorageFullDataMap.filter { it.key.ownerUUID == owner }
    }

    fun getDuplicatedStorages(name: String): List<Pair<StorageInfo, MutableList<Inventory>>> {
        return StorageData.privateStorageFullDataMap.toList().filter { it.first.storageName == name }
    }

    fun getStorageByInventory(inventory: Inventory): Pair<StorageInfo, MutableList<Inventory>>? {
        return StorageData.privateStorageFullDataMap.toList().firstOrNull { inventory in it.second }
    }

    fun removeEmptyInventory(ownerUUID: UUID, storageName: String) {
        val storages = getStorageInventories(ownerUUID, storageName) ?: return

        // TODO: 動作確認
        storages.removeIf {
            storages.indexOf(it) != 0 && it.contents.filterNotNull().size <= 2
        }
    }

    fun updateStorageInfos() {
        StorageData.privateStorages.clear()

        StorageData.privateStorageFullDataMap.forEach { (info, invs) ->
            val fixedItemStacks = mutableListOf<ItemStack>()
            invs.forEach {
                fixedItemStacks.addAll(it.contents.filterNotNull().excludePageButton())
            }

            StorageData.privateStorages.add(
                StorageInfo(
                    info.ownerUUID, info.storageName, fixedItemStacks, info.memberUUIDs
                )
            )
        }
    }

    @Throws(IllegalStateException::class)
    fun arrayToBase64(itemStacks: Array<ItemStack?>): String? {
        try {
            val stream = ByteArrayOutputStream()
            val bukkitStream = BukkitObjectOutputStream(stream)

            // Write the size of the inventory
            bukkitStream.writeInt(itemStacks.size)
            // Save every element in the list
            itemStacks.indices.forEach {
                bukkitStream.writeObject(itemStacks[it])
            }
            bukkitStream.close()
            // Serialize that array
            val buffer = stream.toByteArray()

            return Base64Coder.encodeLines(buffer)
        } catch (e: Exception) {
            throw IllegalStateException("Unable to save item stacks.", e)
        }
    }

    // NOTE: 何かが破損していたらココを確認しよう
    @Throws(IOException::class)
    fun arrayFromBase64(data: String): Array<ItemStack?> {
        try {
            val buffer = Base64Coder.decodeLines(data)
            val stream = ByteArrayInputStream(buffer)

            // Read the serialized inventory
            val bukkitStream = BukkitObjectInputStream(stream)
            val items = arrayOfNulls<ItemStack>(bukkitStream.readInt())
            items.indices.forEach {
                items[it] = bukkitStream.readObject() as ItemStack
            }
            bukkitStream.close()

            return items
        } catch (e: ClassNotFoundException) {
            throw IOException("Unable to decode class type.", e)
        }
    }
}