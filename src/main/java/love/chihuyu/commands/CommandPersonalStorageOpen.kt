package love.chihuyu.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.PlayerArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import love.chihuyu.storage.StorageManager
import love.chihuyu.storage.StorageType
import love.chihuyu.storage.nextPageButton
import love.chihuyu.storage.previousPageButton
import love.chihuyu.wrappers.SqliteWrapper
import org.bukkit.ChatColor
import org.bukkit.entity.Player

object CommandPersonalStorageOpen {

    val main = CommandAPICommand("personalstorageopen")
        .withAliases("pso")
        .withPermission("smartstorage.personalstorageopen")
        .withArguments(PlayerArgument("target"))
        .executesPlayer(PlayerCommandExecutor { sender, args ->
            val targetPlayer = args[0] as Player
            var storage = StorageManager.storages.firstOrNull {
                it.storageType == StorageType.PERSONAL && it.ownerUUID == targetPlayer.uniqueId
            }
            if (storage == null) {
                SqliteWrapper.create(
                    StorageType.PERSONAL, null, targetPlayer.uniqueId, null
                )
                targetPlayer.sendMessage("${ChatColor.GREEN}[SmartStorage] Your Personal Storage has been created!")
                storage = StorageManager.storages.firstOrNull {
                    it.storageType == StorageType.PERSONAL
                            && it.ownerUUID == targetPlayer.uniqueId
                }
            }
            val inventories = StorageManager.inventories[storage!!.id]!!

            inventories.forEach {
                it.setItem(52, previousPageButton)
                it.setItem(53, nextPageButton)
            }

            targetPlayer.openInventory(inventories.first())
        })
}