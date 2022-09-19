package love.chihuyu.listeners

import love.chihuyu.data.StorageData
import love.chihuyu.utils.StorageUtils
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

object ClickInventoryEventListener : Listener {

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val inventory = event.clickedInventory ?: return
        val item = event.currentItem ?: return
        val player = event.whoClicked as Player
        fun invData() = StorageUtils.getStorageByInventory(inventory)?.second

        if (item.itemMeta?.hasEnchants() == false) return

        if (invData() != null) {
            StorageUtils.getStorageByInventory(inventory)?.second?.removeIf { it.contents.isEmpty() }

            when (item.type) {
                Material.LIME_WOOL -> {
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f)
                    if (invData()!!.indexOf(inventory).inc() == invData()!!.size) {
                        if (inventory.contents.isNotEmpty()) {
                            val info = StorageUtils.getStorageByInventory(inventory)!!.first
                            val newPage = Bukkit.createInventory(
                                null,
                                54,
                                "${info.storageName} (${Bukkit.getOfflinePlayer(info.ownerUUID).name}) Page - ${
                                    invData()!!.indexOf(inventory).inc().inc()
                                }"
                            )

                            newPage.setItem(52, ItemStack(Material.RED_WOOL).apply {
                                this.addUnsafeEnchantment(Enchantment.MENDING, 1)
                                this.itemMeta = this.itemMeta?.apply {
                                    this.setDisplayName("Previous Page")
                                    this.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                                }
                            })
                            newPage.setItem(53, ItemStack(Material.LIME_WOOL).apply {
                                this.addUnsafeEnchantment(Enchantment.MENDING, 1)
                                this.itemMeta = this.itemMeta?.apply {
                                    this.setDisplayName("Next Page")
                                    this.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                                }
                            })

                            StorageUtils.getStorageByInventory(inventory)?.second?.add(newPage)
                        }
                    }
                    player.openInventory(invData()!![invData()!!.indexOf(inventory).inc()])
                }
                Material.RED_WOOL -> {
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f)
                    val index =
                        if (invData()!!.indexOf(inventory).dec() < 0) 0 else invData()!!.indexOf(inventory).dec()
                    player.openInventory(invData()!![index % invData()!!.size])
                }
                else -> return
            }
            event.isCancelled = true
        } else if (inventory in StorageData.openStorageInvs) {
            when (item.type) {
                Material.LIME_WOOL -> {
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f)
                    if (StorageData.openStorageInvs.indexOf(inventory).inc() == StorageData.openStorageInvs.size) {
                        if (inventory.contents.isNotEmpty()) {
                            val newPage = Bukkit.createInventory(
                                null,
                                54,
                                "Open Storage  Page - ${StorageData.openStorageInvs.indexOf(inventory).inc().inc()}"
                            )

                            newPage.setItem(52, StorageUtils.previousPageButton)
                            newPage.setItem(53, StorageUtils.nextPageButton)

                            StorageData.openStorageInvs.add(newPage)
                        }
                    }
                    player.openInventory(
                        StorageData.openStorageInvs[StorageData.openStorageInvs.indexOf(inventory).inc()]
                    )
                }
                Material.RED_WOOL -> {
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f)
                    val index = if (StorageData.openStorageInvs.indexOf(inventory)
                            .dec() < 0
                    ) 0 else StorageData.openStorageInvs.indexOf(inventory).dec()
                    player.openInventory(StorageData.openStorageInvs[index % StorageData.openStorageInvs.size])
                }
                else -> return
            }
            event.isCancelled = true
        }
    }
}