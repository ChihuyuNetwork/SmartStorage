package love.chihuyu.data

import com.google.gson.*
import love.chihuyu.PrivateStorage.Companion.plugin
import love.chihuyu.utils.ItemUtil
import love.chihuyu.utils.StorageUtil
import love.chihuyu.utils.StorageUtil.excludePageButton
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.io.*
import java.util.UUID

object StorageData {

    private val DATAFILE = File(plugin.dataFolder, "data.json")

    val openStorage = mutableListOf<ItemStack>()
    val openStorageInv = mutableListOf<Inventory>()
    val privateStorageInv = mutableMapOf<StorageInfo, MutableList<Inventory>>()
    val privateStorages = mutableListOf<StorageInfo>()

    init {
        setup()
    }

    private fun setup() {
        if (!plugin.dataFolder.exists()) plugin.dataFolder.mkdir()
        if (!DATAFILE.exists()) DATAFILE.createNewFile()
    }

    fun save() {
        if (DATAFILE.exists()) DATAFILE.createNewFile()
        val json = JsonObject()

        val openItems = mutableListOf<ItemStack>()
        openStorageInv.forEach { openItems.addAll(it.contents.filterNotNull().excludePageButton()) }
        json.addProperty("openStorage", ItemUtil.itemStackArrayToBase64(openItems.toTypedArray()))

        StorageUtil.updateStorageInfos()

        json.add(
            "privateStorages",
            JsonObject().apply {
                privateStorages.forEach { info ->
                    this.add(info.name, info.toJson())
                }
            }
        )

        val writer = PrintWriter(BufferedWriter(FileWriter(DATAFILE)))
        writer.println(GsonBuilder().setPrettyPrinting().create().toJson(json))
        writer.close()
        plugin.logger.info("Data saved successfully.")
    }

    fun import() {
        val json = try {
            JsonParser.parseString(FileReader(DATAFILE).readText()).asJsonObject
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        val openStorage = json.getAsJsonPrimitive("openStorage").asString

        try {
            this.openStorage.addAll(ItemUtil.itemStackArrayFromBase64(openStorage)!!.toList().filterNotNull())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val privateStorages = json.getAsJsonObject("privateStorages")

        try {
            privateStorages.entrySet().forEach { storage ->
                val obj = storage.value.asJsonObject
                val name = obj.getAsJsonPrimitive("name").asString
                val owner = UUID.fromString(obj.getAsJsonPrimitive("owner").asString)
                val items = ItemUtil.itemStackArrayFromBase64(obj.getAsJsonPrimitive("items").asString)!!.filterNotNull().toMutableList()
                val players = obj.getAsJsonArray("members").map { UUID.fromString(it.asString) }.toMutableSet()
                this.privateStorages.add(StorageInfo(name, owner, items, players))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
        plugin.logger.info("Data imported successfully.")
    }
}