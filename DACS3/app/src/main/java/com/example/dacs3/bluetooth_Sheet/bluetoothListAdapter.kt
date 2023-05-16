package com.example.dacs3.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dacs3.bluetooth_Sheet.BtDevice
import com.example.dacs3.databinding.BluetoothItemBinding



class bluetoothListAdapter(
    var btList: List<BtDevice>
) : RecyclerView.Adapter<bluetoothListAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: BluetoothItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int, bt: BtDevice) {
            binding.pos.text = "${position + 1}. "
            try {
                binding.btName.text = "${bt.device?.name}"
            } catch (e: SecurityException) {
                // Handle the SecurityException here
                // Your code here
                binding.btName.text = "${bt.device?.address}"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = BluetoothItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return btList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position, btList[position])
    }
}