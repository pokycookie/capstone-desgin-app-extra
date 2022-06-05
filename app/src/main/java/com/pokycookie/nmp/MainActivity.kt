package com.pokycookie.nmp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.pokycookie.nmp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Viewpager
        val fragmentList = listOf<Fragment>(MainFragment(), BluetoothFragment())
        val fragmentTabList = listOf<String>("MAIN", "BLUETOOTH")

        val adapter = ViewpagerAdapter(this)
        adapter.fragmentList = fragmentList
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = fragmentTabList[position]
        }.attach()
    }
}