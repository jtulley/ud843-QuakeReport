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

import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.widget.ContentLoadingProgressBar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class EarthquakeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.earthquake_activity)
        val progressBarView = findViewById(R.id.main_activity_progress_bar) as ProgressBar
        progressBarView.visibility= View.VISIBLE

        val earthquakeLoadAsyncTask = EarthquakeLoadAsyncTask()
        earthquakeLoadAsyncTask.execute(URL("https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=2018-02-08&latitude=40.3595967&longitude=-111.7797258&maxradiuskm=2000&minmagnitude=2.00"))
    }

    private fun displayLoadedEarthquakes(earthquakes: ArrayList<Earthquake>?) {
        if (earthquakes != null) {
            val progressBarView = findViewById(R.id.main_activity_progress_bar) as ContentLoadingProgressBar
            progressBarView.visibility = View.GONE

            if (earthquakes.size == 0) {
                val errorView = findViewById(R.id.main_activity_error) as TextView
                if (errorView.visibility != View.VISIBLE) {
                    errorView.text = "No earthquakes found"
                    errorView.visibility = View.VISIBLE
                } // else: an error occurred.  Leave it on the screen
            } else {
                // Find a reference to the {@link ListView} in the layout
                val earthquakeListView = findViewById(R.id.list) as ListView?

                // Create a new {@link ArrayAdapter} of earthquakes
                val adapter = EarthquakeDataArrayAdapter(this, earthquakes)

                // Set the adapter on the {@link ListView}
                // so the list can be populated in the user interface
                earthquakeListView!!.adapter = adapter
                earthquakeListView.visibility = View.VISIBLE
            }
        }
    }

    private fun displayUpdatedProgress(progressAmount: EarthquakeDataLoadProgress?) {
        progressAmount?.let {
            val progressBarView = findViewById(R.id.main_activity_progress_bar) as ContentLoadingProgressBar
            val errorView = findViewById(R.id.main_activity_error) as TextView

            if (progressAmount.error != null) {
                progressBarView.visibility = View.GONE
                errorView.visibility = View.VISIBLE
                errorView.text = progressAmount.error.toString()
            } else {
                val progressBarView = findViewById(R.id.main_activity_progress_bar) as ContentLoadingProgressBar
                progressBarView.visibility = View.VISIBLE
                progressBarView.progress = progressAmount.progress
            }
        }
    }


    companion object {
        val LOG_TAG: String = EarthquakeActivity::class.java.name
    }

    /**
     * {@link AsyncTask} to perform the network request on a background thread, and then
     * update the UI with the first earthquake in the response.
     */
    private inner class EarthquakeLoadAsyncTask : AsyncTask<URL, EarthquakeDataLoadProgress, ArrayList<Earthquake>>() {
        override fun doInBackground(vararg params: URL?): ArrayList<Earthquake> {
            // Perform HTTP request to the URL and receive a JSON response back
            publishProgress(EarthquakeDataLoadProgress(20))
            var jsonResponse: String = ""
            if (params.size > 0) {
                params[0]?.let {
                    try {
                        jsonResponse = makeHttpRequest(it);
                        publishProgress(EarthquakeDataLoadProgress(70))
                    } catch (e: IOException) {
                        Log.e(LOG_TAG, "error getting url:" + this, e)
                        publishProgress(EarthquakeDataLoadProgress(70, e))
                        return ArrayList<Earthquake>()
                    }
                }
            }
            //return QueryUtils.dummyData()
            val response = QueryUtils.parseJsonData(jsonResponse)
            publishProgress(EarthquakeDataLoadProgress(100))
            return response
        }

        override fun onProgressUpdate(vararg values: EarthquakeDataLoadProgress?) {
            displayUpdatedProgress(values[0])
        }

        override fun onPostExecute(result: ArrayList<Earthquake>?) {
            displayLoadedEarthquakes(result)
        }

        fun makeHttpRequest(url: URL): String {
            var response: String = ""
            var urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection;
            urlConnection?.readTimeout=10000
            urlConnection?.connectTimeout = 15000
            try {
                urlConnection?.connect()
                if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = urlConnection.getInputStream();
                    response = inputStream.bufferedReader().use { it.readText() }
                } else {
                    val errorMessage =  "got unexpected Http status code: " + urlConnection.responseCode + ", " + urlConnection.responseMessage
                    Log.v(LOG_TAG, errorMessage)
                    throw java.io.IOException(errorMessage)
                }
            } catch (e: SecurityException) {
                // get permissions?
            }
            return response
        }
    }
}
