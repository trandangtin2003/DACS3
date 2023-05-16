package com.example.dacs3

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.dacs3.admin.DashboardActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SplashActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //khởi tạo firebase cung cấp các tính năng đăng nhập và xác thực người dùng cho ứng dụng.
        firebaseAuth = FirebaseAuth.getInstance()

        /*Khi thời gian chờ đã qua, phương thức checkUser() sẽ được gọi để kiểm tra người dùng đã đăng nhập hay chưa, và thực hiện các hành động tương ứng trong ứng dụng của bạn.*/
        Handler().postDelayed(Runnable {

            checkUser()
//            startActivity(Intent(this@SplashActivity, DashboardAdminActivity::class.java))
//            finish()
        },1000)
    }
    //kiểm tra người dùng đã đăng nhập hay chưa và chuyển hướng người dùng đến màn hình chính của ứng dụng tương ứng.
    private fun checkUser() {
        /*lấy thông tin người dùng hiện tại bằng cách sử dụng FirebaseAuth.getInstance().currentUser. */
        val firebaseUser = firebaseAuth.currentUser

        /*Nếu người dùng chưa đăng nhập, chúng ta chuyển hướng người dùng đến MainActivity,
         nơi người dùng có thể đăng nhập hoặc đăng ký tài khoản.*/
        if(firebaseUser == null){
            //người dùng chưa đăng nhập, vào màn hình chính
            startActivity(Intent(this,MainActivity::class.java))
            finish()

        }
// //Người dùng đã đăng nhập, kiểm tra loại người dùng, giống như đã thực hiện trong màn hình đăng nhập
        else{

            /*Nếu người dùng đã đăng nhập, chúng ta lấy tham chiếu đến nút "Users" trong Firebase Realtime Database và sử dụng addListenerForSingleValueEvent để lắng nghe sự kiện một lần, khi dữ liệu của nút đã được tải xuống từ máy chủ Firebase.*/
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            /*firebaseUser.uid là một chuỗi đại diện cho ID của người dùng hiện tại được lấy từ FirebaseAuth(người dùng đã đăng nhập). Chúng ta sử dụng nó để truy cập vào nút con của "Users" tương ứng với người dùng đó.*/
            ref.child(firebaseUser.uid)

                    /*addListenerForSingleValueEvent là một phương thức trong lớp DatabaseReference được sử dụng để lắng nghe sự kiện một lần duy nhất khi dữ liệu của nút đã được tải xuống từ Firebase Realtime Database.*/
                    /*object : ValueEventListener khai báo một lớp vô danh (anonymous class) thực hiện giao diện ValueEventListener. Lớp này sẽ được sử dụng để lắng nghe sự kiện giá trị được trả về từ nút "Users" và xử lý các giá trị đó.*/
                .addListenerForSingleValueEvent(object  : ValueEventListener {

                    /*lấy thông tin về loại người dùng từ DataSnapshot.*/
                    override fun onDataChange(snapshot: DataSnapshot) {

                        //lấy loại người dùng, ví dụ: người dùng hoặc đăng nhập
                        /*phương thức child() của snapshot để truy cập vào nút userType trong dữ liệu của người dùng.*/
                        val userType = snapshot.child("userType").value
                        Log.d("da toi","user type : $userType")
                        if(userType == "user"){
                            //its simple user , open user dashboard

                            startActivity(Intent(this@SplashActivity, DashboardActivity::class.java))
                            /*finish() để đóng SplashActivity sau khi chuyển hướng người dùng đến màn hình tiếp theo.*/
                            finish()
                        }
                        else if(userType == "admin"){

                            //its admin , open admin dashboard
                            startActivity(Intent(this@SplashActivity, DashboardActivity::class.java))
                            finish()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
    }
}
