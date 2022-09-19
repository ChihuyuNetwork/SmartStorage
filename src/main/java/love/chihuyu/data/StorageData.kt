package love.chihuyu.data

import com.google.gson.*
import love.chihuyu.PrivateStorage.Companion.plugin
import love.chihuyu.utils.StorageUtils
import love.chihuyu.utils.StorageUtils.excludePageButton
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.io.*
import java.util.UUID
import kotlin.system.measureTimeMillis

object StorageData {

    private val DATAFILE = File(plugin.dataFolder, "data.json")

    val openStorageInvs = mutableListOf<Inventory>()
    val openStorageItemStacks = mutableListOf<ItemStack>()

    val privateStorages = mutableListOf<StorageInfo>()
    val privateStorageFullDataMap = mutableMapOf<StorageInfo, MutableList<Inventory>>()

    init {
        setup()
    }

    @Throws(
        SecurityException::class,
        IOException::class
    )
    private fun setup() {
        if (!plugin.dataFolder.exists()) plugin.dataFolder.mkdir()
        if (!DATAFILE.exists()) DATAFILE.createNewFile()
    }

    @Throws(
        FileNotFoundException::class,
        JsonParseException::class,
        JsonSyntaxException::class,
        IllegalStateException::class
    )
    private fun loadJson(): JsonObject {
        return JsonParser.parseString(FileReader(DATAFILE).readText()).asJsonObject
    }

    fun import() {
        val json = loadJson()
        val openStorage = json.getAsJsonPrimitive("openStorage").asString

        try {
            this.openStorageItemStacks.addAll(
                StorageUtils
                    .arrayFromBase64(openStorage)
                    .toList()
                    .filterNotNull()
            )
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        }

        val privateStorages = json.getAsJsonObject("privateStorages")

        try {
            privateStorages.entrySet().forEach { storage ->
                val obj = storage.value.asJsonObject

                val storageName = obj.getAsJsonPrimitive("name").asString
                val ownerUUID = UUID.fromString(obj.getAsJsonPrimitive("owner").asString)
                val itemStacks =
                    StorageUtils
                        .arrayFromBase64(obj.getAsJsonPrimitive("items").asString)
                        .filterNotNull()
                        .toMutableList()
                val memberUUIDs = obj.getAsJsonArray("members").map { UUID.fromString(it.asString) }.toMutableSet()

                this.privateStorages.add(StorageInfo(ownerUUID, storageName, itemStacks, memberUUIDs))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }

        plugin.logger.info("Data imported successfully.")
    }

    fun save() {
        val ms = measureTimeMillis {
            if (DATAFILE.exists()) DATAFILE.createNewFile()

            val json = JsonObject()

            val openItems = mutableListOf<ItemStack>()
            openStorageInvs.forEach { openItems.addAll(it.contents.filterNotNull().excludePageButton()) }

            json.addProperty("openStorage", StorageUtils.arrayToBase64(openItems.toTypedArray()))

            StorageUtils.updateStorageInfos()

            json.add(
                "privateStorages",
                JsonObject().apply {
                    privateStorages.forEach { info ->
                        this.add(info.storageName, info.toJson())
                    }
                }
            )

            val writer = PrintWriter(BufferedWriter(FileWriter(DATAFILE)))
            writer.println(GsonBuilder().setPrettyPrinting().create().toJson(json))
            writer.close()
        }
        plugin.logger.info("Data saved successfully (${ms}ms).")
    }


}