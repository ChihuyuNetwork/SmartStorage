package love.chihuyu.utils

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta


object ItemStackUtils {
    fun create(
        material: Material,
        amount: Int = 1,
        enchantments: Map<Enchantment, Int>? = null,
        meta: ItemMeta? = null
    ): ItemStack {
        val item = ItemStack(material)
        enchantments?.forEach { item.addUnsafeEnchantment(it.key, it.value) }
        item.amount = amount
        item.itemMeta = meta
        return item
    }
}