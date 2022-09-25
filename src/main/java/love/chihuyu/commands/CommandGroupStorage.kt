package love.chihuyu.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import love.chihuyu.storage.StorageManager
import love.chihuyu.storage.getStorageHead
import org.bukkit.Bukkit
import love.chihuyu.storage.StorageType.GROUP

object CommandGroupStorage {

    val main: CommandAPICommand = CommandAPICommand("groupstorage")
        .withAliases("gs")
        .withPermission(CommandPermission.NONE)
        .withPermission("smartstorage.groupstorage")
        .executesPlayer(
            PlayerCommandExecutor { sender, _ ->
                val guiName = "Group Storage Menu"
                val guiInv = Bukkit.createInventory(null, 54, guiName)

                StorageManager.storages
                    .filter { it.storageType == GROUP && it.memberUUIDs.contains(sender.uniqueId) }
                    .forEachIndexed { index, storage ->
                        guiInv.setItem(index, getStorageHead(storage))
                    }

                sender.openInventory(guiInv)
            }
        )

}