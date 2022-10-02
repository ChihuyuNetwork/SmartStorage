package love.chihuyu.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.OfflinePlayerArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import love.chihuyu.storage.StorageManager
import love.chihuyu.storage.StorageType.GROUP
import love.chihuyu.wrappers.SqliteWrapper
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import java.util.concurrent.CompletableFuture

object CommandGroupStorageEdit {

    val main: CommandAPICommand = CommandAPICommand("groupstorageedit")
        .withAliases("gse", "gsedit")
        .withPermission("smartstorage.groupstorageedit")
        .withSubcommands(
            createCommand(),
            deleteCommand(),
            forceDeleteCommand(),
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
            Bukkit.getOfflinePlayers().mapNotNull { it.name }.toTypedArray()
        })

    private fun argTargetMemberName() =
        OfflinePlayerArgument("targetPlayerName").replaceSuggestions(ArgumentSuggestions.strings { info ->
                (StorageManager.storages.firstOrNull {
                    it.storageName == info.previousArgs[0] as String
                            && it.ownerUUID == (info.previousArgs[1] as OfflinePlayer).uniqueId
                }?.memberUUIDs?.mapNotNull { Bukkit.getOfflinePlayer(it).name } ?: listOf()).toTypedArray()
        })

    private fun createCommand(): CommandAPICommand = CommandAPICommand("create")
        .withPermission("smartstorage.groupstorageedit.create")
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
        .withPermission("smartstorage.groupstorageedit.delete")
        .withArguments(StringArgument("storageNameThatYouOwn")
            .replaceSuggestions(
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

                sender.spigot().sendMessage(
                    TextComponent("${net.md_5.bungee.api.ChatColor.GREEN}[SmartStorage] ${net.md_5.bungee.api.ChatColor.RED}Storage can't restore. Are you sure? ${net.md_5.bungee.api.ChatColor.BOLD}[Confirm]").apply {
                        this.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/gsedit forcedelete $storageName")
                        this.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("Click to remove storage"))
                    }
                )
            }
        )

    private fun forceDeleteCommand(): CommandAPICommand = CommandAPICommand("forcedelete")
        .withPermission("smartstorage.groupstorageedit.forcedelete")
        .withArguments(StringArgument("storageNameThatYouOwn")
            .replaceSuggestions(
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
        .withPermission("smartstorage.groupstorageedit.addmember")
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

                if ("${targetPlayer.name}" == "null") {
                    sender.sendMessage("${ChatColor.GREEN}[SmartStorage] ${ChatColor.RED}The player is not valid.")
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
        .withPermission("smartstorage.groupstorageedit.removemember")
        .withArguments(
            argMemberStorageName(),
            argOwnerName(),
            argTargetMemberName(),
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

                if (targetPlayer.uniqueId == ownerUUID) {
                    sender.sendMessage("${ChatColor.GREEN}[SmartStorage] ${ChatColor.RED}Can't remove owner from members.")
                    return@PlayerCommandExecutor
                }

                val result = storage.memberUUIDs.remove(targetPlayer.uniqueId)
                val message =
                    if (result) "${ChatColor.GREEN}[SmartStorage] ${ChatColor.RED}The player has been removed from the storage."
                    else "${ChatColor.GREEN}[SmartStorage] ${ChatColor.RED}An error occurred while removing the player from the storage."
                sender.sendMessage(message)
            }
        )

    private fun listCommand(): CommandAPICommand = CommandAPICommand("list")
        .withPermission("smartstorage.groupstorageedit.list")
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