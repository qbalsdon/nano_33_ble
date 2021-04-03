package com.balsdon.nano33ble

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ConfigurationInfo
import android.content.pm.PackageManager
import android.graphics.Color

fun log(category: String, message: String) {
    android.util.Log.d(category,"[$category]: $message")
}

fun Context.toast(message: String) {
    android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
}

fun Int.colourToFloatArray(): FloatArray = floatArrayOf(
    Color.red(this) / 255f,
    Color.green(this) / 255f,
    Color.blue(this) / 255f,
    1.0f
)

