package love.chihuyu.utils

import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

object ItemUtil {

    fun create(
        material: Material,
        name: String? = null,
        localizedName: String? = null,
        amount: Int? = null,
        customModelData: Int? = null,
        lore: List<String>? = null,
        attributeModifier: Map<Attribute, AttributeModifier>? = null,
        enchantments: Map<Enchantment, Int>? = null,
        flags: List<ItemFlag>? = null,
        unbreakable: Boolean? = null,
    ): ItemStack {
        val item = ItemStack(material)
        if (amount != null) item.amount = amount

        val meta = item.itemMeta ?: return item
        if (name != null) meta.setDisplayName(name)
        if (localizedName != null) meta.setLocalizedName(localizedName)
        if (unbreakable != null) meta.isUnbreakable = unbreakable
        if (lore != null) meta.lore = lore
        if (customModelData != null) meta.setCustomModelData(customModelData)

        attributeModifier?.forEach { meta.addAttributeModifier(it.key, it.value) }
        enchantments?.forEach { item.addUnsafeEnchantment(it.key, it.value) }
        flags?.forEach { meta.addItemFlags(it) }

        item.itemMeta = meta
        return item
    }

    @Throws(IllegalStateException::class)
    fun itemStackArrayToBase64(items: Array<ItemStack?>): String? {
        return try {
            val outputStream = ByteArrayOutputStream()
            val dataOutput = BukkitObjectOutputStream(outputStream)

            // Write the size of the inventory
            dataOutput.writeInt(items.size)

            // Save every element in the list
            for (i in items.indices) {
                dataOutput.writeObject(items[i])
            }

            // Serialize that array
            dataOutput.close()
            Base64Coder.encodeLines(outputStream.toByteArray())
        } catch (e: Exception) {
            throw IllegalStateException("Unable to save item stacks.", e)
        }
    }

    @Throws(IOException::class)
    fun itemStackArrayFromBase64(data: String?): Array<ItemStack?>? {
        return try {
            val inputStream = ByteArrayInputStream(Base64Coder.decodeLines(data))
            val dataInput = BukkitObjectInputStream(inputStream)
            val items = arrayOfNulls<ItemStack>(dataInput.readInt())

            // Read the serialized inventory
            for (i in items.indices) {
                items[i] = dataInput.readObject() as? ItemStack
            }
            dataInput.close()
            items
        } catch (e: ClassNotFoundException) {
            throw IOException("Unable to decode class type.", e)
        }
    }
}
