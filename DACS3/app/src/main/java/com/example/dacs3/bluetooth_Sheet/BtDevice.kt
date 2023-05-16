package com.example.dacs3.bluetooth_Sheet

import android.bluetooth.BluetoothDevice
import android.os.ParcelUuid


data class BtDevice(
    var device: BluetoothDevice?,
    var uuid: MutableList<ParcelUuid>?,
    var rssi: Int
)