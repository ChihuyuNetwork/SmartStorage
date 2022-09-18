package love.chihuyu.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import love.chihuyu.data.StorageData
import love.chihuyu.utils.StorageUtil
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
                    inv.setItem(52, StorageUtil.previousPageButton)
                    inv.setItem(53, StorageUtil.nextPageButton)
                }

                if (StorageData.openStorageInv.size < 1) StorageData.openStorageInv.add(Bukkit.createInventory(null, 54, "Open Storage  Page - 1"))
                sender.openInventory(StorageData.openStorageInv[0])
            }
        )
}