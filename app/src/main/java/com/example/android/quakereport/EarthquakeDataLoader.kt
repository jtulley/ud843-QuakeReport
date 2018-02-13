package com.example.android.quakereport

import android.content.AsyncTaskLoader
import android.content.Context
import android.util.Log
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by jefftulley on 2/12/18.
 */
class EarthquakeDataLoader(context: Context, val queryString: String) : AsyncTaskLoader<ArrayList<Earthquake>>(context) {
    var eqData: ArrayList<Earthquake> = ArrayList()

    override fun onStartLoading() {
        forceLoad()
    }

    override fun loadInBackground(): ArrayList<Earthquake> {
        Log.v(LOG_TAG, "in loadinBackground")
        val queryUrl = URL(queryString)
        var jsonResponse: String
        try {
            jsonResponse = makeHttpRequest(queryUrl);
        } catch (e: IOException) {
            Log.e(LOG_TAG, "error getting url:" + this, e)
            return ArrayList<Earthquake>()
        } catch (e: SecurityException) {
            Log.e(LOG_TAG, "security error getting url:" + this, e)
            return ArrayList<Earthquake>()
        }

        //return QueryUtils.dummyData()
        eqData =  QueryUtils.parseJsonData(jsonResponse)
        return eqData
    }

    companion object {
        val LOG_TAG: String = EarthquakeDataLoader::class.java.name
    }

    private fun makeHttpRequest(url: URL): String {
        var response: String
        var urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection;
        urlConnection.readTimeout=10000
        urlConnection.connectTimeout = 15000
        urlConnection.connect()
        if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
            val inputStream = urlConnection.getInputStream();
            response = inputStream.bufferedReader().use { it.readText() }
        } else {
            throw java.io.IOException("got unexpected Http status code: " + urlConnection.responseCode + ", " + urlConnection.responseMessage)
        }
        return response
    }
}