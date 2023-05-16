package com.example.dacs3.admin

//import com.example.dacs3.admin.add_buoiHoc.buoiHocAddActivity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dacs3.MainActivity
import com.example.dacs3.R
import com.example.dacs3.add_HP.HpAddActivity
import com.example.dacs3.add_acc.RegisterActivity
import com.example.dacs3.add_buoiHoc.buoiHocAddActivity
import com.example.dacs3.add_nhomHP.nhomHPAddActivity
import com.example.dacs3.dataBase
import com.example.dacs3.dataBase.classAct
import com.example.dacs3.dataBase.id
import com.example.dacs3.dataBase.id_arr_ref
import com.example.dacs3.dataBase.nhomHp_list
import com.example.dacs3.dataBase.ref
import com.example.dacs3.dataBase.subTitle_list
import com.example.dacs3.dataBase.users
import com.example.dacs3.dataBase.users_start
import com.example.dacs3.dataBase.users_to_Hp
import com.example.dacs3.databinding.ActivityDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    /*progress dialog*/
    private lateinit var progressDialog: ProgressDialog

    //arayList to hold categories
    private lateinit var hpArrayList: ArrayList<Model_ad>

    // adapter
    private lateinit var Adapter_ad: Adapter_ad

    //xác định data
    private lateinit var ref_ad : Query

    var clickCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*init firebase Auth*/
        firebaseAuth = FirebaseAuth.getInstance()
        //configure progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)


        dataBase.dashboardAdminActivity = this

        ref = FirebaseDatabase.getInstance().getReference("Hoc_phan")
        //load()

        checkUser()
        /*Search*/
        binding.searchEt.addTextChangedListener(object : TextWatcher {


            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //called as and when user type anything
                try {
                    Adapter_ad.filter.filter(s)
                    Log.d("Search", "Filtering with query: $s")
                } catch (e: Exception) {

                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
        binding.subTitleTv.text = "quản lý Học phần"

        /*handle click , layout*/
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }


/*chuyển đổi giữa học phần và user*/

        binding.transfer.setOnClickListener {
            transfer_click()

        }

        //handle click , start add hp page
        binding.addBtn.setOnClickListener {

            if (id_arr_ref==4){
                users = true
                id_arr_ref =1
            }else startActivity(Intent(this, classAct))
        }

        binding.addNhomHpUser.setOnClickListener {
            //show progress
            progressDialog.show()

            for (nhomHp in dataBase.nhomHp_list) {
                //get timestamp
                val timestamp = System.currentTimeMillis()

                //setup data to add in firebase db
                val hashMap =
                    HashMap<String, Any?>() //secon param is any ; because the value could be of any type

                hashMap["id_user"] = id[1]
                hashMap["id_nhomHp"] = nhomHp
                hashMap["timestamp"] = timestamp
                hashMap["uid"] = "${firebaseAuth.uid}"

                //add to firebase db: Database Root > hp > hpId > hp info
                val ref = FirebaseDatabase.getInstance().getReference("User_nhomHP")
                ref.child("$timestamp")
                    .setValue(hashMap)
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Added succesfuly ... ", Toast.LENGTH_SHORT).show()
                        id_arr_ref --
                        nhomHp_list.clear()
                        users_to_Hp = false
                        binding.addNhomHpUser.visibility = View.GONE
                        binding.addBtn.visibility = View.VISIBLE
                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Toast.makeText(
                            this,
                            "Failed to add due to ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }


    }

    private fun transfer_click() {
        if (users_to_Hp == true) {
            users_to_Hp = false
            nhomHp_list.clear()
            id_arr_ref--
        } else if (users_to_Hp == false && users == true) {
            users = false
            dataBase.nhomHp_list_khongtrung.clear()
            id_arr_ref = 4

        } else if (id_arr_ref == 4 && users_start == true) {
            users_start = false
            id_arr_ref = 0
            id.clear()
            dataBase.nhomHp_list_khongtrung.clear()
            clickCount = 0
        } else if (id_arr_ref == 3 && users_start == true) {
            id_arr_ref = 4

        }

        subTitle_list?.removeLastOrNull()
        id?.removeLastOrNull()


        clickCount++
        if (id_arr_ref > 1 && users_start == false) {
            id_arr_ref--
            clickCount++

        } else if (clickCount % 2 != 0 && users_start == false) {
    //                binding.transfer.setImageResource(R.drawable.ic_supervised_user_circle_green)
            id_arr_ref = 0
    //                ref = FirebaseDatabase.getInstance().getReference("Users")
            //load()
            //Trường hợp 1 :
        } else if (clickCount % 2 == 0 && users_start == false) {

            // binding.transfer.setImageResource(R.drawable.ic_edit_calendar_green)
            id_arr_ref = 1
    //                ref = FirebaseDatabase.getInstance().getReference("Hoc_phan")
            //load()
            //Trường hợp 2 :
        }

        Log.i(
            "id_ref trans",
            "$id_arr_ref, ref: $ref , cloick count : $clickCount , id : ${id.lastOrNull()}"
        )
    }

    fun load() {
        //init arrayList
        hpArrayList = ArrayList()
        if (id_arr_ref == 0){
            ref_ad = dataBase.ref?.orderByChild("uid_ad")?.equalTo("${firebaseAuth.uid}")!!
//            .orderByChild("userType")?.equalTo("user")!!

        }
        else if (id_arr_ref == 1){
            ref_ad = ref?.orderByChild("uid")?.equalTo("${firebaseAuth.uid}")!!
        }
        else if (id_arr_ref == 2){
            ref_ad = dataBase.ref?.orderByChild("id_hp")?.equalTo(id.last())!!
        }
        else if (id_arr_ref == 3){
            ref_ad = dataBase.ref?.orderByChild("id_nhomHP")?.equalTo(id.last())!!
            Log.d("đã tới đây :", "ok")
        }
//        else if(id_arr_ref == 4){
//            ref_ad = dataBase.ref?.orderByChild("id_user")?.equalTo(id[1])!!
//        }
        else ref_ad = ref!!
        Log.e("ref_ad : ","$ref_ad")
        //get all categories from firebase database ... Firebase DB > categories
//        val ref = FirebaseDatabase.getInstance().getReference("Hoc_phan")
        if (id_arr_ref != 4){
            ref_ad.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //clear list before starting adding data into it
                    hpArrayList.clear()
                    for (ds in snapshot.children) {
                        //get data as model
                        //getValue() là một phương thức để chuyển đổi dữ liệu trong DataSnapshot thành đối tượng Model_ad trong ứng dụng
                        val model = ds.getValue(Model_ad::class.java)

                        //add to arrayList
                        hpArrayList.add(model!!)
                    }
                    //setup adapter
                    Adapter_ad = Adapter_ad(this@DashboardActivity, hpArrayList)
                    //set adapter to recylerView
                    binding.categoriesRv.adapter = Adapter_ad
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
        else if (id_arr_ref == 4){

            ref_ad = dataBase.ref?.orderByChild("id_user")?.equalTo(id[1])!!
//            addListenerForSingleValueEvent để chỉ lấy dữ liệu một lần duy nhất mà không lắng nghe sự kiện thay đổi dữ liệu
            ref_ad.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val idSet = HashSet<String>()
                    val tempList = ArrayList<Model_ad>()
                    for (ds in snapshot.children) {
                        val timestamp = ds.child("timestamp").value as Long
                        val id_nhomHp = ds.child("id_nhomHp").value.toString()
                        val refNhomHp = FirebaseDatabase.getInstance().getReference("nhom_HP/$id_nhomHp")
                        refNhomHp.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(nhomHpSnapshot: DataSnapshot) {
                                val nhomHp = nhomHpSnapshot.child("nhom_hp").value.toString()
                                val idHp = nhomHpSnapshot.child("id_hp").value.toString()
                                val refHp = FirebaseDatabase.getInstance().getReference("Hoc_phan/$idHp")
                                refHp.addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(hpSnapshot: DataSnapshot) {
                                        val hp = hpSnapshot.child("hp").value.toString()
                                        val hpLink_spreadsheet = hpSnapshot.child("hpLink_spreadsheet").value.toString()
                                        val model = Model_ad(
                                            hp = hp,
                                            hpLink_spreadsheet = hpLink_spreadsheet,
                                            nhom_hp = nhomHp,
                                            id_nhomHp = id_nhomHp,
                                            timestamp = timestamp.toLong()
                                        )
                                        if (!idSet.contains(model.id_nhomHp)) {
                                            tempList.add(model)
                                            idSet.add(model.id_nhomHp)
                                        }
                                        //setup adapter
                                        Adapter_ad = Adapter_ad(this@DashboardActivity, tempList)
                                        //set adapter to recylerView
                                        binding.categoriesRv.adapter = Adapter_ad
                                    }
                                    override fun onCancelled(error: DatabaseError) {}
                                })
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    //đổi ảnh nút chuyển transfer
    fun doi_icon_transfer() {
        binding.subTitleTv.text = subTitle_list.joinToString(" | ")
        when (id_arr_ref) {
            0 -> {
                binding.transfer.setImageResource(R.drawable.ic_supervised_user_circle_green)
                classAct = RegisterActivity::class.java
                binding.subTitleTv.text = "quản lý USER"
            }
            1 ->{
                binding.transfer.setImageResource(R.drawable.ic_edit_calendar_green)
                classAct = HpAddActivity::class.java
                binding.subTitleTv.text = "quản lý Học phần"
            }
            2 ->{
                binding.transfer.setImageResource(R.drawable.ic_keyboard_return_green)
                classAct = nhomHPAddActivity::class.java

            }
            3 ->{
                binding.transfer.setImageResource(R.drawable.ic_keyboard_return_green)
                classAct =  buoiHocAddActivity::class.java

            }
            4 ->{
                binding.transfer.setImageResource(R.drawable.ic_keyboard_return_green)
                binding.subTitleTv.text = subTitle_list.joinToString(" | ")
            }
        }
        if (users == true){
            binding.addBtn.visibility = View.GONE
            binding.transfer.setImageResource(R.drawable.ic_keyboard_return_green)
        }else binding.addBtn.visibility = View.VISIBLE

        if (dataBase.userType == "user" && id_arr_ref == 4){
            subTitle_list.clear()
            binding.subTitleTv.text =""
            binding.transfer.visibility = View.GONE

        }else binding.transfer.visibility = View.VISIBLE
    }


    private fun checkUser() {



        //get currnent user
        val firebaseUser = firebaseAuth.currentUser

        if (firebaseUser == null) {
            //not logged in , goto main screenn
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        else {
            //logged in , get and show user infor
            val email = firebaseUser.email
            //set to textview of toolbar
            id_arr_ref = 1
        }
        /* 4. Check user type - Firebase Auth
        *       If User - Move to user dashboard
        *       If Admin- Move to admin dashboard*/
        progressDialog.setMessage("Checking User...")

        /*lấy đối tượng FirebaseUser hiện tại */
//        val firebaseUser = firebaseAuth.currentUser!!

        /*lấy tham chiếu tới nút "Users" trong cơ sở dữ liệu Firebase*/
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        /*child() của tham chiếu để truy cập vào nút con có tên là firebaseUser.uid (đây là id của người dùng hiện tại).*/
        ref.child(firebaseUser!!.uid)
            /*đăng ký một ValueEventListener để lắng nghe sự kiện thay đổi dữ liệu từ cơ sở dữ liệu Firebase. */
            .addListenerForSingleValueEvent(object  : ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {
                    progressDialog.dismiss()
                    val email = firebaseUser.email

                    //get user type e.g user or login
                    val userType = snapshot.child("userType").value
                    //kiểm tra giá trị và chuyển
                    if(userType == "user"){
                        //its simple user , open user dashboard
//                        startActivity(Intent(this@LoginActivity, DashboardUserActivity::class.java))
//                        finish()

                        binding.titleTv.text = "user : $email"
                        dataBase.userType = "user"
                        users_start = true
                        dataBase.id.add("0")
                        dataBase.id.add("${firebaseUser!!.uid}")
                        id_arr_ref=4
                    }
                    else if(userType == "admin"){
                        //its admin , open admin dashboard
//                        startActivity(Intent(this@LoginActivity, DashboardAdminActivity::class.java))
//                        finish()

                        dataBase.uid_ad = firebaseUser.uid
                            //logged in , get and show user infor
                        binding.titleTv.text = "admin : $email"
                            //set to textview of toolbar
                        id_arr_ref = 1

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    override fun onBackPressed() {
        if (binding.transfer.visibility == View.GONE){

        }else {
            transfer_click()
        }

    }

}

