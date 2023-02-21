package com.example.rtspstreamdemo

import android.Manifest.permission
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity: AppCompatActivity() {

// MARK: - Methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)

        _cameraDemoButton.setOnClickListener(this::onCameraButtonClicked)
        _settingsButton.setOnClickListener(this::onSettingsButtonClicked)
    }

// MARK: - Private Methods

    private fun onCameraButtonClicked(view: View) {
        when (hasPermissions()) {
            true -> startActivity(Intent(this, CameraDemoActivity::class.java))
            else -> _permissionsRequestLauncher.launch(Const.PERMISSIONS)
        }
    }

    private fun onSettingsButtonClicked(view: View) {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun hasPermissions(): Boolean {
        Const.PERMISSIONS.forEach { permission ->

            val permissionResult = ActivityCompat
                .checkSelfPermission(this, permission)

            if (permissionResult != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }

        return true
    }

    private fun handlePermissionsRequestResult() {
        if (hasPermissions()) {
            startActivity(Intent(this, CameraDemoActivity::class.java))
        }
    }

// MARK: - Constants

    private object Const {
        val PERMISSIONS = arrayOf(
            permission.RECORD_AUDIO,
            permission.CAMERA,
            permission.WRITE_EXTERNAL_STORAGE,
        )
    }
// MARK: - Variables

    private val _cameraDemoButton: Button by lazy {
        findViewById(R.id.cameraDemoButton)
    }

    private val _settingsButton: Button by lazy {
        findViewById(R.id.settingsButton)
    }

    private val _permissionsRequestLauncher =
        registerForActivityResult(RequestMultiplePermissions()) {
            handlePermissionsRequestResult()
        }
}
