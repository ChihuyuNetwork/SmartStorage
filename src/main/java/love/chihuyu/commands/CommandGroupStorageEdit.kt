package love.chihuyu.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.OfflinePlayerArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import love.chihuyu.storage.StorageManager
import love.chihuyu.storage.StorageType.GROUP
import love.chihuyu.wrappers.SqliteWrapper
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import java.util.concurrent.CompletableFuture

object CommandGroupStorageEdit {

    val main: CommandAPICommand = CommandAPICommand("groupstorageedit")
        .withAliases("gse", "gsedit")
        .withSubcommands(
            createCommand(),
            deleteCommand(),
            addMemberCommand(),
            removeMemberCommand(),
            listCommand()
        )

    private fun argMemberStorageName() = StringArgument("storageNameThatYouJoined").replaceSuggestions(
        ArgumentSuggestions.strings { info ->
            CompletableFuture.supplyAsync {
                StorageManager.storages.filter {
                    ((it.storageType == GROUP)
                            && it.memberUUIDs.contains(
                        Bukkit.getOfflinePlayers()
                            .firstOrNull { it2 -> it2.name.equals(info.sender.name) }?.uniqueId
                    ))
                }.map { it.storageName }.toTypedArray()
            }.get()
        }
    )

    private fun argOwnerName() = OfflinePlayerArgument("ownerName").replaceSuggestions(
        ArgumentSuggestions.strings { info ->
            CompletableFuture.supplyAsync {
                StorageManager.storages.filter {
                    it.storageType == GROUP
                            && it.memberUUIDs.contains(
                        Bukkit.getOfflinePlayers()
                            .firstOrNull { it2 -> it2.name.equals(info.sender.name) }?.uniqueId
                    )
                            && it.storageName == (info.previousArgs[0] as String)
                }
                    .map { Bukkit.getOfflinePlayer(it.ownerUUID!!).name }.toTypedArray()
            }.get()
        }
    )

    private fun argTargetPlayerName() =
        OfflinePlayerArgument("targetPlayerName").replaceSuggestions(ArgumentSuggestions.strings {
            Bukkit.getOfflinePlayers().map { it.name }.toTypedArray()
        })

    private fun createCommand(): CommandAPICommand = CommandAPICommand("create")
        .withPermission(CommandPermission.NONE)
        .withPermission("groupstorageedit.create")
        .withArguments(StringArgument("storageName"))
        .executesPlayer(
            PlayerCommandExecutor { sender, args ->
                val storageName = args[0] as String

                // Check if the storage exists
                if (StorageManager.storages.any {
                        it.storageType == GROUP
                                && it.ownerUUID == sender.uniqueId
                                && it.storageName == storageName
                    }) {
                    sender.sendMessage("${ChatColor.GREEN}[SmartStorage] ${ChatColor.RED}The storage already exists.")
                    return@PlayerCommandExecutor
                }

                try {
                    SqliteWrapper.create(GROUP, storageName, sender.uniqueId, listOf(sender.uniqueId))
                    sender.sendMessage("${ChatColor.GREEN}[SmartStorage] Storage created as ${ChatColor.UNDERLINE}$storageName${ChatColor.RESET}.")
                } catch (e: Exception) {
                    sender.sendMessage("${ChatColor.GREEN}[SmartStorage] ${ChatColor.RED}An error occurred while creating the storage!")
                    e.printStackTrace()
                    return@PlayerCommandExecutor
                }
            }
        )

    private fun deleteCommand(): CommandAPICommand = CommandAPICommand("delete")
        .withPermission(CommandPermission.NONE)
        .withPermission("groupstorageedit.delete")
        .withArguments(StringArgument("storageNameThatYouOwn").replaceSuggestions(
            ArgumentSuggestions.strings { info ->
                CompletableFuture.supplyAsync {
                    StorageManager.storages.filter {
                        ((it.storageType == GROUP)
                                && it.ownerUUID ==
                                Bukkit.getOfflinePlayers()
                                    .firstOrNull { it2 -> it2.name.equals(info.sender.name) }?.uniqueId
                                )
                    }.map { it.storageName }.toTypedArray()
                }.get()
            }))
        .executesPlayer(
            PlayerCommandExecutor { sender, args ->
                val storageName = args[0] as String
                val ownerUUID = sender.uniqueId
                val storage = StorageManager.storages.firstOrNull {
                    it.storageType == GROUP
                            && it.ownerUUID == ownerUUID
                            && it.storageName == storageName
                }

                if (storage == null) {
                    sender.sendMessage("${ChatColor.GREEN}[SmartStorage] ${ChatColor.RED}The storage does not exist.")
                    return@PlayerCommandExecutor
                }

                try {
                    StorageManager.remove(storage)
                    SqliteWrapper.delete(storage)
                    sender.sendMessage("${ChatColor.GREEN}[SmartStorage] $storageName deleted successfully!")
                } catch (e: Exception) {
                    sender.sendMessage("${ChatColor.GREEN}[SmartStorage] ${ChatColor.RED}An error occurred while deleting the storage!")
                    e.printStackTrace()
                    return@PlayerCommandExecutor
                }
            }
        )

