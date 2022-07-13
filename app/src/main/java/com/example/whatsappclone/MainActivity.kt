package com.example.whatsappclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.whatsappclone.adapters.ScreenSliderAdapter
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolBar)
        viewPager.adapter = ScreenSliderAdapter(this)
        TabLayoutMediator(tabs,viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "CHATS"
                1 -> tab.text = "PEOPLE"
            }
        }.attach()
    }
}