package love.chihuyu.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
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
        .withPermission("privatestorage.ps")
        .withPermission(CommandPermission.NONE)
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

                StorageUtil.removeEmptyInventory(owner, name)
                val inv = StorageUtil.getExactStorageInventory(owner, name)?.second

                if (inv == null) {
                    sender.sendMessage(StorageData.privateStorageInv.toString())
                    sender.sendMessage("${ChatColor.RED}Storage not found.")
                    return@PlayerCommandExecutor
                }
                inv.forEach { it.setItem(52, StorageUtil.previousPageButton) }
                inv.forEach { it.setItem(53, StorageUtil.nextPageButton) }
                sender.openInventory(inv[0])
            }
        )
}