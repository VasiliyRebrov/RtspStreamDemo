package com.example.rtspstreamdemo

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.preference.PreferenceManager

class PreferencesProvider(context: Context) {

// MARK: - Methods

    val videoBitrate: String
        get() = getString(
            prefKeyResId = R.string.prefKey_bitrate_video,
            defaultValueResId = R.string.bitrate_video_default_value,
        )

    val videoFPS: String
        get() = getString(
            prefKeyResId = R.string.prefKey_fps,
            defaultValueResId = R.string.fps_default_value,
        )

    val audioBitrate: String
        get() = getString(
            prefKeyResId = R.string.prefKey_bitrate_audio,
            defaultValueResId = R.string.bitrate_audio_default_value,
        )

    val audioSampleRate: String
        get() = getString(
            prefKeyResId = R.string.prefKey_sample_rate,
            defaultValueResId = R.string.sample_rate_default_value,
        )

    val audioChannel: String
        get() = getString(
            prefKeyResId = R.string.prefKey_channel,
            defaultValueResId = R.string.channel_default_value,
        )

// MARK: - Private Methods

    private fun getString(
        @StringRes prefKeyResId: Int,
        @StringRes defaultValueResId: Int,
    ): String {

        val prefKey = _resources.getString(prefKeyResId)
        val defaultValue = _resources.getString(defaultValueResId)

        return requireNotNull(_sharedPreferences.getString(prefKey, defaultValue))
    }

// MARK: - Variables

    private val _resources: Resources = context.resources
    private val _sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
}
