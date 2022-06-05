package com.pokycookie.nmp

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pokycookie.nmp.databinding.BluetoothRecyclerBinding
import java.io.IOException
import java.util.*

class RecyclerAdapter(private val fragment: BluetoothFragment): RecyclerView.Adapter<RecyclerHolder>() {
    var deviceData = mutableListOf<BluetoothData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerHolder {
        val binding = BluetoothRecyclerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecyclerHolder(binding, fragment)
    }

    override fun onBindViewHolder(holder: RecyclerHolder, position: Int) {
        holder.setData(deviceData[position])
    }

    override fun getItemCount(): Int {
        return deviceData.size
    }
}

class RecyclerHolder(private val binding: BluetoothRecyclerBinding, private val fragment: BluetoothFragment): RecyclerView.ViewHolder(binding.root) {
    private var deviceData: BluetoothData? = null
    private lateinit var bluetoothSocket: BluetoothSocket

    init {
        binding.root.setOnClickListener {
            val device: BluetoothDevice = fragment.bluetoothAdapter?.getRemoteDevice("${deviceData?.mac}")!!
            try {
                bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
                connectBluetooth()
            } catch (e: IOException) {
                Log.e("socket create error", "$e")
                binding.deviceMAC.text = "error1"
            }
        }
    }

    fun setData(data: BluetoothData) {
        binding.deviceName.text = "${data.name}"
        binding.deviceMAC.text = "${data.mac}"
        deviceData = data
    }

    private fun connectBluetooth() {
        fragment.bluetoothAdapter?.cancelDiscovery()
        try {
            bluetoothSocket.connect()
        } catch (e: IOException) {
            Log.e("socket connect error", "$e")
            binding.deviceMAC.text = "error2"
            try {
                bluetoothSocket.close()
            } catch (E: IOException) {
                Log.e("socket close error", "$E")
                binding.deviceMAC.text = "error3"
            }
        }
    }
}