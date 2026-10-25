package com.example.habittracker.shared

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    HabitTrackerApp(storage = IosHabitStorage())
}
