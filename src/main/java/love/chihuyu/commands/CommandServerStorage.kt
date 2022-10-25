package love.chihuyu.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.IntegerArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import love.chihuyu.storage.StorageManager
import love.chihuyu.storage.StorageType
import love.chihuyu.storage.nextPageButton
import love.chihuyu.storage.previousPageButton
import love.chihuyu.wrappers.SqliteWrapper
import org.bukkit.ChatColor

object CommandServerStorage {

    val main: CommandAPICommand = CommandAPICommand("serverstorage")
        .withAliases("ss")
        .withPermission("smartstorage.serverstorage")
        .executesPlayer(
            PlayerCommandExecutor { sender, _ ->
                var storage = StorageManager.storages.firstOrNull {
                    it.storageType == StorageType.SERVER
                }

                if (storage == null) {
                    SqliteWrapper.create(StorageType.SERVER, null, null, null)
                    sender.server.broadcastMessage("${ChatColor.GREEN}[SmartStorage] The Server Storage has been created!")
                    storage = StorageManager.storages.find {
                        it.storageType == StorageType.SERVER
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

    val withPages: CommandAPICommand = CommandAPICommand("serverstorage")
        .withAliases("ss")
        .withPermission("smartstorage.serverstorage")
        .withArguments(IntegerArgument("page"))
        .executesPlayer(
            PlayerCommandExecutor { sender, args ->
                val page = (args[0] as Int).dec()
                var storage = StorageManager.storages.firstOrNull {
                    it.storageType == StorageType.SERVER
                }

                if (storage == null) {
                    SqliteWrapper.create(StorageType.SERVER, null, null, null)
                    sender.server.broadcastMessage("${ChatColor.GREEN}[SmartStorage] The Server Storage has been created!")
                    storage = StorageManager.storages.find {
                        it.storageType == StorageType.SERVER
                    }
                }
                val inventories = StorageManager.inventories[storage!!.id]!!

                if (page > inventories.size) {
                    sender.sendMessage("${ChatColor.RED}[SmartStorage] Invalied page number!")
                    return@PlayerCommandExecutor
                }

                inventories.forEach {
                    it.setItem(52, previousPageButton)
                    it.setItem(53, nextPageButton)
                }

                sender.openInventory(inventories[page])
            }
        )
}