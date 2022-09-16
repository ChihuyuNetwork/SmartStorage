package love.chihuyu.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.OfflinePlayerArgument
import dev.jorel.commandapi.arguments.PlayerArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.CommandExecutor
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import love.chihuyu.data.StorageData
import love.chihuyu.data.StorageInfo
import love.chihuyu.utils.StorageUtil
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import java.util.concurrent.CompletableFuture

object CommandPsedit {

    val main = CommandAPICommand("psedit")
        .withSubcommands(
            CommandAPICommand("create")
                .withPermission("privatestorage.create")
                .withPermission(CommandPermission.NONE)
                .withArguments(StringArgument("name"))
                .executesPlayer(
                    PlayerCommandExecutor { sender, args ->
                        val name = args[0] as String
                        if (StorageUtil.getStoragesByOwner(sender.uniqueId).any { it.key.name == name }) {
                            sender.sendMessage("The storage already created in yours.")
                            return@PlayerCommandExecutor
                        }
                        val info = StorageInfo(name, sender.uniqueId, mutableListOf(), mutableSetOf(sender.uniqueId))
                        val inv = Bukkit.createInventory(null, 54, "$name (${Bukkit.getOfflinePlayer(info.owner).name}) Page - 1")
                        StorageData.privateStorageInv[info] = mutableListOf(inv)
                        StorageUtil.updateStorageInfos()
                        sender.sendMessage("Storage created as ${ChatColor.UNDERLINE}$name${ChatColor.RESET}.")
                    }
                ),
            CommandAPICommand("delete")
                .withPermission("privatestorage.delete")
                .withPermission(CommandPermission.NONE)
                .withArguments(
                    StringArgument("storageName").replaceSuggestions(
                        ArgumentSuggestions.strings { info ->
                            CompletableFuture.supplyAsync { StorageUtil.getJoinedStorages(Bukkit.getOfflinePlayer(info.sender.name).uniqueId).map { it.key.name }.toTypedArray() }.get()
                        }
                    ),
                    OfflinePlayerArgument("owner").replaceSuggestions(
                        ArgumentSuggestions.strings { info ->
                            CompletableFuture.supplyAsync { StorageUtil.getDuplicatedStorages(info.previousArgs[0] as String).map { Bukkit.getOfflinePlayer(it.first.owner).name }.toTypedArray() }.get()
                        }
                    )
                )
                .executesPlayer(
                    PlayerCommandExecutor { sender, args ->
                        val name = args[0] as String
                        val owner = (args[1] as OfflinePlayer).uniqueId
                        val storage = StorageUtil.getExactStorage(owner, name)
                        if (storage == null) {
                            sendErrorNotFound(sender)
                            return@PlayerCommandExecutor
                        }
                        StorageData.privateStorageInv.remove(storage)
                        StorageUtil.updateStorageInfos()
                        sender.sendMessage("The storage ${ChatColor.UNDERLINE}$name${ChatColor.RESET} is deleted.")
                    }
                ),
            CommandAPICommand("addmember")
                .withPermission("privatestorage.addmember")
                .withPermission(CommandPermission.NONE)
                .withArguments(
                    StringArgument("storageName").replaceSuggestions(
                        ArgumentSuggestions.strings { info ->
                            CompletableFuture.supplyAsync { StorageUtil.getJoinedStorages(Bukkit.getOfflinePlayer(info.sender.name).uniqueId).map { it.key.name }.toTypedArray() }.get()
                        }
                    ),
                    OfflinePlayerArgument("owner").replaceSuggestions(
                        ArgumentSuggestions.strings { info ->
                            CompletableFuture.supplyAsync { StorageUtil.getDuplicatedStorages(info.previousArgs[0] as String).map { Bukkit.getOfflinePlayer(it.first.owner).name }.toTypedArray() }.get()
                        }
                    ),
                    OfflinePlayerArgument("member").replaceSuggestions(ArgumentSuggestions.strings {
                        Bukkit.getOfflinePlayers().map { it.name }.toTypedArray()
                    })
                )
                .executesPlayer(
                    PlayerCommandExecutor { sender, args ->
                        val name = args[0] as String
                        val owner = (args[1] as OfflinePlayer).uniqueId
                        val member = args[2] as OfflinePlayer
                        val storage = StorageUtil.getExactStorage(owner, name)
                        if (storage == null) {
                            sendErrorNotFound(sender)
                            return@PlayerCommandExecutor
                        }
                        val result = storage.members.add(member.uniqueId)
                        StorageUtil.updateStorageInfos()
                        if (result) {
                            sender.sendMessage("The member successfuly added to ${ChatColor.UNDERLINE}$name${ChatColor.RESET}.")
                        } else {
                            sender.sendMessage("The member is already in ${ChatColor.UNDERLINE}$name${ChatColor.RESET}.")
                        }
                    }
                ),
            CommandAPICommand("removemember")
                .withPermission("privatestorage.removemember")
                .withPermission(CommandPermission.NONE)
                .withArguments(
                    StringArgument("storageName").replaceSuggestions(
                        ArgumentSuggestions.strings { info ->
                            CompletableFuture.supplyAsync { StorageUtil.getJoinedStorages(Bukkit.getOfflinePlayer(info.sender.name).uniqueId).map { it.key.name }.toTypedArray() }.get()
                        }
                    ),
                    OfflinePlayerArgument("owner").replaceSuggestions(
                        ArgumentSuggestions.strings { info ->
                            CompletableFuture.supplyAsync { StorageUtil.getDuplicatedStorages(info.previousArgs[0] as String).map { Bukkit.getOfflinePlayer(it.first.owner).name }.toTypedArray() }.get()
                        }
                    ),
                    OfflinePlayerArgument("member").replaceSuggestions(ArgumentSuggestions.strings {
                        Bukkit.getOfflinePlayers().map { it.name }.toTypedArray()
                    })
                )
                .executesPlayer(
                    PlayerCommandExecutor { sender, args ->
                        val name = args[0] as String
                        val owner = (args[1] as OfflinePlayer).uniqueId
                        val member = args[2] as OfflinePlayer
                        val storage = StorageUtil.getExactStorage(owner, name)
                        if (storage == null) {
                            sendErrorNotFound(sender)
                            return@PlayerCommandExecutor
                        }
                        val result = storage.members.remove(member.uniqueId)
                        StorageUtil.updateStorageInfos()
                        if (result) {
                            sender.sendMessage("The member successfuly removed from ${ChatColor.UNDERLINE}$name${ChatColor.RESET}.")
                        } else {
                            sender.sendMessage("The member not found in ${ChatColor.UNDERLINE}$name${ChatColor.RESET}.")
                        }
                    }
                ),
            CommandAPICommand("save")
                .withPermission("privatestorage.save")
                .withPermission(CommandPermission.OP)
                .executes(
                    CommandExecutor { sender, args ->
                        StorageData.save()
                        sender.sendMessage("Datas saved to file.")
                    }
                ),
            CommandAPICommand("list")
                .withPermission("privatestorage.list")
                .withPermission(CommandPermission.NONE)
                .executesPlayer(
                    PlayerCommandExecutor { sender, args ->
                        sender.sendMessage(
                            "=== Your Storages ===\n" + StorageUtil.getJoinedStorages(sender.uniqueId).keys.joinToString(
                                "\n"
                            ) { "${ChatColor.GOLD}${it.name} (${Bukkit.getOfflinePlayer(it.owner).name})" }
                        )
                    }
                )
        )

    private fun sendErrorNotFound(sender: CommandSender) {
        sender.sendMessage("${ChatColor.RED}The storage not found.")
    }
}