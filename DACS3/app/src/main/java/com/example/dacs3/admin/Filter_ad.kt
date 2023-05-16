package com.example.dacs3.admin

import android.widget.Filter

class Filter_ad : Filter {
    //arrayList in which we want to search
    private var filterList : ArrayList<Model_ad>

    //adapter in which filter need to be implemented
    private var Adapter_ad: Adapter_ad

    //constructor
    constructor(filterList: ArrayList<Model_ad>, Adapter_ad: Adapter_ad) {
        this.filterList = filterList
        this.Adapter_ad = Adapter_ad
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()

        //values should not be null and not emty
        if (constraint != null && constraint.isEmpty()){
            //search value is nor null not emplty


            //change to upper case , or lower case to avoid case sensitivity
            constraint = constraint.toString().uppercase()
            val filterModels:ArrayList<Model_ad> = ArrayList()
            for (i in 0 until filterList.size){
                //validate
                //contains : có chứ    / contraint : giá trị được nhập vào
                if (filterList[i].hp.uppercase().contains(constraint)){
                    //add to filtered list
                    filterModels.add(filterList[i])

                }
            }
            results.count = filterModels.size
            results.values = filterModels
        }
        else{
            //search value is either null or empty
            results.count = filterList.size
            results.values = filterList
        }
        return results //don't miss it
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        //apply filter changes
        Adapter_ad.ArrayList = results.values as ArrayList<Model_ad>

        //notify changes
        Adapter_ad.notifyDataSetChanged()
    }

}