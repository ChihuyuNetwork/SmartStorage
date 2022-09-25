package love.chihuyu.storage

enum class StorageType {
    SERVER,
    PERSONAL,
    GROUP;

    companion object {
        private val SQL_NAMES = mapOf(
            SERVER to "server",
            PERSONAL to "personal",
            GROUP to "group"
        )

        fun fromSqlName(sqlName: String) = SQL_NAMES.entries.firstOrNull { it.value == sqlName }?.key
    }

    override fun toString() = SQL_NAMES[this]!!
}