    private fun addMemberCommand(): CommandAPICommand = CommandAPICommand("addmember")
        .withPermission(CommandPermission.NONE)
        .withPermission("groupstorageedit.addmember")
        .withArguments(
            argMemberStorageName(),
            argOwnerName(),
            argTargetPlayerName(),
        )
        .executesPlayer(
            PlayerCommandExecutor { sender, args ->
                val storageName = args[0] as String
                val ownerUUID = (args[1] as OfflinePlayer).uniqueId
                val targetPlayer = args[2] as OfflinePlayer
                val storage = StorageManager.storages.firstOrNull {
                    it.storageType == GROUP
                            && it.ownerUUID == ownerUUID
                            && it.storageName == storageName
                            && it.memberUUIDs.contains(sender.uniqueId)
                }

                if (storage == null) {
                    sender.sendMessage("${ChatColor.GREEN}[SmartStorage] ${ChatColor.RED}The storage does not exist.")
                    return@PlayerCommandExecutor
                }

                if (storage.memberUUIDs.contains(targetPlayer.uniqueId)) {
                    sender.sendMessage("${ChatColor.GREEN}[SmartStorage] ${ChatColor.RED}The player is already a member of the storage.")
                    return@PlayerCommandExecutor
                }

                val result = storage.memberUUIDs.add(targetPlayer.uniqueId)
                val message =
                    if (result) "${ChatColor.GREEN}[SmartStorage] ${targetPlayer.name} has been added to the storage."
                    else "${ChatColor.GREEN}[SmartStorage] ${ChatColor.RED}An error occurred while adding the player to the storage."
                sender.sendMessage(message)
            }
        )

    private fun removeMemberCommand(): CommandAPICommand = CommandAPICommand("removemember")
        .withPermission(CommandPermission.NONE)
        .withPermission("groupstorageedit.removemember")
        .withArguments(
            argMemberStorageName(),
            argOwnerName(),
            argTargetPlayerName(),
        )
        .executesPlayer(
            PlayerCommandExecutor { sender, args ->
                val storageName = args[0] as String
                val ownerUUID = (args[1] as OfflinePlayer).uniqueId
                val targetPlayer = args[2] as OfflinePlayer

                val storage = StorageManager.storages.firstOrNull {
                    it.storageType == GROUP
                            && it.ownerUUID == ownerUUID
                            && it.storageName == storageName
                            && it.memberUUIDs.contains(sender.uniqueId)
                }

                if (storage == null) {
                    sender.sendMessage("${ChatColor.GREEN}[SmartStorage] ${ChatColor.RED}The storage does not exist.")
                    return@PlayerCommandExecutor
                }

                val result = storage.memberUUIDs.add(targetPlayer.uniqueId)
                val message =
                    if (result) "${ChatColor.GREEN}[SmartStorage] ${ChatColor.RED}The player has been removed from the storage."
                    else "${ChatColor.GREEN}[SmartStorage] ${ChatColor.RED}An error occurred while removing the player from the storage."
                sender.sendMessage(message)
            }
        )

    private fun listCommand(): CommandAPICommand = CommandAPICommand("list")
        .withPermission(CommandPermission.NONE)
        .withPermission("groupstorageedit.list")
        .executesPlayer(
            PlayerCommandExecutor { sender, _ ->
                sender.sendMessage(
                    "${ChatColor.GREEN}=== Your Storages ===${ChatColor.RESET}\n" + StorageManager.storages.filter {
                        it.storageType == GROUP
                                && it.memberUUIDs.contains(
                            Bukkit.getOfflinePlayers().firstOrNull { it2 -> it2.name.equals(sender.name) }?.uniqueId
                        )
                    }
                        .joinToString("\n") { "${ChatColor.GOLD}${it.storageName} (${Bukkit.getOfflinePlayer(it.ownerUUID!!).name})" }
                )
            }
        )


}