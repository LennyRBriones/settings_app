package com.lennyrbriones.settingsapp

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lennyrbriones.settingsapp.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : AppCompatActivity() {

    companion object {
        const val VOLUME_LVL = "volume_lvl"
        const val KEY_BLUETOOTH = "key_bluetooth"
        const val KEY_VIBRATION = "key_vibration"
        const val KEY_DARK_MODE = "key_dark_mode"

    }

    private lateinit var binding: ActivityMainBinding
    private var firstTime: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CoroutineScope(Dispatchers.IO).launch {
            getSettings().filter { firstTime }.collect { settingsModel ->
                if (settingsModel != null) {
                    runOnUiThread {
                        binding.switchvibration.isChecked = settingsModel.vibration
                        binding.switchbluethoot.isChecked = settingsModel.bluetooth
                        binding.switchdarkmode.isChecked = settingsModel.darkmode
                        binding.rsVolume.setValues(settingsModel.volume.toFloat())
                        firstTime = !firstTime
                    }
                }// Data SettingModel()
            }

        }
        initUI()

    }

    private fun initUI() {
        binding.rsVolume.addOnChangeListener { _, value, _ ->
            CoroutineScope(Dispatchers.IO).launch {
                saveVolume(value.toInt())
            }

        }
        binding.switchbluethoot.setOnCheckedChangeListener { _, value ->
            CoroutineScope(Dispatchers.IO).launch {
                saveOptions("KEY_BLUETOOTH", value)
            }
        }
        binding.switchvibration.setOnCheckedChangeListener { _, value ->
            CoroutineScope(Dispatchers.IO).launch {
                saveOptions("KEY_VIBRATION", value)
            }
        }
        binding.switchdarkmode.setOnCheckedChangeListener { _, value ->
            if (value){
                enableDarkMode()
            }else{

                disableDarkMode()
            }

            CoroutineScope(Dispatchers.IO).launch {
                saveOptions("KEY_DARK_MODE", value)
            }

        }

    }

    private suspend fun saveVolume(value: Int) {
        dataStore.edit { preferences ->
            preferences[intPreferencesKey(VOLUME_LVL)] = value
        }

    }

    private suspend fun saveOptions(key: String, value: Boolean) {
        dataStore.edit { preferences ->
            preferences[booleanPreferencesKey(key)] = value
        }
    }

    //Flow
    private fun getSettings(): Flow<SettingModel?> {
        return dataStore.data.map { preferences ->
            SettingModel(
                volume = preferences[intPreferencesKey(VOLUME_LVL)] ?: 50,
                bluetooth = preferences[booleanPreferencesKey(KEY_BLUETOOTH)] ?: true, // in case of null
                vibration = preferences[booleanPreferencesKey(KEY_VIBRATION)] ?: true,
                darkmode = preferences[booleanPreferencesKey(KEY_DARK_MODE)] ?: false
            )

        }
    }

    private fun enableDarkMode() {
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        delegate.applyDayNight()
    }

    private fun disableDarkMode() {
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
        delegate.applyDayNight()

    }

}