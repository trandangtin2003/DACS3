package com.example.dacs3

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.dacs3.bluetooth_Sheet.bluetooth_SheetActivity
import com.example.dacs3.databinding.ActivityMainBinding
import com.example.dacs3.login.LoginActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //click login
        binding.loginBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        //skip
        binding.skipBtn.setOnClickListener {
            dataBase.isGuest = true
            startActivity(Intent(this, bluetooth_SheetActivity::class.java))
        }


    }
}