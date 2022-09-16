package love.chihuyu.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import love.chihuyu.data.StorageData
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

object CommandOpenStorage {

    val main = CommandAPICommand("openstorage")
        .withPermission("privatestorage.openstorage")
        .withPermission(CommandPermission.NONE)
        .withAliases("os")
        .executesPlayer(
            PlayerCommandExecutor { sender, args ->
                StorageData.openStorageInv.removeIf { StorageData.openStorageInv.indexOf(it) != 0 && it.contents.filterNotNull().isEmpty() }
                StorageData.openStorageInv.forEach { inv ->
                    inv.setItem(52, ItemStack(Material.RED_WOOL).apply {
                        this.addUnsafeEnchantment(Enchantment.MENDING, 1)
                        this.itemMeta = this.itemMeta?.apply {
                            this.setDisplayName("Previous Page")
                            this.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                        }
                    })
                    inv.setItem(53, ItemStack(Material.LIME_WOOL).apply {
                        this.addUnsafeEnchantment(Enchantment.MENDING, 1)
                        this.itemMeta = this.itemMeta?.apply {
                            this.setDisplayName("Next Page")
                            this.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                        }
                    })
                }

                if (StorageData.openStorageInv.size < 1) StorageData.openStorageInv.add(Bukkit.createInventory(null, 54, "Open Storage  Page - 1"))
                sender.openInventory(StorageData.openStorageInv[0])
            }
        )
}