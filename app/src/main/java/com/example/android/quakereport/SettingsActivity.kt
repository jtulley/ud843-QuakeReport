package com.example.android.quakereport

import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity

/**
 * Created by jefftulley on 2/12/18.
 */

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
    }

    class EarthquakePreferenceFragment : PreferenceFragment(), Preference.OnPreferenceChangeListener {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.settings_main)
            bindPreferenceSummaryToValue(findPreference(getString(R.string.settings_min_magnitude_key)))
            bindPreferenceSummaryToValue(findPreference(getString(R.string.settings_limit_key)))
            bindPreferenceSummaryToValue(findPreference(getString(R.string.settings_radius_key)))
            bindPreferenceSummaryToValue(findPreference(getString(R.string.settings_coordinates_key)))
            bindPreferenceSummaryToValue(findPreference(getString(R.string.settings_orderby_key)))
            bindPreferenceSummaryToValue(findPreference(getString(R.string.settings_daycount_key)))
        }

        fun bindPreferenceSummaryToValue(preference: Preference) {
            preference.onPreferenceChangeListener = this
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(preference.context)
            val preferenceString = sharedPreferences.getString(preference.key, "")
            onPreferenceChange(preference, preferenceString)

        }

        override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
            val stringValue = newValue.toString()
            if (preference is ListPreference ) {
                val prefIndex = preference.findIndexOfValue(stringValue)
                if (prefIndex >= 0) {
                    val labels = preference.entries
                    preference.summary = labels[prefIndex]
                }
            } else {
                preference.summary = stringValue
            }
            return true
        }
    }
}