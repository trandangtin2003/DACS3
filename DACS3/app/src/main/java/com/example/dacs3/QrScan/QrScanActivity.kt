package com.example.dacs3.QrScan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.isNotEmpty
import com.example.dacs3.R
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import java.io.IOException

class QrScanActivity : AppCompatActivity() {

    private lateinit var cameraSource: CameraSource
    private lateinit var detector: BarcodeDetector
    private lateinit var surfaceView: SurfaceView

    private val REQUEST_CAMERA_PERMISSION = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scan)

        surfaceView = findViewById(R.id.cameraView)

        // Create a barcode detector
        detector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.QR_CODE)
            .build()

        // Create a camera source
        cameraSource = CameraSource.Builder(this, detector)
            .setAutoFocusEnabled(true)
            .build()

        // Set up surface view
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                if (ContextCompat.checkSelfPermission(
                        this@QrScanActivity,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // Camera permission has been granted, start the camera
                    try {
                        cameraSource.start(holder)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } else {
                    // Camera permission has not been granted, request the permission
                    ActivityCompat.requestPermissions(
                        this@QrScanActivity,
                        arrayOf(Manifest.permission.CAMERA),
                        REQUEST_CAMERA_PERMISSION
                    )
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                // No need to handle anything in this case
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                // Stop the camera source
                cameraSource.stop()
            }
        })

        // Set the processor for the barcode detector
        detector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {}

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.isNotEmpty()) {
                    val intent = Intent()
                    intent.putExtra("qrCode", barcodes.valueAt(0).displayValue)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission has been granted, start the camera
                try {
                    if (surfaceView.holder.surface.isValid) {
                        startCamera(surfaceView.holder)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                // Camera permission has been denied, show an error message or request the permission again
                // ...
            }
        }
    }

    private fun startCamera(holder: SurfaceHolder) {
        try {
            cameraSource.start(holder)
        } catch (e: SecurityException) {
            // Camera permission denied
            // Handle the error gracefully or show an error message
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}