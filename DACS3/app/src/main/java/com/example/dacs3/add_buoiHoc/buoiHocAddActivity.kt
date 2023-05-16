package com.example.dacs3.add_buoiHoc

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dacs3.R
import com.example.dacs3.dataBase
import com.example.dacs3.databinding.ActivityBuoiHocAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class buoiHocAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBuoiHocAddBinding

    /*firebase auth*/
    private lateinit var firebaseAuth: FirebaseAuth

    /*progress dialog*/
    private lateinit var progressDialog: ProgressDialog





    private val calendar = Calendar.getInstance()


    private lateinit var startEditText: EditText
    private lateinit var endEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuoiHocAddBinding.inflate(layoutInflater)
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

        startEditText = findViewById(R.id.start_timeEt)
        endEditText = findViewById(R.id.end_timeEt)
    }

    fun showTimePickerDialog(view: View) {
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            saveTime()
        }

        TimePickerDialog(
            this@buoiHocAddActivity,
            timeSetListener,
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    fun showDayOfWeekPickerDialog(view: View) {
        val daysOfWeek = arrayOf("Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Chọn thứ")
        builder.setItems(daysOfWeek) { _, which ->
            val selectedDay = daysOfWeek[which]
            binding.thuHocEt.setText(selectedDay)
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun saveTime() {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.US)
        binding.timeHocEt.setText(dateFormat.format(calendar.time))
    }
    fun showDatePickerDialog(view: View) {
        val editText = view as EditText
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            saveDate(editText)
        }

        DatePickerDialog(
            this@buoiHocAddActivity,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun saveDate(editText: EditText) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        editText.setText(dateFormat.format(calendar.time))
    }







    private var timeHocEt = ""
    private var thuHocEt = ""
    private var start_timeEt = ""
    private var end_timeEt = ""

    private val id_nhomHP = dataBase.id



    private fun update() {
        if (dataBase.id_update != "") {
            // Lấy reference đến node Hoc_phan trong Realtime Database
            val database = FirebaseDatabase.getInstance().reference.child("buoi_Hoc")

            // Truy vấn theo child có key bằng với database.id_update
            val query = database.child(dataBase.id_update)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Kiểm tra xem có dữ liệu phù hợp hay không
                    if (dataSnapshot.exists()) {
                        binding.thuHocEt.visibility = View.GONE
                        binding.endTimeEt.visibility = View.GONE

                        val h = dataSnapshot.child("h").getValue(String::class.java)
                        val ngay_hoc = dataSnapshot.child("ngay_hoc").getValue(String::class.java)
                        if (h != null && ngay_hoc!= null) {
                            binding.submitBtn.text = "Update"
                            binding.contentTv.text = "Update : " + ngay_hoc
                            binding.timeHocEt.text = Editable.Factory().newEditable(h)
                            binding.startTimeEt.text = Editable.Factory().newEditable(ngay_hoc)
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
        timeHocEt = binding.timeHocEt.text.toString().trim()
        thuHocEt = binding.thuHocEt.text.toString().trim()
        start_timeEt = binding.startTimeEt.text.toString()
        end_timeEt = binding.endTimeEt.text.toString()


        //validate data
        if (timeHocEt.isEmpty()){
            Toast.makeText(this,"nhập thời gian học ... ", Toast.LENGTH_SHORT).show()
        }

        else if (start_timeEt.isEmpty()){
            Toast.makeText(this,"thời gian bắt đầu học kỳ ?... ", Toast.LENGTH_SHORT).show()
        }
//        else if (dataBase.id_update == "") {
//            if (thuHocEt.isEmpty()) {
//                Toast.makeText(this, "thứ mấy học ?  ... ", Toast.LENGTH_SHORT).show()
//            } else if (end_timeEt.isEmpty()) {
//                Toast.makeText(this, "thời gian kết thúc khóa học ... ", Toast.LENGTH_SHORT).show()
//            }
//        }

        else{
            addFirebase()
        }
    }

    private fun addFirebase() {
        progressDialog.show()

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val start = dateFormat.parse(start_timeEt)


//get timestamp


//setup data to add in firebase db
        val ref = FirebaseDatabase.getInstance().getReference("buoi_Hoc")
        if (dataBase.id_update.isNotEmpty()) {
                val hashMap = hashMapOf<String, Any>()
                hashMap["h"] = timeHocEt
                hashMap["ngay_hoc"] = start_timeEt
            ref.child(dataBase.id_update)
                .updateChildren(hashMap)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                        Toast.makeText(
                            this,
                            "Update thành công ...",
                            Toast.LENGTH_SHORT
                        ).show()
                    update()
                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Toast.makeText(
                            this,
                            "Failed to update due to ${e.message}",
                            Toast.LENGTH_SHORT
                        )
                            .show()
            }
        } else {
            val end = dateFormat.parse(end_timeEt)

            val calendar = Calendar.getInstance()
            calendar.time = start

            val daysOfWeek = arrayOf("Chủ nhật", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7")

            val dates = mutableListOf<Date>()

            while (calendar.time.before(end)) {
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                if (daysOfWeek[dayOfWeek - 1] == thuHocEt) {
                    dates.add(calendar.time)
                }
                calendar.add(Calendar.DATE, 1)
            }
            for (date in dates) {
                val timestamp = System.currentTimeMillis()
                val hashMap = hashMapOf<String, Any>()
                hashMap["id_nhomHP"] = dataBase.id.last()
                hashMap["h"] = timeHocEt
                hashMap["ngay_hoc"] = dateFormat.format(date)
                hashMap["start_time"] = start_timeEt
                hashMap["end_time"] = end_timeEt
                hashMap["sv_vang"] = ""
                hashMap["timestamp"] = timestamp
                hashMap["uid"] = "${firebaseAuth.uid}"
                ref.child("$timestamp").setValue(hashMap)
                    .addOnSuccessListener {

                        progressDialog.dismiss()
                        Toast.makeText(
                            this,
                            "đã thêm thành công ...${dateFormat.format(date)} ",
                            Toast.LENGTH_SHORT
                        ).show()
                        /* startActivity(
                        Intent(
                            this@buoiHocAddActivity,
                            DashboardAdminActivity::class.java
                        )
                    )
                    dataBase.id_arr_ref = 3*/
                        // finish()
                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Toast.makeText(
                            this,
                            "Failed to add due to ${e.message}",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
            }
        }
    }
}