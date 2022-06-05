package com.pokycookie.nmp

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.pokycookie.nmp.databinding.FragmentBluetoothBinding

class BluetoothFragment : Fragment() {
    private val binding by lazy { FragmentBluetoothBinding.inflate(layoutInflater) }

    private lateinit var mainActivity: MainActivity

    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var pairedList = mutableListOf<BluetoothData>()
    var bluetoothList = mutableListOf<BluetoothData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("debug", "Start")

        // Check bluetooth
        if (bluetoothAdapter == null) {
            binding.bluetoothSwt.text = "This device isn't support bluetooth"
            bluetoothBindingSet(false)
        } else {
            bluetoothBindingSet(bluetoothAdapter.isEnabled)
            getPairedDevice()
        }

        // RecyclerView
        val bluetoothRecyclerAdapter = RecyclerAdapter(this)
        bluetoothRecyclerAdapter.deviceData = bluetoothList
        binding.bluetoothListView.adapter = bluetoothRecyclerAdapter
        binding.bluetoothListView.layoutManager = LinearLayoutManager(mainActivity)

        val pairedRecyclerAdapter = RecyclerAdapter(this)
        pairedRecyclerAdapter.deviceData = pairedList
        binding.pairedListView.adapter = pairedRecyclerAdapter
        binding.pairedListView.layoutManager = LinearLayoutManager(mainActivity)

        // Bluetooth receiver
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        }
        mainActivity.registerReceiver(receiver, filter)

        // binding
        binding.bluetoothSearchingBar.isVisible = false
        binding.bluetoothSwt.setOnClickListener {
            if(bluetoothAdapter !== null) {
                binding.bluetoothSwt.isChecked = bluetoothAdapter.isEnabled
                if (bluetoothAdapter.isEnabled) {
                    bluetoothAdapter.disable()
                } else {
                    val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(intent, 5)
                }
            } else {
                binding.bluetoothSwt.isChecked = false
            }
        }
        binding.scanBtn.setOnClickListener {
            bluetoothAdapter?.startDiscovery()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    private val receiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action) {
                // Bluetooth connected
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    binding.bluetoothSwt.isChecked = true
                }
                // Bluetooth disconnected
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    binding.bluetoothSwt.isChecked = false
                }
                // Bluetooth ON or OFF
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    bluetoothChecker(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1))
                }
                // Find bluetooth device
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val result = BluetoothData(device.name,device.address)
                    bluetoothList.add(result)
                }
                // Bluetooth searching started
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.d("debug", "searching...")
                    bluetoothList.clear()
                    binding.bluetoothSearchingBar.isVisible = true
                    binding.noDeviceText.isVisible = false
                }
                // Bluetooth searching finished
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d("debug", "search finished: $bluetoothList")
                    binding.bluetoothSearchingBar.isVisible = false
                    if (bluetoothList.size == 0) {
                        binding.noDeviceText.isVisible = true
                    }
                }
            }
        }
    }

    private fun bluetoothChecker(state: Int) {
        when(state) {
            10 -> {
                // STATE_OFF
                bluetoothBindingSet(false)
            }
            11 -> {
                // STATE_TURNING_ON
            }
            12 -> {
                // STATE_ON
                bluetoothBindingSet(true)
                getPairedDevice()
            }
            13 -> {
                // STATE_TURNING_OFF
            }
        }
    }

    private fun bluetoothBindingSet(isBluetoothOn: Boolean) {
        binding.bluetoothSwt.isChecked = isBluetoothOn
        binding.pairedListView.isVisible = isBluetoothOn
        binding.bluetoothListView.isVisible = isBluetoothOn
        binding.scanBtn.isVisible = isBluetoothOn
        binding.noDeviceText.isVisible = isBluetoothOn
    }

    private fun getPairedDevice() {
        pairedList.clear()
        val pairedDevice: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevice?.forEach { device ->
            val result = BluetoothData(device.name, device.address)
            pairedList.add(result)
        }
    }
}