package com.example.dacs3.add_acc

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dacs3.admin.DashboardActivity
import com.example.dacs3.dataBase
import com.example.dacs3.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RegisterActivity : AppCompatActivity() {

     private lateinit var binding: ActivityRegisterBinding

     /*firebase auth*/
     private lateinit var firebaseAuth: FirebaseAuth

     /*progress dialog*/
     private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        update()

        /*init firebase auth*/
        firebaseAuth = FirebaseAuth.getInstance()

        /*init progres dialog , will show white creating account | Register user*/
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.contentTv.text = binding.contentTv.text.toString() + if (dataBase.uid_ad == "") ": Admin" else ": User"

        /*nút quay lại*/
        binding.backBtn.setOnClickListener{
            dataBase.id_update = ""
            onBackPressed()
        }

        /*handle click , begin register*/
        binding.registerBtn.setOnClickListener{
            /*Step
            * 1. Input Data
            * 2. Validate data
            * 3. Create Account - Firebase Auth
            * 4. Save user infor - Firebase Realtime Database*/
            validateData()
        }

    }
    private var name = ""
    private var email = ""
    private var password = ""

    private fun update() {
        if (dataBase.id_update != "") {
            // Lấy reference đến node Hoc_phan trong Realtime Database
            val database = FirebaseDatabase.getInstance().reference.child("Users")

            // Truy vấn theo child có key bằng với database.id_update
            val query = database.child(dataBase.id_update)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Kiểm tra xem có dữ liệu phù hợp hay không
                    if (dataSnapshot.exists()) {
                        val name = dataSnapshot.child("name").getValue(String::class.java)
                        val email = dataSnapshot.child("email").getValue(String::class.java)
                        if (name != null && email != null){
                            binding.registerBtn.text = "Update"
                            binding.contentTv.text = "Update : " + name
                            // Điền dữ liệu vào các trường
                            binding.nameEt.text = Editable.Factory().newEditable(name)
                            binding.emailEt.text = Editable.Factory().newEditable(email)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Xử lý khi có lỗi xảy ra trong quá trình truy vấn
                }
            })
        }
    }

    private fun validateData() {
        /*1. Input Data*/
        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
//        val cPassword = binding.cPasswordEt.text.toString().trim()

        /*2. Validate Data*/
        if (name.isEmpty()) {
            Toast.makeText(this, "Nhập Tên .... ", Toast.LENGTH_SHORT).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid Email Pattern .... ", Toast.LENGTH_SHORT).show()
        } else if (dataBase.id_update.isEmpty() && password.isEmpty()) {
            Toast.makeText(this, "Nhập Mật Khẩu .... ", Toast.LENGTH_SHORT).show()
        } else {
            if (dataBase.id_update.isEmpty()) {
                createUserAccount()
            } else {
                updateUserInfo()
            }
        }
    }

    private fun createUserAccount() {
        /*3. Create Account - Firebase Auth*/
        /*show progress*/
        progressDialog.setMessage("Creating Account...")
        progressDialog.show()

        /*creating user in firebase auth*/
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                updateUserInfo()
            }
            .addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(this,"Failed creating account due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /*Sau khi tạo tài khoản thành công, ta cần lưu thông tin của user đó vào database để sử dụng cho các chức năng khác trong ứng dụng*/
    private fun updateUserInfo() {
        /*4.Save User Infor - Firebase Realtime Database*/

        progressDialog.setMessage("Saving user infor...")

        /*timestamp*/
        /*timestamp để lưu thời điểm đăng ký.*/
        val timestamp = System.currentTimeMillis()

        /*lấy uid người dùng hiện tại, vì người dùng đã đăng ký nên chúng tôi có thể lấy ngay bây giờ*/
        val uid = firebaseAuth.uid

        /*thiết lập dữ liệu để thêm vào db*/
        /* HashMap với key là tên của thông tin cần lưu và value là giá trị tương ứng.*/
        val hashMap: HashMap<String,Any?> = HashMap()
        hashMap["email"] = email
        hashMap["name"] = name




        //set data to db
        //FirebaseDatabase.getInstance().getReference("Users") để lấy tham chiếu đến node "Users" trên Firebase Database
        val ref = FirebaseDatabase.getInstance().getReference("Users")

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
            hashMap["uid"] = uid
            hashMap["uid_ad"] = if (dataBase.uid_ad == "") "" else "${dataBase.uid_ad}"
            hashMap["profileImage"] = "" //add empty , will do in profile edit
            hashMap["userType"] = if (dataBase.uid_ad == "") "admin" else "user" //possible value are user/admin , will change value to admin manually on firebase db
            hashMap["timestamp"] = timestamp
            hashMap["id_user"] = "$timestamp"

            ref.child("$uid")
                .setValue(hashMap)
                .addOnSuccessListener {
                    /*Đã lưu thông tin người dùng, mở bảng điều khiển người dùng*/
                    progressDialog.dismiss()
                    Toast.makeText(this, "Account created ...", Toast.LENGTH_SHORT).show()
                    binding.nameEt.text = null
                    binding.emailEt.text = null
                    //đăng xuất khoải tài khoản vừa tạo
//                firebaseAuth.signOut()
                    //đăng nhập vào lại tk admin

//                firebaseAuth.signInWithEmailAndPassword("${dataBase.email}","${dataBase.password}")

//                startActivity(Intent(this@RegisterActivity, DashboardUserActivity::class.java))
//                finish()

                    // Kiểm tra nếu userType là user thì đăng nhập lại tk admin
                    if (hashMap["userType"] == "user") {
                        firebaseAuth.signInWithEmailAndPassword(
                            "${dataBase.email}",
                            "${dataBase.password}"
                        )
                        Log.e("acc","${dataBase.email}")
                    }
                    // Ngược lại nếu userType là admin thì chuyển tới màn hình DashboardAdminActivity
                    else {
                        startActivity(
                            Intent(
                                this@RegisterActivity,
                                DashboardActivity::class.java
                            )
                        )
                        finish()
                    }

                }
                .addOnFailureListener { e ->
                    /*failed adding data to db*/
                    progressDialog.dismiss()
                    Toast.makeText(
                        this,
                        "Failed saving user info due to ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

    }
}