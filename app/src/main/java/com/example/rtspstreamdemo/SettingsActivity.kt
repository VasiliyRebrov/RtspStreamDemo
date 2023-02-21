package com.example.rtspstreamdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class SettingsActivity: AppCompatActivity() {

// MARK: - Methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settingsContainer, SettingsFragment())
            .commit()
    }
}
