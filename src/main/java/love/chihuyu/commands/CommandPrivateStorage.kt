package love.chihuyu.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.OfflinePlayerArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import love.chihuyu.data.StorageData
import love.chihuyu.utils.StorageUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import java.util.concurrent.CompletableFuture

object CommandPrivateStorage {

    val main = CommandAPICommand("privatestorage")
        .withPermission("privatestorage.ps")
        .withPermission(CommandPermission.NONE)
        .withAliases("ps")
//        .withArguments(
//            StringArgument("storageName").replaceSuggestions(
//                ArgumentSuggestions.strings { info ->
//                    CompletableFuture.supplyAsync {
//                        StorageUtils.getJoinedStorages(Bukkit.getOfflinePlayer(info.sender.name).uniqueId)
//                            .map { it.key.storageName }.toTypedArray()
//                    }.get()
//                }
//            ),
//            OfflinePlayerArgument("owner").replaceSuggestions(
//                ArgumentSuggestions.strings { info ->
//                    CompletableFuture.supplyAsync {
//                        StorageUtils.getDuplicatedStorages(info.previousArgs[0] as String)
//                            .map { Bukkit.getOfflinePlayer(it.first.ownerUUID).name }.toTypedArray()
//                    }.get()
//                }
//            )
//        )
        .executesPlayer(
            PlayerCommandExecutor { sender, _ ->
                val guiName = "Private Storages"
                val gui = Bukkit.createInventory(null, 54, guiName)

//                val storageName = args[0] as String
//                val ownerUUID = (args[1] as OfflinePlayer).uniqueId

//                StorageUtils.removeEmptyInventory(ownerUUID, storageName)
                val storages = StorageUtils.getJoinedStorages(sender.uniqueId)

//                val inventories = StorageUtils.getStorageInventories(ownerUUID, storageName)
//                if (inventories == null) {
//                    sender.sendMessage(StorageData.privateStorageFullDataMap.toString())
//                    sender.sendMessage("${ChatColor.RED}Storage not found.")
//                    return@PlayerCommandExecutor
//                }
                storages.keys.forEachIndexed { index, storageInfo ->
                    gui.setItem(index, StorageUtils.getStorageHead(storageInfo))
                }

//                inventories.forEach {
//                    it.setItem(52, StorageUtils.previousPageButton)
//                    it.setItem(53, StorageUtils.nextPageButton)
//                }

                sender.openInventory(gui)
            }
        )
}