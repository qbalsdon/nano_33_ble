# nano_33_ble

The repository has two elements:

1. The Arduino code for the [Arduino Nano 33 BLE board][0]
1. The Android Kotlin code for connection

There will probably be project-oriented branches. I'm not a huge fan or having a repository per project based on a particular technology.

## 1. The Arduino Code

At the moment fairly simple. A basic copy of the [example][3].


## 2. The Android Code

This is going to be a Kotlin port of my [existing code][5], which is currently screaming for an update (hopefully I can delete that repository once this is running). I want to make the BLE code a module that can be imported, rather than an entire project. If I add Raspberry Pi's again, I'll make a folder on this project and [delete that legacy library][4].

### TODO:
:x: There are a lot of null values that need to be properly managed
:x: On initial start, the Bluetooth and location seem disconnected
:x: Unit test BleCentral - there is a LOOOT of lifecycle there
    - :x: Create an adapter for the bluetooth components
    - :x: Separate the object components

## Notes

#### 1. No port detected

There have been instances where I cannot see the connection port and the orange light has been [regularly flashing][2]. Thanks to the forums I was able to reset the device and upload NEW code that stopped it from crashing on boot. If there is a crash in the code you can get stuck in this loop where you reset the device, upload the code, which resets the device and the code crashes.  

#### 2. Demo code compilation

I had to modify `accelerometerBLEZ.writeValue(bleBuffer, writeLength);` to include a third parameter which I just set to true [REFERENCE][1].

[0]: https://store.arduino.cc/arduino-nano-33-ble
[1]: https://forum.arduino.cc/index.php?topic=730824.0
[2]: https://forum.arduino.cc/index.php?topic=679763.msg4579785#msg4579785
[3]: https://github.com/DaleGia/Nano33BLESensor/blob/master/examples/Nano33BLESensorExample_IMU/Nano33BLESensorExample_IMU.ino
[4]: https://github.com/qbalsdon/BLE_Rpi_Peripheral
[5]: https://github.com/qbalsdon/BLE_Android
