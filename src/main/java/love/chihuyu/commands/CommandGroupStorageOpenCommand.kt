package love.chihuyu.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.PlayerArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import love.chihuyu.storage.StorageManager
import love.chihuyu.storage.StorageType
import love.chihuyu.storage.nextPageButton
import love.chihuyu.storage.previousPageButton
import org.bukkit.entity.Player

object CommandGroupStorageOpenCommand {

    val main = CommandAPICommand("groupstorageopen")
        .withAliases("gso")
        .withPermission("smartstorage.groupstorageopen")
        .withArguments(PlayerArgument("target"), StringArgument("storage"))
        .executesPlayer(PlayerCommandExecutor { sender, args ->
            val player = args[0] as Player
            val storageName = args[1] as String
            val storage = StorageManager.storages.firstOrNull {
                it.storageType == StorageType.GROUP
                        && it.storageName.equals(storageName)
                        && player.uniqueId in it.memberUUIDs
            }?: return@PlayerCommandExecutor
            val inventories = StorageManager.inventories[storage.id] ?: return@PlayerCommandExecutor

            inventories.forEach {
                it.setItem(52, previousPageButton)
                it.setItem(53, nextPageButton)
            }

            player.openInventory(inventories[0])
        })
}