package com.example.dacs3.add_nhomHP

import android.app.ProgressDialog
import android.os.Bundle
import android.text.Editable
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dacs3.dataBase
import com.example.dacs3.databinding.ActivityNhomHpaddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class nhomHPAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNhomHpaddBinding

    /*firebase auth*/
    private lateinit var firebaseAuth: FirebaseAuth

    /*progress dialog*/
    private lateinit var progressDialog: ProgressDialog

    //lấy id học phần vừa mới nhấn vô


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNhomHpaddBinding.inflate(layoutInflater)
        setContentView(binding.root)
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



        /*handle click , begin upload hp*/
        binding.submitBtn.setOnClickListener {
            validateDate()
        }
    }
    private var nhomhp = ""
    private var idhp = dataBase.id.last()


    private fun update() {
        if (dataBase.id_update != "") {
            // Lấy reference đến node Hoc_phan trong Realtime Database
            val database = FirebaseDatabase.getInstance().reference.child("nhom_HP")

            // Truy vấn theo child có key bằng với database.id_update
            val query = database.child(dataBase.id_update)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Kiểm tra xem có dữ liệu phù hợp hay không
                    if (dataSnapshot.exists()) {
                        val nhom_hp = dataSnapshot.child("nhom_hp").getValue(String::class.java)
                        if (nhom_hp != null) {
                            binding.submitBtn.text = "Update"
                            binding.contentTv.text = "Update : " + nhom_hp
                            binding.nhomHpEt.text = Editable.Factory().newEditable(nhom_hp)
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
        nhomhp = binding.nhomHpEt.text.toString().trim()


        //validate data
        if (nhomhp.isEmpty()){
            Toast.makeText(this,"Enter hp ... ", Toast.LENGTH_SHORT).show()
        }
        else{
            addFirebase()
        }
    }

    private fun addFirebase() {
        //show progress
        progressDialog.show()

        //get timestamp
        val timestamp = System.currentTimeMillis()

        //setup data to add in firebase db
        val hashMap = HashMap<String,Any?>() //secon param is any ; because the value could be of any type

        hashMap["nhom_hp"] = nhomhp


        //add to firebase db: Database Root > hp > hpId > hp info
        val ref = FirebaseDatabase.getInstance().getReference("nhom_HP")
        if (dataBase.id_update.isNotEmpty()) {
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
            hashMap["id_hp"] = idhp
            hashMap["timestamp"] = timestamp
            hashMap["uid"] = "${firebaseAuth.uid}"

            ref.child("$timestamp")
                .setValue(hashMap)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Added succesfuly ... ", Toast.LENGTH_SHORT).show()
                    binding.nhomHpEt.text = null
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Failed to add due to ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }
}