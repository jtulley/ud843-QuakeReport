package com.example.android.quakereport

import java.util.*

/**
 * Created by jefftulley on 2/8/18.
 */
data class Earthquake(val magnitude: Double, val location: String, val dateTime: Long, val detailsUrl: String)
