package love.chihuyu.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import love.chihuyu.data.StorageData

object CommandOpenStorage {

    val main = CommandAPICommand("openstorage")
        .withPermission("privatestorage.openstorage")
        .withPermission(CommandPermission.NONE)
        .withAliases("os")
        .executesPlayer(
            PlayerCommandExecutor { sender, args ->
                sender.openInventory(StorageData.openStorageInv)
            }
        )
}