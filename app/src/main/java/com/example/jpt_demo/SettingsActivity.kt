package com.example.jpt_demo

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    private val notificationSharedPreference = "notificationSharedPreference"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val backButton = btn_back
        val notificationSwitch = switch_allow_notification

        backButton.setOnClickListener {
            onBackPressed()
        }
        val sharedPref : SharedPreferences = this.getSharedPreferences(
            notificationSharedPreference,
            Context.MODE_PRIVATE
        )
        val editor : SharedPreferences.Editor = sharedPref.edit()

        notificationSwitch.setOnClickListener {
            editor.putBoolean("NOTIFICATION_ENABLED", notificationSwitch.isChecked)
            editor.apply()
            editor.commit()
            Log.d("Switch", "switch ${sharedPref.getBoolean(
                "NOTIFICATION_ENABLED",true)
            }")
        }
    }
}