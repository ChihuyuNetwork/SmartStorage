package love.chihuyu

import love.chihuyu.commands.CommandOpenStorage
import love.chihuyu.commands.CommandPrivateStorage
import love.chihuyu.commands.CommandPsedit
import love.chihuyu.data.StorageData
import love.chihuyu.utils.StorageUtil
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

class PrivateStorage : JavaPlugin(), Listener {

    companion object {
        lateinit var plugin: JavaPlugin
    }

    init {
        plugin = this
    }

    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)

        StorageData.import()

        StorageData.openStorage.chunked(52).forEachIndexed { index, itemStacks ->
            val inv = Bukkit.createInventory(null, 54, "Open Storage  Page - ${index.inc()}")
            itemStacks.forEach { inv.addItem(it) }
            StorageData.openStorageInv.add(inv)
        }

        StorageData.privateStorages.forEach {
            it.items.chunked(52).forEachIndexed { index, itemStacks ->
                val inv = Bukkit.createInventory(null, 54, "${it.name} (${Bukkit.getOfflinePlayer(it.owner).name}) Page - ${index.inc()}")
                itemStacks.forEach { item -> inv.addItem(item) }
                StorageData.privateStorageInv.getOrPut(it) { mutableListOf() }.add(inv)
            }
        }

        CommandPsedit.main.register()
        CommandPrivateStorage.main.register()
        CommandOpenStorage.main.register()
    }

    override fun onDisable() {
        StorageData.save()
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val inventory = event.clickedInventory ?: return
        val item = event.currentItem ?: return
        val player = event.whoClicked as Player
        fun invData() = StorageUtil.getStorageByInventory(inventory)?.second

        if (item.itemMeta?.hasEnchants() == true) {
            if (invData() != null) {
                StorageUtil.getStorageByInventory(inventory)?.second?.removeIf { it.contents.isEmpty() }
                when (item.type) {
                    Material.LIME_WOOL -> {
                        player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f)
                        if (invData()!!.indexOf(inventory).inc() == invData()!!.size) {
                            if (inventory.contents.isNotEmpty()) {
                                val info = StorageUtil.getStorageByInventory(inventory)!!.first
                                val newPage = Bukkit.createInventory(null, 54, "${info.name} (${Bukkit.getOfflinePlayer(info.owner).name}) Page - ${invData()!!.indexOf(inventory).inc().inc()}")
                                newPage.setItem(53, ItemStack(Material.RED_WOOL).apply {
                                    this.addUnsafeEnchantment(Enchantment.MENDING, 1)
                                    this.itemMeta = this.itemMeta?.apply {
                                        this.setDisplayName("Previous Page")
                                        this.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                                    }
                                })
                                StorageUtil.getStorageByInventory(inventory)?.second?.add(newPage)
                            }
                        }
                        player.openInventory(invData()!![invData()!!.indexOf(inventory).inc()])
                    }
                    Material.RED_WOOL -> {
                        player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f)
                        val index = if (invData()!!.indexOf(inventory).dec() < 0) 0 else invData()!!.indexOf(inventory).dec()
                        player.openInventory(invData()!![index % invData()!!.size])
                    }
                    else -> return
                }
                event.isCancelled = true
            } else if (inventory in StorageData.openStorageInv) {
                when (item.type) {
                    Material.LIME_WOOL -> {
                        player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f)
                        if (StorageData.openStorageInv.indexOf(inventory).inc() == StorageData.openStorageInv.size) {
                            if (inventory.contents.isNotEmpty()) {
                                val newPage = Bukkit.createInventory(null, 54, "Open Storage  Page - ${StorageData.openStorageInv.indexOf(inventory).inc().inc()}")
                                newPage.setItem(53, ItemStack(Material.RED_WOOL).apply {
                                    this.addUnsafeEnchantment(Enchantment.MENDING, 1)
                                    this.itemMeta = this.itemMeta?.apply {
                                        this.setDisplayName("Previous Page")
                                        this.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                                    }
                                })
                                StorageData.openStorageInv.add(newPage)
                            }
                        }
                        player.openInventory(StorageData.openStorageInv[StorageData.openStorageInv.indexOf(inventory).inc()])
                    }
                    Material.RED_WOOL -> {
                        player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f)
                        val index = if (StorageData.openStorageInv.indexOf(inventory).dec() < 0) 0 else StorageData.openStorageInv.indexOf(inventory).dec()
                        player.openInventory(StorageData.openStorageInv[index % StorageData.openStorageInv.size])
                    }
                    else -> return
                }
                event.isCancelled = true
            }
        }

    }
}