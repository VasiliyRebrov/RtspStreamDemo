package com.example.rtspstreamdemo

import android.os.Bundle
import android.view.SurfaceHolder
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pedro.encoder.input.video.CameraHelper
import com.pedro.encoder.input.video.CameraHelper.Facing
import com.pedro.encoder.input.video.CameraOpenException
import com.pedro.rtplibrary.view.OpenGlView
import com.pedro.rtsp.utils.ConnectCheckerRtsp
import com.pedro.rtspserver.RtspServerCamera2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CameraDemoActivity: AppCompatActivity() {

// MARK: - Methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_camera_demo)

        initComponents()
    }

// MARK: - Private Methods

    private fun initComponents() {

        _primaryActionButton.setOnClickListener(this::onPrimaryActionButtonClicked)
        _switchCameraButton.setOnClickListener(this::onSwitchCameraButtonClicked)
        _secondaryActionButton.setOnClickListener(this::onSecondaryActionClicked)

        _openGlView.holder.addCallback(_surfaceHolderCallback)

        _rtspServerCamera2 = RtspServerCamera2(_openGlView, _connectCheckerRtspImpl, Endpoint.PORT)
    }

    private fun onPrimaryActionButtonClicked(view: View) {
        val isNotStreaming = !_rtspServerCamera2.isStreaming

        when (isNotStreaming) {
            true -> startStream()
            else -> stopStream()
        }
    }

    private fun onSwitchCameraButtonClicked(view: View) {
        try {
            _rtspServerCamera2.switchCamera()

            _cameraFacing = _rtspServerCamera2.cameraFacing

            GlobalScope.launch(Dispatchers.Main) {

                withContext(Dispatchers.IO) {
                    delay(50)
                }

                if (_cameraFacing == Facing.BACK) {

                    if (_isFlashOn) {
                        switchFlash()
                    }

                    _openGlView.setCameraFlip(false, false)
                    _secondaryActionButton.setText(R.string.switch_flash_button)
                }
                else {
                    _openGlView.setCameraFlip(_isFrontCameraFlipHorizontal, false)
                    _secondaryActionButton.setText(R.string.flip_horizontal_button)
                }
            }
        }
        catch (e: CameraOpenException) {
            showToast(text = e.message)
        }
    }

    private fun onSecondaryActionClicked(view: View) {
        when (_cameraFacing) {
            Facing.BACK -> switchFlash()
            Facing.FRONT -> flipFrontCamera()
        }
    }

    private fun startStream() {
        val preparatorySuccess = prepareVideo() && prepareAudio()

        when (preparatorySuccess) {
            true -> {
                _rtspServerCamera2.startStream()
                _primaryActionButton.setText(R.string.stop_button)
                _urlText.text = _rtspServerCamera2.getEndPointConnection()
            }
            else -> {
                showToast(text = "Error preparing stream, This device cant do it")
            }
        }
    }

    private fun stopStream() {
        _rtspServerCamera2.stopStream()

        _primaryActionButton.setText(R.string.start_button)
        _urlText.text = ""
    }

    private fun switchFlash() {
        try {

            val isFlashOn = _rtspServerCamera2.isLanternEnabled
            when (isFlashOn) {
                true -> _rtspServerCamera2.disableLantern()
                false -> _rtspServerCamera2.enableLantern()
            }

            _isFlashOn = !isFlashOn
        }
        catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun flipFrontCamera() {
        _isFrontCameraFlipHorizontal = !_isFrontCameraFlipHorizontal
        _openGlView.setCameraFlip(_isFrontCameraFlipHorizontal, false)
    }

    private fun prepareVideo(): Boolean {
        val videoFPS = Integer.parseInt(_preferencesProvider.videoFPS)
        val videoBitrate = parseBitrate(_preferencesProvider.videoBitrate)

        val preparatorySuccess = _rtspServerCamera2.prepareVideo(
            640,
            480,
            videoFPS,
            videoBitrate,
            CameraHelper.getCameraOrientation(this),
        )

        return preparatorySuccess
    }

    private fun prepareAudio(): Boolean {
        val audioBitrate = parseBitrate(_preferencesProvider.audioBitrate)
        val audioSampleRate = Integer.parseInt(_preferencesProvider.audioSampleRate)
        val isStereo = Integer.parseInt(_preferencesProvider.audioChannel) == 1

        val preparatorySuccess = _rtspServerCamera2.prepareAudio(
            audioBitrate,
            audioSampleRate,
            isStereo,
        )

        return preparatorySuccess
    }

    private fun showToast(text: String?) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    private fun parseBitrate(bitrate: String): Int {
        return Integer.parseInt(bitrate) * 1024
    }

// MARK: - Inner Types

    private inner class ConnectCheckerRtspImpl: ConnectCheckerRtsp {

        override fun onNewBitrateRtsp(bitrate: Long) {
            // Do nothing
        }

        override fun onConnectionSuccessRtsp() {
            runOnUiThread {
                this@CameraDemoActivity.showToast(text = "Connection success")
            }
        }

        override fun onConnectionFailedRtsp(reason: String) {
            runOnUiThread {
                this@CameraDemoActivity.showToast(text = "Connection failed. $reason")

                _rtspServerCamera2.stopStream()

                _primaryActionButton.setText(R.string.start_button)
            }
        }

        override fun onConnectionStartedRtsp(rtspUrl: String) {
            // Do nothing
        }

        override fun onDisconnectRtsp() {
            runOnUiThread {
                this@CameraDemoActivity.showToast(text = "Disconnected")
            }
        }

        override fun onAuthErrorRtsp() {
            runOnUiThread {
                this@CameraDemoActivity.showToast(text = "Auth error")

                _rtspServerCamera2.stopStream()

                _primaryActionButton.setText(R.string.start_button)
                _urlText.text = ""
            }
        }

        override fun onAuthSuccessRtsp() {
            runOnUiThread {
                this@CameraDemoActivity.showToast(text = "Auth success")
            }
        }
    }

    private inner class SurfaceHolderCallback: SurfaceHolder.Callback {

        override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
            // Do nothing
        }

        override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {
            _rtspServerCamera2.startPreview()
        }

        override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
            if (_rtspServerCamera2.isStreaming) {
                _rtspServerCamera2.stopStream()

                _primaryActionButton.text = resources.getString(R.string.start_button)
                _urlText.text = ""
            }
            _rtspServerCamera2.stopPreview()
        }
    }

// MARK: - Constants

    private object Endpoint {
        const val PORT = 1935
    }

// MARK: - Variables

    private val _openGlView: OpenGlView by lazy {
        findViewById(R.id.openGlView)
    }

    private val _urlText: TextView by lazy {
        findViewById(R.id.urlText)
    }

    private val _primaryActionButton: Button by lazy {
        findViewById(R.id.primaryActionButton)
    }

    private val _switchCameraButton: Button by lazy {
        findViewById(R.id.switchCameraButton)
    }

    private val _secondaryActionButton: Button by lazy {
        findViewById(R.id.secondaryActionButton)
    }

    private val _connectCheckerRtspImpl: ConnectCheckerRtspImpl by lazy {
        ConnectCheckerRtspImpl()
    }
    private val _surfaceHolderCallback: SurfaceHolderCallback by lazy {
        SurfaceHolderCallback()
    }

    private val _preferencesProvider: PreferencesProvider by lazy {
        PreferencesProvider(this)
    }

    private lateinit var _rtspServerCamera2: RtspServerCamera2

    private var _cameraFacing: Facing = Facing.BACK
    private var _isFlashOn: Boolean = false
    private var _isFrontCameraFlipHorizontal: Boolean = true
}
