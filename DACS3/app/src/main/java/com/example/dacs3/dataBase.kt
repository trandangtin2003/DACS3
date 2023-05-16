package com.example.dacs3

import com.example.dacs3.admin.DashboardActivity
import com.example.dacs3.add_HP.HpAddActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object dataBase {

    var isGuest: Boolean = false

    var userType:String =""
    //lưu holder vừa click vào

    var uid_ad:String =""
    var email:String =""
    var password:String =""

    var id = mutableListOf<String>()
    val subTitle_list = mutableListOf<String>()

    var users_start : Boolean = false
    var users : Boolean = false
    var users_to_Hp:Boolean = false
    val nhomHp_list: MutableList<String> = mutableListOf()
    val nhomHp_list_khongtrung: MutableList<String> = mutableListOf()


    val ref_data = arrayOf(
        FirebaseDatabase.getInstance().getReference("Users"),
        FirebaseDatabase.getInstance().getReference("Hoc_phan"),
        FirebaseDatabase.getInstance().getReference("nhom_HP"),
        FirebaseDatabase.getInstance().getReference("buoi_Hoc"),
        FirebaseDatabase.getInstance().getReference("User_nhomHP")
    )
    var id_arr_ref:Int = 1
    set(value) {
        field = value
        ref = ref_data[value]
        dashboardAdminActivity?.doi_icon_transfer()
    }

    var ref: DatabaseReference? = null
        set(value) {
            field = value
            dashboardAdminActivity?.load()
        }
    var dashboardAdminActivity: DashboardActivity? = null
    var classAct: Class<*> = HpAddActivity::class.java




    //sheet
    var hpLink_spreadsheet:String =""
    var nhom_hp:String =""
    var ngay_hoc:String =""
    var h:String = ""


    var id_update = ""

}