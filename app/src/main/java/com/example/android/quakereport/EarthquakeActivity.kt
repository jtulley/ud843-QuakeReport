/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.quakereport

import android.os.Bundle
import android.app.LoaderManager
import android.content.Context
import android.content.Loader
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import java.text.SimpleDateFormat
import java.util.*


class EarthquakeActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<ArrayList<Earthquake>> {

    //var lastKnownLocation: Location? = null

    companion object {
        val LOG_TAG: String = EarthquakeActivity::class.java.name
        val EARTHQUAKE_LOADER: Int = 1
        val USGS_REQUEST_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationProvider = LocationManager.NETWORK_PROVIDER
        try {
            lastKnownLocation = locationManager.getLastKnownLocation(locationProvider)
        } catch (e: SecurityException) {
            Log.e(LOG_TAG, "security error getting url:" + this, e)
        }
        */

        setContentView(R.layout.earthquake_activity)

        fetchEarthquakeData()

        val refreshLayout = findViewById(R.id.swiperefresh) as SwipeRefreshLayout

        refreshLayout.setOnRefreshListener {
            fetchEarthquakeData()
            Log.v(LOG_TAG, "got a swipe refresh event")
        }
    }

    private fun fetchEarthquakeData() {
        val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork = cm.activeNetworkInfo
        val isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting

        val loaderArgs = Bundle()
        loaderArgs.putString("queryString", buildQueryURL())

        if (isConnected) {
            val progressBarView = findViewById(R.id.main_activity_progress_bar) as ProgressBar
            progressBarView.visibility = View.VISIBLE

            val emptyTextView = findViewById(R.id.main_empty_view) as TextView
            emptyTextView.text = getString(R.string.no_earthquakes_found)

            initOrRestartLoader(loaderArgs)
            Log.v(LOG_TAG, "calling initLoader")
        } else {
            val emptyTextView = findViewById(R.id.main_empty_view) as TextView
            emptyTextView.text = getString(R.string.no_internet)

            displayLoadedEarthquakes(ArrayList())
        }
    }

    fun buildQueryURL(): String {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val minMagnitude = sharedPrefs.getString(
                getString(R.string.settings_min_magnitude_key),
                getString(R.string.settings_min_magnitude_default))

        val limitCount = sharedPrefs.getString(
                getString(R.string.settings_limit_key),
                getString(R.string.settings_limit_default))

        val searchRadiusKm = sharedPrefs.getString(
                getString(R.string.settings_radius_key),
                getString(R.string.settings_radius_default))

        var latitude = "40.3595967"
        var longitude = "-111.7797258"

        val searchLatLong = sharedPrefs.getString(
                getString(R.string.settings_coordinates_key),
                getString(R.string.settings_coordinates_default))
        val latLongArray = searchLatLong.split(" ")
        if (latLongArray.size == 2) {
            latitude = latLongArray[0]
            longitude = latLongArray[1]
        }

        val orderBy = sharedPrefs.getString(
                getString(R.string.settings_orderby_key),
                getString(R.string.settings_orderby_default))

        val dayCount = sharedPrefs.getString(
                getString(R.string.settings_daycount_key),
                getString(R.string.settings_daycount_default))
        val dayCountInt = dayCount.toInt()

        val curTimeMillis = System.currentTimeMillis()
        val starTimeMillis = curTimeMillis - (1000*60*60*24*dayCountInt)
        val startDate = Date(starTimeMillis)
        val dateFormatter = SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ssZ")
        val startDateString = dateFormatter.format(startDate)



        val baseUri = Uri.parse(USGS_REQUEST_URL)
        val uriBuilder = baseUri.buildUpon()
        uriBuilder.appendQueryParameter("format", "geojson")
        if (limitCount.toInt() > 0) {
            uriBuilder.appendQueryParameter("limit", limitCount)
        }
        uriBuilder.appendQueryParameter("minmagnitude", minMagnitude)
        uriBuilder.appendQueryParameter("orderby", orderBy)
        uriBuilder.appendQueryParameter("starttime", startDateString)
        uriBuilder.appendQueryParameter("latitude", latitude)
        uriBuilder.appendQueryParameter("longitude", longitude)
        uriBuilder.appendQueryParameter("maxradiuskm", searchRadiusKm)
        Log.v(LOG_TAG, "built query:" + uriBuilder.toString())
        return uriBuilder.toString()

    }


    fun initOrRestartLoader(args: Bundle?) {
        if (loaderManager.getLoader<ArrayList<Earthquake>>(EARTHQUAKE_LOADER) != null) {
            loaderManager.restartLoader(EARTHQUAKE_LOADER, args, this)
        } else {
            loaderManager.initLoader(EARTHQUAKE_LOADER, args, this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        if (id == R.id.action_settings) {
            val settingsIntent = Intent(this, SettingsActivity::class.java)
            startActivity(settingsIntent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<ArrayList<Earthquake>> {
        Log.v(LOG_TAG, "in onCreateLoader")
        return EarthquakeDataLoader(applicationContext, args.getString("queryString"))
    }

    override fun onLoadFinished(loader: Loader<ArrayList<Earthquake>>?, data: ArrayList<Earthquake>?) {
        displayLoadedEarthquakes(data)
    }

    override fun onLoaderReset(loader: Loader<ArrayList<Earthquake>>?) {
    }

    private fun displayLoadedEarthquakes(earthquakes: ArrayList<Earthquake>?) {
        val refreshLayout = findViewById(R.id.swiperefresh) as SwipeRefreshLayout
        refreshLayout.isRefreshing = false
        Log.v(LOG_TAG, "got " + earthquakes?.size + " items")

        val progressBarView = findViewById(R.id.main_activity_progress_bar) as ProgressBar
        progressBarView.visibility= View.GONE

        if (earthquakes != null) {
            val earthquakeListView = findViewById(R.id.list) as ListView
            val emptyTextView = findViewById(R.id.main_empty_view) as TextView
            earthquakeListView.emptyView = emptyTextView

            // Create a new {@link ArrayAdapter} of earthquakes
            val adapter = EarthquakeDataArrayAdapter(this, earthquakes)

            // Set the adapter on the {@link ListView}
            // so the list can be populated in the user interface
            earthquakeListView.adapter = adapter
            earthquakeListView.visibility = View.VISIBLE
        }
    }
}
