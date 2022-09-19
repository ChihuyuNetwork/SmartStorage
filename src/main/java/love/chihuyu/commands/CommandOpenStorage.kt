package love.chihuyu.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import love.chihuyu.data.StorageData
import love.chihuyu.utils.StorageUtils
import love.chihuyu.utils.StorageUtils.excludePageButton
import org.bukkit.Bukkit

object CommandOpenStorage {

    val main: CommandAPICommand = CommandAPICommand("openstorage")
        .withAliases("os")
        .withPermission("privatestorage.openstorage")
        .withPermission(CommandPermission.NONE)
        .executesPlayer(
            PlayerCommandExecutor { sender, _ ->
                StorageData.openStorageInvs.removeIf {
                    it.contents.filterNotNull().excludePageButton().isEmpty()
                }

                // HACK: とりあえずこれで動かす
                if (StorageData.openStorageInvs.size == 0) {
                    val newInventory = Bukkit.createInventory(null, 54, "Open Storage  Page - 1")
                    newInventory.setItem(52, StorageUtils.previousPageButton)
                    newInventory.setItem(53, StorageUtils.nextPageButton)
                    StorageData.openStorageInvs.add(newInventory)
                } else {
                    StorageData.openStorageInvs.forEach { inv ->
                        inv.setItem(52, StorageUtils.previousPageButton)
                        inv.setItem(53, StorageUtils.nextPageButton)
                    }
                }

                sender.openInventory(StorageData.openStorageInvs.first())
            }
        )
}