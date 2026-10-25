package com.example.habittracker.shared

import platform.Foundation.NSDate
import platform.Foundation.NSTimeZone
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUUID
import platform.Foundation.localTimeZone
import platform.Foundation.timeIntervalSince1970

class IosHabitStorage : HabitStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun read(): String? = defaults.stringForKey("habits")

    override fun write(value: String) {
        defaults.setObject(value, forKey = "habits")
    }

    override fun readFinance(): String? = defaults.stringForKey("finance")

    override fun writeFinance(value: String) {
        defaults.setObject(value, forKey = "finance")
    }

    override fun isStarterPackInstalled(): Boolean = defaults.boolForKey("starter_pack_v2")

    override fun markStarterPackInstalled() {
        defaults.setBool(true, forKey = "starter_pack_v2")
    }
}

actual fun currentEpochDay(): Long {
    val now = NSDate()
    val localSeconds = now.timeIntervalSince1970 +
        NSTimeZone.localTimeZone.secondsFromGMTForDate(now).toDouble()
    return (localSeconds / 86_400.0).toLong()
}

actual fun newHabitId(): String = NSUUID().UUIDString()
