package love.chihuyu.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import love.chihuyu.storage.StorageManager
import love.chihuyu.storage.StorageType
import love.chihuyu.storage.nextPageButton
import love.chihuyu.storage.previousPageButton
import love.chihuyu.wrappers.SqliteWrapper
import org.bukkit.ChatColor

object CommandPersonalStorage {

    val main: CommandAPICommand = CommandAPICommand("personalstorage")
        .withAliases("ps")
        .withPermission(CommandPermission.NONE)
        .withPermission("smartstorage.personalstorage")
        .executesPlayer(
            PlayerCommandExecutor { sender, _ ->
                var storage = StorageManager.storages.firstOrNull {
                    it.storageType == StorageType.PERSONAL && it.ownerUUID == sender.uniqueId
                }
                if (storage == null) {
                    SqliteWrapper.create(
                        StorageType.PERSONAL, null, sender.uniqueId, null
                    )
                    sender.sendMessage("${ChatColor.GREEN}[SmartStorage] Your Personal Storage has been created!")
                    storage = StorageManager.storages.firstOrNull {
                        it.storageType == StorageType.PERSONAL
                                && it.ownerUUID == sender.uniqueId
                    }
                }
                val inventories = StorageManager.inventories[storage!!.id]!!

                inventories.forEach {
                    it.setItem(52, previousPageButton)
                    it.setItem(53, nextPageButton)
                }

                sender.openInventory(inventories.first())
            }
        )

}