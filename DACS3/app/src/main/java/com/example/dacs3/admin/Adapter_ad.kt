package com.example.dacs3.admin

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.dacs3.R
import com.example.dacs3.bluetooth_Sheet.bluetooth_SheetActivity
import com.example.dacs3.dataBase
import com.example.dacs3.dataBase.id_arr_ref
import com.example.dacs3.dataBase.nhomHp_list
import com.example.dacs3.dataBase.nhomHp_list_khongtrung
import com.example.dacs3.dataBase.users
import com.example.dacs3.dataBase.users_start
import com.example.dacs3.dataBase.users_to_Hp
import com.example.dacs3.databinding.RowItemBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class Adapter_ad : RecyclerView.Adapter<Adapter_ad.Holder> , Filterable{
    private val context: Context
    public var ArrayList: ArrayList<Model_ad>



    /* filterList là danh sách các mục cần lọc*/
    private var filterList: ArrayList<Model_ad>
    /*filter là đối tượng Filter_ad sẽ được sử dụng để thực hiện tìm kiếm và lọc.*/
    private var filter: Filter_ad? = null



    private lateinit var binding: RowItemBinding


    var dashboardAdminActivity: DashboardActivity? = null

    var count_checked = 0


    //constructor
    constructor(context: Context, ArrayList: ArrayList<Model_ad>) {
        this.context = context
        this.ArrayList = ArrayList
        this.filterList = ArrayList
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        //inflate / bind row_hp.xml
        /*inflate file layout row_hp.xml bằng phương thức inflate() trong đối tượng LayoutInflater*/
        binding = RowItemBinding.inflate(LayoutInflater.from(context),parent,false)

        return Holder(binding.root,binding)

    }



    override fun onBindViewHolder(holder: Holder, position: Int) {
        /*get data , set data , handle click etc */
//binding.checkBox.visibility = View.VISIBLE
        //get data

        if (dataBase.userType == "user" && id_arr_ref == 4) holder.optionBtn.visibility =View.GONE

        val model = ArrayList[position]
        val id = model.id_hp?.takeIf { it.isNotBlank() } ?:model.id_user

        val uid = model.uid
        val timestamp = model.timestamp


        val hp = model.hp
        val hpLink_spreadsheet = model.hpLink_spreadsheet
        val name = model.name
        val nhom_hp =   model.nhom_hp
        val ngay_hoc = model.ngay_hoc
        val h = model.h
        val buoi_hoc = ngay_hoc +" | " + h
        var id_nhomHp = model.id_nhomHp
        Log.d("kt","$name , id : $id ,uid : $uid,nhom_hp : $nhom_hp,timetamp : $timestamp,hp : $hp,ngay học : $ngay_hoc , h : $h , id nhomHp : $id_nhomHp, id_nhomHp_user : $id_nhomHp , hp link : $hpLink_spreadsheet ")

        if (id_arr_ref ==4 ) dataBase.nhomHp_list_khongtrung.add(id_nhomHp)
        Log.e("id_nhomHP không trùng :"," ${nhomHp_list_khongtrung.joinToString(",")}")
        //set data

        holder.Tv.text = name?.takeIf { it.isNotBlank() } ?: hp ?.takeIf { it.isNotBlank() } ?:nhom_hp?.takeIf { it.isNotBlank() } ?:buoi_hoc?.takeIf { it.isNotBlank() } ?:h?.takeIf { it.isNotBlank() } ?:id_nhomHp
        if (id_arr_ref == 4 ){
            holder.Tv.text = hp + "(" + nhom_hp +")"
        }
        if (users == true) binding.deleteBtn.visibility = View.GONE
         if (users_to_Hp == true){
            Log.e("đã tới","bật check box")
             if (nhomHp_list_khongtrung.contains(timestamp.toString())) {
                 binding.checkBox.visibility = View.GONE
             }else binding.checkBox.visibility = View.VISIBLE
             binding.rowLl.isFocusable = true
             binding.rowLl.isFocusableInTouchMode = true
        }


        holder.rowLl.setOnClickListener{
            //a

            if (id_arr_ref >= 2){
                dataBase.id.add("${timestamp.toString()}")
            }else dataBase.id.add("${model.id_hp}")
            dataBase.subTitle_list.add("${holder.Tv.text}")

            if (id_arr_ref == 0){
                users_start = true
                dataBase.id.add("$uid")
                id_arr_ref=4
            }
            else if (id_arr_ref==4){
                dataBase.id.add("${model.id_nhomHp}")
                dataBase.hpLink_spreadsheet = hpLink_spreadsheet
                dataBase.nhom_hp = nhom_hp
                id_arr_ref = 3
            }
            else if (users == true){
                users_to_Hp = true
                id_arr_ref++
            }

            else if (dataBase.userType == "user" && id_arr_ref == 3){
                dataBase.ngay_hoc = ngay_hoc
                dataBase.h = h
                context.startActivity(Intent(context, bluetooth_SheetActivity::class.java))
            }



            else id_arr_ref ++

//            dataBase.ref = FirebaseDatabase.getInstance().getReference("nhom_HP")

            Log.i("id_ref","$id_arr_ref, ref: ${dataBase.ref} , name : ${dataBase.subTitle_list.joinToString(" | ")},nhóm hp : $timestamp,id_hp : ${model.id_hp}, id được truyền : ${dataBase.id.last()}, id trong danh sách : ${dataBase.id.joinToString(", ")}")
            Log.d("user truyen :","${dataBase.hpLink_spreadsheet} và ${dataBase.nhom_hp}")
        }


        holder.optionBtn.setOnClickListener{
            val layoutParams = holder.binding.optionBtn.layoutParams as LinearLayout.LayoutParams
            if (holder.binding.menuLayout.visibility == View.VISIBLE) {
                holder.binding.menuLayout.visibility = View.GONE
                layoutParams.width = context.resources.getDimensionPixelSize(R.dimen.option_btn_normal_width)
                layoutParams.height = context.resources.getDimensionPixelSize(R.dimen.option_btn_normal_height)
                holder.optionBtn.setBackgroundResource(R.drawable.shape_button03)
            } else {
                holder.binding.menuLayout.visibility = View.VISIBLE
                layoutParams.width = context.resources.getDimensionPixelSize(R.dimen.option_btn_large_width)
                layoutParams.height = context.resources.getDimensionPixelSize(R.dimen.option_btn_large_height)
                holder.optionBtn.setBackgroundResource(R.drawable.shape_button02)
            }
            holder.binding.optionBtn.layoutParams = layoutParams
        }

        //handle click , delete hp
        holder.deleteBtn.setOnClickListener{
            //confirm before delete
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete")
                .setMessage("Are sure you want to delete this.. ?")
                .setPositiveButton("Confirm"){ a, d ->
                    Toast.makeText(context, "Deleting...",Toast.LENGTH_SHORT).show()
                    delete(model,holder)

                }
                .setNegativeButton("Cancel"){a,d ->
                    a.dismiss()
                }
                .show()
        }

        holder.editBtn.setOnClickListener {
            var id = model.timestamp.toString()?.takeIf { it.isNotBlank() } ?:model.id_hp?.takeIf { it.isNotBlank() } ?:model.uid
            if (id_arr_ref == 0) id = model.uid

            dataBase.id_update = id
            Log.e("dataBase.id_update =" ,"${dataBase.id_update}" )
            val intent = Intent(context, dataBase.classAct)
            context.startActivity(intent)

        }



        holder.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            val model = ArrayList[position]
            val text = model.timestamp.toString()

            if (isChecked) {
                // Checkbox đã được check
                count_checked++
                if (!nhomHp_list.contains(text)) {
                    nhomHp_list.add(text)
                }
            } else {
                // Checkbox đã được uncheck
                count_checked--
                nhomHp_list.remove(text)
            }
            Log.d("count_checked","$count_checked , danh sách : ${nhomHp_list}")
            if (count_checked!=0) holder.addNhomHpUser.visibility = View.VISIBLE
            else holder.addNhomHpUser.visibility = View.GONE
        }

    }

    private fun delete(model: Model_ad, holder: Holder) {
        val listDeletedKeys = mutableListOf<String>()
        //get id of hp delete
        var id = model.timestamp.toString()?.takeIf { it.isNotBlank() } ?:model.id_hp?.takeIf { it.isNotBlank() } ?:model.uid
        if (id_arr_ref == 0) id = model.uid

        nhomHp_list_khongtrung.clear()
        //Firebase DB > Categories > categotyId
        Log.i("ref","${dataBase.ref}")
        Log.i("ref id","$id")
        dataBase.ref?.child(id)
            ?.removeValue()
            ?.addOnSuccessListener {
                Toast.makeText(context, "Deleted...",Toast.LENGTH_SHORT).show()

                //xóa child của nhom_Hp với id của Hoc_phan
                var query = dataBase.ref_data[2].orderByChild("id_hp").equalTo(id)
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (childSnapshot in dataSnapshot.children) {
                            // Lấy child cần xóa
                            val childKey_nhomHP = childSnapshot.key
                            // Xóa child khỏi cơ sở dữ liệu
                            if (childKey_nhomHP != null) {
                                dataBase.ref_data[2].child(childKey_nhomHP).removeValue()
                                    .addOnSuccessListener {
                                        listDeletedKeys.add(childKey_nhomHP)
                                        Log.e("listDeletedKeys : ", "${listDeletedKeys.joinToString(" | ")} ")
                                        xoa_note_link(childKey_nhomHP, listDeletedKeys)
                                    }
                            }
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        // Xử lý khi có lỗi xảy ra
                    }
                })
            }
            ?.addOnFailureListener{e->
                Toast.makeText(context, "Unable to delete due to ${e.message}",Toast.LENGTH_SHORT).show()
            }


        //nếu xóa nhom_hp
        if (id_arr_ref == 2){
            xoa_note_link(id, listDeletedKeys)
        }


    }

    private fun xoa_note_link(
        childKey_nhomHP: String?,
        listDeletedKeys: MutableList<String>
    ) {
        //nếu nhom_HP bị xóa thì buoi_Hoc xóa theo

        var query = dataBase.ref_data[3].orderByChild("id_nhomHP").equalTo(childKey_nhomHP)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (childSnapshot in dataSnapshot.children) {
                    // Lấy child cần xóa
                    val childKey = childSnapshot.key
                    // Xóa child khỏi cơ sở dữ liệu
                    if (childKey != null) {
                        dataBase.ref_data[3].child(childKey).removeValue()
                            .addOnSuccessListener {
                                listDeletedKeys.clear()
                                listDeletedKeys.add(childKey)
                                Log.e(
                                    "listDeletedKeys buoiHoc: ",
                                    "${listDeletedKeys.joinToString(" | ")} "
                                )

                            }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Xử lý khi có lỗi xảy ra
            }
        })


        //xóa user_nhomHp
        query = dataBase.ref_data[4].orderByChild("id_nhomHp").equalTo(childKey_nhomHP)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (childSnapshot in dataSnapshot.children) {
                    // Lấy child cần xóa
                    val childKey = childSnapshot.key
                    // Xóa child khỏi cơ sở dữ liệu
                    if (childKey != null) {
                        dataBase.ref_data[4].child(childKey).removeValue()
                            .addOnSuccessListener {
                                listDeletedKeys.clear()
                                listDeletedKeys.add(childKey)
                                Log.e(
                                    "listDeletedKeys user_nhomHp : ",
                                    "${listDeletedKeys.joinToString(" | ")} "
                                )

                            }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Xử lý khi có lỗi xảy ra
            }
        })
    }

    override fun getItemCount(): Int {
        return ArrayList.size
    }

    //viewHolder class to hold  / init UI views for row_hp.xml
    inner class Holder(itemView: View,  val binding: RowItemBinding): RecyclerView.ViewHolder(itemView){
        //init ui view
        var rowLl:LinearLayout = binding.rowLl
        var Tv:TextView = binding.Tv

        var optionBtn: ImageButton = binding.optionBtn
        var deleteBtn: ImageButton = binding.deleteBtn
        var editBtn: ImageButton = binding.editBtn

        var checkBox:CheckBox = binding.checkBox
        var addNhomHpUser:Button = (context as DashboardActivity).findViewById(R.id.addNhomHpUser)

//        //đúng đắn
//        init {
//            val layoutParams = optionBtn.layoutParams as LinearLayout.LayoutParams
//            optionBtn.setOnClickListener {
//                if (binding.menuLayout.visibility == View.VISIBLE) {
//                    binding.menuLayout.visibility = View.GONE
//                    layoutParams.width = context.resources.getDimensionPixelSize(R.dimen.option_btn_normal_width)
//                    layoutParams.height = context.resources.getDimensionPixelSize(R.dimen.option_btn_normal_height)
//                    optionBtn.setBackgroundResource(R.drawable.shape_button03)
//                } else {
//                    binding.menuLayout.visibility = View.VISIBLE
//                    layoutParams.width = context.resources.getDimensionPixelSize(R.dimen.option_btn_large_width)
//                    layoutParams.height = context.resources.getDimensionPixelSize(R.dimen.option_btn_large_height)
//                    optionBtn.setBackgroundResource(R.drawable.shape_button02)
//                }
//                optionBtn.layoutParams = layoutParams
//            }
//        }

    }

    override fun getFilter(): Filter {
        if (filter == null){
            filter = Filter_ad(filterList, this)
        }
        return filter as Filter_ad
    }



}