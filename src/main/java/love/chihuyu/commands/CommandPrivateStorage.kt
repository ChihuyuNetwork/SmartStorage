package love.chihuyu.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.OfflinePlayerArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import love.chihuyu.data.StorageData
import love.chihuyu.utils.StorageUtil
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.concurrent.CompletableFuture

object CommandPrivateStorage {

    val main = CommandAPICommand("privatestorage")
        .withAliases("ps")
        .withArguments(
            StringArgument("storageName").replaceSuggestions(
                ArgumentSuggestions.strings { info ->
                    CompletableFuture.supplyAsync { StorageUtil.getJoinedStorages(Bukkit.getOfflinePlayer(info.sender.name).uniqueId).map { it.key.name }.toTypedArray() }.get()
                }
            ),
            OfflinePlayerArgument("owner").replaceSuggestions(
                ArgumentSuggestions.strings { info ->
                    CompletableFuture.supplyAsync { StorageUtil.getDuplicatedStorages(info.previousArgs[0] as String).map { Bukkit.getOfflinePlayer(it.first.owner).name }.toTypedArray() }.get()
                }
            )
        )
        .executesPlayer(
            PlayerCommandExecutor { sender, args ->
                val name = args[0] as String
                val owner = (args[1] as OfflinePlayer).uniqueId

                val nextPage = ItemStack(Material.LIME_WOOL).apply {
                    this.addUnsafeEnchantment(Enchantment.MENDING, 1)
                    this.itemMeta = this.itemMeta?.apply {
                        this.setDisplayName("Next Page")
                        this.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                    }
                }
                val prevPage = ItemStack(Material.RED_WOOL).apply {
                    this.addUnsafeEnchantment(Enchantment.MENDING, 1)
                    this.itemMeta = this.itemMeta?.apply {
                        this.setDisplayName("Previous Page")
                        this.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                    }
                }

                StorageUtil.removeEmptyInventory(owner, name)
                val inv = StorageUtil.getExactStorageInventory(owner, name)?.second

                if (inv == null) {
                    sender.sendMessage(StorageData.privateStorageInv.toString())
                    sender.sendMessage("${ChatColor.RED}Storage not found.")
                    return@PlayerCommandExecutor
                }
                inv.forEach { it.setItem(52, prevPage) }
                inv.forEach { it.setItem(53, nextPage) }
                sender.openInventory(inv[0])
            }
        )
}