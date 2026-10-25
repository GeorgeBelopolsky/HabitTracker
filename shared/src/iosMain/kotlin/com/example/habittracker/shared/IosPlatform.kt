package com.example.habittracker.shared

import platform.Foundation.NSDate
import platform.Foundation.NSTimeZone
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUUID

class IosHabitStorage : HabitStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun read(): String? = defaults.stringForKey("habits")

    override fun write(value: String) {
        defaults.setObject(value, forKey = "habits")
    }
}

actual fun currentEpochDay(): Long {
    val now = NSDate()
    val localSeconds = now.timeIntervalSince1970 +
        NSTimeZone.localTimeZone.secondsFromGMTForDate(now).toDouble()
    return (localSeconds / 86_400.0).toLong()
}

actual fun newHabitId(): String = NSUUID().UUIDString()
