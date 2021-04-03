package com.balsdon.ble

import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import java.util.*

class BleCentral(
    private val manager: BlePeripheralHandler,
    private val deviceId: UUID,
    private val autoSubscribeAll: Boolean = false
) {
    companion object {
        val subscriptionDescriptor: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }

    private var gatt: BluetoothGatt? = null
    private var gattService: BluetoothGattService? = null
        private set

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        (manager.context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
            .adapter
    }

    private val scanner: BluetoothLeScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    init {
        manager.onDisconnected()
    }

    fun scanForPeripheral() {
        if (!bluetoothAdapter.isEnabled) {
            manager.enableBluetooth()
        } else if (!manager.checkPermission()) {
            return
        }
        manager.log("BT ENABLED: SCANNING FOR DEVICES")
        scanner.startScan(scanCallback)
    }

    fun stopScan() {
        scanner.stopScan(scanCallback)
    }

    private val scanCallback: ScanCallback = object : ScanCallback() {
        private fun reportNoScanRecord(address: String) {
            manager.log("Device [$address] has no scan record")
            return
        }

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device: BluetoothDevice = result.device
            result.scanRecord?.apply {
                val name = deviceName
                var UUID: String? = null
                if (serviceUuids != null) {
                    for (pId in serviceUuids) {
                        if (pId.uuid.toString() == deviceId.toString()) {
                            UUID = pId.uuid.toString()
                        }
                    }
                }
                if (UUID == null) {
                    manager.log("Discovered Device [$name]. Continuing search")
                    return
                }
                manager.log("Peripheral [${UUID}] located on Device [$name]. Attempting connection")
                gatt = device.connectGatt(manager.context, true, gattCallback)
                stopScan()
                super.onScanResult(callbackType, result)
            } ?: reportNoScanRecord(device.address)
        }
    }

    private fun closeGatt() {
        if (gatt == null) return

        manager.onDisconnected()
        gatt?.close()
        gatt = null
        scanForPeripheral()
    }

    private val gattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            manager.onConnectionStateChange(newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                manager.log("Connected to device GATT. Discovering services")
                //TODO:[0] Handle null better
                this@BleCentral.gatt!!.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                manager.log("Disconnected from GATT server. Continuing scanning")
                closeGatt()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //TODO:[0] Handle null better
                if (this@BleCentral.gatt!!.services != null) {
                    for (service in this@BleCentral.gatt!!.services) {
                        if (service.uuid.toString() == deviceId.toString()) {
                            gattService = service
                            manager.onConnected()
                            manager.log("Service discovered")
                            if (autoSubscribeAll) {
                                subscribeToAllCharacteristics()
                            } else {
                                subscribe(manager.characteristics)
                            }
                        }
                    }
                }
            } else {
                manager.log("onServicesDiscovered received: [$status]")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                manager.log("onCharacteristicRead received: [${characteristic.uuid}] value: [${characteristic.value}]")
            } else {
                manager.log("onCharacteristicRead fail received: [$status]")
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                manager.log("onCharacteristicWrite received: [${characteristic.uuid.toString()}] value: [${characteristic.value}]")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val packet = String(characteristic.value)
            //manager.log("onCharacteristicChanged received: [${characteristic.uuid}] Value: [${packet}]")
            manager.onCharacteristicChanged(characteristic.uuid, packet)
        }
    }

    //TODO:[0] Handle null better
    private fun findCharacteristicById(id: UUID): BluetoothGattCharacteristic? {
        return if (gattService!!.characteristics != null) {
            gattService!!.getCharacteristic(id)
        } else null
    }

    private fun subscribeToAllCharacteristics() {
        val charList = gattService!!.characteristics
        if (charList == null) {
            manager.log("There are no characteristics present")
        } else {

            manager.log("There are [${charList.size}] characteristics present")
            charList.forEach {
                subscribeTo(it)
            }
        }
    }

    private fun subscribe(subscriptions: List<UUID>) =
        subscriptions.forEach {
            val characteristic = findCharacteristicById(it)
            if (characteristic == null) {
                manager.log("Characteristic does not exist")
                return
            }
            subscribeTo(characteristic)
        }

    private fun subscribeTo(characteristic: BluetoothGattCharacteristic) {
        manager.log("Subscribing to characteristic [${characteristic.uuid}]")
        with(gatt!!) {
            setCharacteristicNotification(characteristic, true)
            writeDescriptor(characteristic.getDescriptor(subscriptionDescriptor).apply {
                value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            })
        }
    }

    fun writeCharacteristic(characteristicId: UUID, data: String?) {
        val characteristic = findCharacteristicById(characteristicId)
        if (characteristic != null) {
            characteristic.setValue(data)
            gatt!!.writeCharacteristic(characteristic)
            manager.log("Wrote [$data] to [$characteristicId]")
        } else {
            manager.log("[$characteristicId] not found on device")
        }
    }

    fun readCharacteristic(characteristicId: UUID) {
        val characteristic = findCharacteristicById(characteristicId) ?: return
        gatt!!.readCharacteristic(characteristic)
    }
}