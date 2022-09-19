package love.chihuyu.utils

import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

inline fun bukkitRunnable(crossinline block: BukkitRunnable.() -> Unit) = object : BukkitRunnable() {
    override fun run() = block()
}

inline fun Plugin.runTaskTimer(delay: Long, period: Long, crossinline block: BukkitRunnable.() -> Unit): BukkitTask {
    return bukkitRunnable(block).runTaskTimer(this, delay, period)
}