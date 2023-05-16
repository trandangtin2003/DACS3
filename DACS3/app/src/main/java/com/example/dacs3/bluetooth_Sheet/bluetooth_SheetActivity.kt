package com.example.dacs3.bluetooth_Sheet

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.dacs3.MainActivity
import com.example.dacs3.R
import com.example.dacs3.admin.DashboardActivity
import com.example.dacs3.add_HP.HpAddActivity
import com.example.dacs3.admin.bluetoothListAdapter
import com.example.dacs3.dataBase
import com.example.dacs3.databinding.ActivityBluetoothSheetBinding
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class bluetooth_SheetActivity : AppCompatActivity() {

    private val REQUEST_PERMISSION_CODE = 1001
    private val REQUEST_ENABLE_BT = 1002
    private var scanHandler: Handler? = null

    private val SCAN_INTERVAL = 500 //(đại diện cho khoảng thời gian giữa các lần quét)

    private lateinit var binding: ActivityBluetoothSheetBinding
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothListAdapter: bluetoothListAdapter
    private val scanDevices = ArrayList<BtDevice>()

    private lateinit var hplinkGuest : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBluetoothSheetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (dataBase.isGuest){
            binding.logoutBtn.visibility = View.GONE
            binding.transfer.setImageResource(R.drawable.ic_settings_gray)
            binding.transfer.setOnClickListener {
                // Create an intent to start the QR scanning activity
                startActivity(Intent(this, HpAddActivity::class.java))
            }
        }else binding.transfer.visibility = View.GONE
        binding.subTitleTv.text = dataBase.subTitle_list.joinToString(" | ")


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // Kiểm tra và bật Bluetooth nếu chưa được bật
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        bluetoothListAdapter = bluetoothListAdapter(scanDevices)
        binding.categoriesRv.adapter = bluetoothListAdapter

        binding.startDiscoverBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSION_CODE
                )
            } else {
                startBluetoothScan()
            }
        }


        binding.addSheet.setOnClickListener {
            if (dataBase.isGuest && dataBase.nhom_hp.isEmpty() && dataBase.hpLink_spreadsheet.isEmpty()) startActivity(
                Intent(this, HpAddActivity::class.java)
            )
            else {
                val url =
                    "https://script.google.com/macros/s/AKfycbwBim4OeDzPH69dpbXE6BxkalM0dCAZIOSayKXDgzhB6qFwVN0xil-gEQgvcrbytQU/exec"
                val stringRequest = object : StringRequest(
                    Request.Method.POST, url,
                    Response.Listener {
                        Toast.makeText(
                            this@bluetooth_SheetActivity,
                            it.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("lỗi sheet1", "${it.toString()}")
                    },
                    Response.ErrorListener {
                        Toast.makeText(
                            this@bluetooth_SheetActivity,
                            it.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("lỗi sheet", "${it.toString()}")
                    }
                ) {
                    override fun getParams(): MutableMap<String, String> {
                        val params = HashMap<String, String>()
                        if (ContextCompat.checkSelfPermission(
                                this@bluetooth_SheetActivity,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            val json = JSONArray(scanDevices.map { it.device?.name.toString() })
                            Log.e("name ", " $json")
                            params["hpLink_spreadsheet"] = dataBase.hpLink_spreadsheet
                            params["nhom_hp"] = dataBase.nhom_hp

                            params["ngay_hoc"] =
                                if (dataBase.isGuest) getCurrentDateTime() else dataBase.ngay_hoc + " " + dataBase.h

                            params["id"] = json.toString()
                            // Tiếp tục xử lý với json
                            Log.d("hplink", "${dataBase.hpLink_spreadsheet}")
                        } else {
                            // Yêu cầu cấp quyền truy cập vị trí
                            ActivityCompat.requestPermissions(
                                this@bluetooth_SheetActivity,
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                REQUEST_PERMISSION_CODE
                            )
                        }

                        return params
                    }
                }

                val queue = Volley.newRequestQueue(this@bluetooth_SheetActivity)

                queue.add(stringRequest)

//            if (dataBase.isGuest)startActivity(Intent(this, MainActivity::class.java))
//            else {
//                dataBase.id_arr_ref = 3
//                startActivity(Intent(this, DashboardAdminActivity::class.java))
//            }
            }
        }

    }

    private fun startBluetoothScan() {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)

        try {
            if (bluetoothAdapter.isDiscovering) {
                bluetoothAdapter.cancelDiscovery()
            }

            bluetoothAdapter.startDiscovery()
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val btDevice = BtDevice(device, null, 0)
                if (!scanDevices.contains(btDevice) && btDevice.device != null && isSupportedDevice(
                        btDevice.device
                    )
                ) {
                    scanDevices.add(btDevice)
                    bluetoothListAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        scanHandler?.removeCallbacksAndMessages(null)
    }

    private fun isSupportedDevice(device: BluetoothDevice?): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        return device?.name != null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                // Bluetooth đã được bật
            } else {
                // Yêu cầu bật Bluetooth đã bị từ chối
                // Xử lý tùy theo trường hợp
            }
        }
    }

//    override fun onPause() {
//        super.onPause()
//        // Tắt Bluetooth nếu đang bật
//        if (bluetoothAdapter.isEnabled) {
//            // Kiểm tra quyền BLUETOOTH_ADMIN trước khi tắt Bluetooth
//            if (ContextCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.BLUETOOTH_ADMIN
//                ) == PackageManager.PERMISSION_GRANTED
//            ) {
//                bluetoothAdapter.disable()
//            } else {
//                // Yêu cầu cấp quyền BLUETOOTH_ADMIN
//                ActivityCompat.requestPermissions(
//                    this,
//                    arrayOf(Manifest.permission.BLUETOOTH_ADMIN),
//                    REQUEST_PERMISSION_CODE
//                )
//            }
//        }
//        // Tắt quyền truy cập vị trí
//        if (ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            // Tắt quyền truy cập vị trí nếu được cấp
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                REQUEST_PERMISSION_CODE
//            )
//        }
//    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền đã được cấp, tiến hành quét Bluetooth

                startBluetoothScan()
            } else {
                // Quyền bị từ chối, xử lý tùy theo trường hợp
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startBluetoothScan()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSION_CODE
            )
        }
    }



    // Override onActivityResult to handle the result from QrScanActivity
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == QR_SCAN_REQUEST_CODE && resultCode == RESULT_OK) {
//            val qrCode = data?.getStringExtra("qrCode")
//            binding.titleTv.setText(qrCode)
//        }
//    }

    fun getCurrentDateTime(): String {
        val currentDate = Calendar.getInstance().time

        val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        return dateTimeFormat.format(currentDate)
    }
    override fun onBackPressed() {
        if (dataBase.isGuest) {
            dataBase.nhom_hp = ""
            dataBase.hpLink_spreadsheet = ""
            dataBase.isGuest = false
            startActivity(Intent(this, MainActivity::class.java))
        }else startActivity(Intent(this, DashboardActivity::class.java))
    }
}