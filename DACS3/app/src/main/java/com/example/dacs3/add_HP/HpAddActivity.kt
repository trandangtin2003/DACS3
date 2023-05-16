package com.example.dacs3.add_HP

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dacs3.QrScan.QrScanActivity
import com.example.dacs3.bluetooth_Sheet.bluetooth_SheetActivity
import com.example.dacs3.dataBase
import com.example.dacs3.databinding.ActivityHpAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HpAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHpAddBinding

    /*firebase auth*/
    private lateinit var firebaseAuth: FirebaseAuth

    /*progress dialog*/
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHpAddBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.e("idupdate ", "${dataBase.id_update}")
        update()


        /*init firebase auth*/
        firebaseAuth = FirebaseAuth.getInstance()

        //configure progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        /*handle click , go back*/
        binding.backBtn.setOnClickListener{
            dataBase.id_update = ""
            onBackPressed()
        }


        binding.qrScanBtn.setOnClickListener {
            // Create an intent to start the QR scanning activity
            val intent = Intent(this, QrScanActivity::class.java)
            startActivityForResult(intent, QR_SCAN_REQUEST_CODE)
        }

        /*handle click , begin upload hp*/
        binding.submitBtn.setOnClickListener {
            validateDate()
        }
    }

    private var hp = ""
    private var hpLink = ""

    private fun update() {
        if (dataBase.isGuest) {
            binding.contentTv.text = "Địa chỉ cần lưu"
            binding.hpEt.hint = "tên Sheet"
            binding.submitBtn.text = "Save"
            if (dataBase.hpLink_spreadsheet.isNotEmpty() && dataBase.nhom_hp.isNotEmpty()){
                binding.hpEt.text = Editable.Factory.getInstance().newEditable(dataBase.nhom_hp)
                binding.hpLinkEt.text = Editable.Factory.getInstance().newEditable(dataBase.hpLink_spreadsheet)
            }
        }
        else if (dataBase.id_update != "") {
            // Lấy reference đến node Hoc_phan trong Realtime Database
            val database = FirebaseDatabase.getInstance().reference.child("Hoc_phan")

            // Truy vấn theo child có key bằng với database.id_update
            val query = database.child(dataBase.id_update)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Kiểm tra xem có dữ liệu phù hợp hay không
                    if (dataSnapshot.exists()) {
                        val hocPhan = dataSnapshot.child("hp").getValue(String::class.java)
                        val hpLinkSpreadsheet = dataSnapshot.child("hpLink_spreadsheet").getValue(String::class.java)
                        if (hocPhan != null && hpLinkSpreadsheet != null){
                            binding.submitBtn.text = "Update"
                            binding.contentTv.text = "Update : " + hocPhan
                            // Điền dữ liệu vào các trường
                            binding.hpEt.text = Editable.Factory().newEditable(hocPhan)
                            binding.hpLinkEt.text = Editable.Factory().newEditable(hpLinkSpreadsheet)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Xử lý khi có lỗi xảy ra trong quá trình truy vấn
                }
            })
        }
    }

    private fun validateDate() {
        //get data
        hp = binding.hpEt.text.toString().trim()
        hpLink = binding.hpLinkEt.text.toString().trim()

        //validate data
        if (hp.isEmpty()){
            Toast.makeText(this,"Enter hp ... ", Toast.LENGTH_SHORT).show()
        }
        else if (hpLink.isEmpty()){
            Toast.makeText(this,"Enter link học phần ... ", Toast.LENGTH_SHORT).show()
        }
        else if (!URLUtil.isValidUrl(hpLink)){
            Toast.makeText(this,"link học phần không hợp lệ ... ", Toast.LENGTH_SHORT).show()
        }
        else{
            addhpFirebase()
        }
    }

    private fun addhpFirebase() {
        //show progress
        progressDialog.show()

        //get timestamp
        val timestamp = System.currentTimeMillis()

        //setup data to add or update in firebase db
        val hashMap = HashMap<String, Any?>() // second param is any ; because the value could be of any type
        hashMap["hp"] = hp
        hashMap["hpLink_spreadsheet"] = hpLink

        //add or update data in firebase db: Database Root > hp > hpId > hp info
        val ref = FirebaseDatabase.getInstance().getReference("Hoc_phan")
        if (dataBase.isGuest) {
            dataBase.hpLink_spreadsheet = hpLink
            dataBase.nhom_hp = hp
            startActivity(Intent(this, bluetooth_SheetActivity::class.java))
        }
        else if (dataBase.id_update.isNotEmpty()) {
            ref.child(dataBase.id_update)
                .updateChildren(hashMap)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Updated successfully... ", Toast.LENGTH_SHORT).show()
//                    binding.hpEt.text = null
//                    binding.hpLinkEt.text = null
                    update()
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Failed to update due to ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            hashMap["id_hp"] = "$timestamp"
            hashMap["timestamp"] = timestamp
            hashMap["uid"] = firebaseAuth.uid

            ref.child("$timestamp")
                .setValue(hashMap)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Added successfully... ", Toast.LENGTH_SHORT).show()
                    binding.hpEt.text = null
                    binding.hpLinkEt.text = null
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Failed to add due to ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    companion object {
        private const val QR_SCAN_REQUEST_CODE = 1
    }

    // Override onActivityResult to handle the result from QrScanActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == QR_SCAN_REQUEST_CODE && resultCode == RESULT_OK) {
            val qrCode = data?.getStringExtra("qrCode")
            binding.hpLinkEt.setText(qrCode)
        }
    }
    override fun onBackPressed() {
        val intent = Intent()
        setResult(RESULT_CANCELED, intent)
        super.onBackPressed()
    }
}