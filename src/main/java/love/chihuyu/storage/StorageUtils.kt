package love.chihuyu.storage

import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException


fun List<ItemStack?>.excludePaginationButtonAndNull(): List<ItemStack> {
    return this.filterNotNull().filterNot { it.isPaginationButton() }
}

@Throws(IllegalStateException::class)
fun arrayToBase64(itemStacks: Array<ItemStack>): String {
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
fun listFromBase64(data: String): List<ItemStack> {
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

        return items.filterNotNull().toList()
    } catch (e: ClassNotFoundException) {
        throw IOException("Unable to decode class type.", e)
    }
}