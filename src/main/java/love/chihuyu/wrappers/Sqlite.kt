package love.chihuyu.wrappers

import love.chihuyu.SmartStorage.Companion.plugin
import love.chihuyu.storage.*
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.util.*

object SmartStorageTable : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    //TODO storageTypeをenumeration化する
    val storageType = text("storage_type") // "server" | "personal" | "group"
    val storageName = text("storage_name").nullable()
    val ownerUUID = uuid("owner_uuid").nullable()
    val memberUUIDs = text("member_uuids") // uuid,uuid2,uuid3 ... uuidN
    val rawItemStacks = text("raw_item_stacks") // base64 encoded ItemStacks
}

object SqliteWrapper {
    fun initialize() {
        val dataFolder = File(plugin.dataFolder.path)
        if (!dataFolder.exists()) {
            dataFolder.mkdir()
        }
        val file = File(plugin.dataFolder, "smart-storage.db")
        if (!file.exists()) {
            file.createNewFile()
        }
        Database.connect("jdbc:sqlite:${file.path}", "org.sqlite.JDBC")

        transaction {
            SchemaUtils.createMissingTablesAndColumns(SmartStorageTable)
        }
    }

    fun import() = transaction {
        SmartStorageTable.selectAll().forEach {
            val uuidCsv = it[SmartStorageTable.memberUUIDs]
            val uuids = if (uuidCsv == "") mutableSetOf() else uuidCsv.split(",").map { uuid -> UUID.fromString(uuid) }
                .toMutableSet()

            val storage = SmartStorage(
                it[SmartStorageTable.id],
                StorageType.fromSqlName(it[SmartStorageTable.storageType]) ?: return@forEach,
                it[SmartStorageTable.storageName],
                it[SmartStorageTable.ownerUUID],
                uuids,
                it[SmartStorageTable.rawItemStacks]
            )
            StorageManager.add(storage)
        }
    }

    fun create(
        _storageType: StorageType,
        _storageName: String?,
        _ownerUUID: UUID?,
        _memberUUIDs: List<UUID>?,
    ) = transaction {
        val newStorage = SmartStorageTable.insert {
            it[storageType] = _storageType.toString()
            it[storageName] = _storageName
            it[ownerUUID] = _ownerUUID
            it[memberUUIDs] = _memberUUIDs?.joinToString(",") ?: ""
            it[rawItemStacks] = arrayToBase64(listOf(ItemStack(Material.AIR)).toTypedArray())
        }.resultedValues?.firstOrNull()

        if (newStorage != null) {
            val uuidCsv = newStorage[SmartStorageTable.memberUUIDs]
            val uuids = if (uuidCsv == "") mutableSetOf() else uuidCsv.split(",").map { uuid -> UUID.fromString(uuid) }
                .toMutableSet()

            val storage = SmartStorage(
                newStorage[SmartStorageTable.id],
                StorageType.fromSqlName(newStorage[SmartStorageTable.storageType]) ?: return@transaction,
                newStorage[SmartStorageTable.storageName],
                newStorage[SmartStorageTable.ownerUUID],
                uuids,
                newStorage[SmartStorageTable.rawItemStacks]
            )
            StorageManager.add(storage)
        } else {
            error("Failed to create new storage")
        }
    }

    fun delete(storage: SmartStorage) = transaction {
        SmartStorageTable.deleteWhere { SmartStorageTable.id eq storage.id }
    }

    fun save(storage: SmartStorage) = transaction {
        val inventories = StorageManager.inventories[storage.id] ?: return@transaction

        val itemStacks = mutableListOf<ItemStack?>()
        inventories.forEach {
            val lst = it.contents.asList().excludePaginationButton().dropLastWhile { itemStack -> itemStack == null };
            itemStacks.addAll(lst)
        }

        SmartStorageTable.update({ SmartStorageTable.id eq storage.id }) {
            it[storageType] = storage.storageType.toString()
            it[storageName] = storage.storageName
            it[ownerUUID] = storage.ownerUUID
            it[memberUUIDs] = storage.memberUUIDs.joinToString(",")
            it[rawItemStacks] = arrayToBase64(itemStacks.toTypedArray())
        }
    }

    fun saveAll() = transaction {
        StorageManager.inventories.forEach { (id, inventories) ->
            val smartStorage = StorageManager.storages.firstOrNull { it.id == id } ?: return@forEach
            val itemStacks = mutableListOf<ItemStack?>()
            inventories.forEach { itemStacks.addAll(it.contents.asList()
                .excludePaginationButton().dropLastWhile { itemStack -> itemStack == null })
            }

            SmartStorageTable.update({ SmartStorageTable.id eq smartStorage.id }) {
                it[storageType] = smartStorage.storageType.toString()
                it[storageName] = smartStorage.storageName
                it[ownerUUID] = smartStorage.ownerUUID
                it[memberUUIDs] = smartStorage.memberUUIDs.joinToString(",")
                it[rawItemStacks] = arrayToBase64(itemStacks.toTypedArray())
            }
        }
    }
}