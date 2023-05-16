package com.example.dacs3.admin

/*Lớp Model_ad là một lớp mô hình để lưu trữ dữ liệu của các danh mục trong cơ sở dữ liệu Firebase. */
class Model_ad(
    val hp: String = "",
    val nhom_hp: String = "",
    val id_nhomHp: String = "",
    val id_hp: String = "",
    val hpLink_spreadsheet: String = "",
    val email: String = "",
    val name: String = "",
    val ngay_hoc: String = "",
    val h: String = "",
    val id_user: String = "",
    val timestamp: Long = 0,
    val uid: String = ""
) {
    constructor(
        id: String? = null,
        hp: String = "", hpLink_spreadsheet: String = "",
        email: String = "", name: String = "",
        nhom_hp: String = "",
        ngay_hoc: String = "", h: String = "",
        id_nhomHp: String = "",
        timestamp: Long = 0, uid: String = ""
    ) : this(
        hp = hp,
        nhom_hp = nhom_hp,
        id_nhomHp = id_nhomHp,
        id_hp = id ?: "",
        hpLink_spreadsheet = hpLink_spreadsheet,
        email = email,
        name = name,
        ngay_hoc = ngay_hoc,
        h = h,
        id_user = id ?: "",
        timestamp = timestamp,
        uid = uid
    )
}