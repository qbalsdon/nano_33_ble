package com.balsdon.nano33ble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothProfile.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.balsdon.ble.BlePeripheralHandler
import com.balsdon.ble.BleCentral
import com.balsdon.nano33ble.render.SceneView
import com.balsdon.nano33ble.render.SurfaceRenderer
import java.util.*

class Nano33BleActivity : AppCompatActivity(), BlePeripheralHandler {
    companion object {
        private val peripheralUUID = UUID.fromString("22a0e806-a503-4866-943c-839fe9415460")
        private val rotationCharacteristic = UUID.fromString("00000010-0000-1000-8000-00805f9b34fb")

        private val REQUEST_ENABLE_BT = 10001
        private val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }

    override val characteristics: List<UUID>
        get() = listOf(rotationCharacteristic)

    private val centralManager: BleCentral by lazy {
        BleCentral(this, peripheralUUID)
    }

    private val stateLabel: TextView by lazy {
        findViewById(R.id.connection_state)
    }

    private val labelContainer: ViewGroup by lazy {
        findViewById(R.id.valuesContainer)
    }

    private val xValue: TextView by lazy {
        findViewById(R.id.xValue)
    }

    private val yValue: TextView by lazy {
        findViewById(R.id.yValue)
    }

    private val zValue: TextView by lazy {
        findViewById(R.id.zValue)
    }

    private val glSurfaceView: GLSurfaceView by lazy {
        findViewById(R.id.glSurface)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            toast(getString(R.string.ble_not_supported))
            finish();
        }
        stateLabel.text = getString(R.string.state_started)
        centralManager.scanForPeripheral()
    }

    override fun onPause() {
        glSurfaceView.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    centralManager.scanForPeripheral()
                } else {
                    log("Permission denied. App is a brick")
                }
                return
            }
        }
    }

    override val context: Context
        get() = baseContext

    override fun log(message: String) = log("BLE_LOG", message)

    override fun onConnectionStateChange(newState: Int) = runOnUiThread {
        labelContainer.visibility = View.GONE
        stateLabel.text =
            getString(
                when (newState) {
                    STATE_CONNECTED -> {
                        labelContainer.visibility = View.VISIBLE
                        R.string.state_connected
                    }
                    STATE_DISCONNECTED -> R.string.state_disconnected
                    STATE_CONNECTING -> R.string.state_connecting
                    STATE_DISCONNECTING -> R.string.state_disconnecting
                    else -> R.string.state_unkown
                }
            )
    }

    private fun setData(rot: String) {
        with(rot.split(",")) {
            xValue.text = this[0]
            yValue.text = this[1]
            zValue.text = this[2]
            ((glSurfaceView as SceneView).renderer as SurfaceRenderer).cube.apply {
                rotationX = this@with[0].toFloat()
                rotationY = this@with[1].toFloat()
                rotationZ = this@with[2].toFloat()
            }
        }
    }

    override fun onCharacteristicChanged(characteristicId: UUID, packetData: String) {
        //log("    ~~> $characteristicId: [$packetData]")
        when (characteristicId) {
            characteristics[0] -> setData(packetData)
            else -> log("NOT HANDLING [$characteristicId]")
        }
    }

    override fun enableBluetooth() =
        startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT)

    override fun onConnected() = Unit

    override fun onDisconnected() = Unit

    override fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this)
                    .setTitle(R.string.permission_rationale_title)
                    .setMessage(R.string.permission_rationale_message)
                    .setCancelable(true)
                    .show()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
                );
            }
            return false
        }

        return true
    }
}


