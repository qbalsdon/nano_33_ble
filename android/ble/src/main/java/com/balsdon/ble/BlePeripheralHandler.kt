package com.balsdon.ble

import android.content.Context
import java.util.*

/**
 * The interface an Android lifecycle component (Activity, Fragment, Service)
 * must implement in order to utilise the library. The specifics of how data
 * is communicated is left to the user.
 */
interface BlePeripheralHandler {
    val context: Context
    fun log(message: String)
    fun onConnectionStateChange(newState: Int)
    val characteristics: List<UUID>
    fun onCharacteristicChanged(characteristicId: UUID, packetData: String)
    fun enableBluetooth()
    fun onConnected()
    fun onDisconnected()
    fun checkPermission(): Boolean
}