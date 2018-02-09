package com.example.android.quakereport

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import java.text.DecimalFormat
import java.text.SimpleDateFormat

/**
 * Created by jefftulley on 2/8/18.
 */
class EarthquakeDataArrayAdapter(context: Context, eqData: ArrayList<Earthquake>) : ArrayAdapter<Earthquake>(context, 0, eqData) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        var listItemView = convertView
        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.earthquake_data_layout, parent, false)
            listItemView!!.setBackgroundResource(R.color.material_grey_300)
        }

        val curItem = getItem(position)

        val magTextView = listItemView.findViewById(R.id.earthquake_data_magnitude) as TextView
        val decimalFormat = DecimalFormat("0.0")
        magTextView.text = decimalFormat.format(curItem.magnitude)

        val magnitudeCircle = magTextView.background as GradientDrawable
        magnitudeCircle.setColor(getMagnitudeCircleColor(context, curItem.magnitude))

        var primaryString = curItem.location
        var locationOffsetString = "Near the"

        val ofLocation = curItem.location.indexOf(" of ")
        if (ofLocation > -1) {
            locationOffsetString =  curItem.location.substring(0, ofLocation + 4)
            primaryString =  curItem.location.substring(ofLocation + 4)
        }

        if (!locationOffsetString.equals("")) {
            val locTextView = listItemView.findViewById(R.id.earthquake_data_location_1) as TextView
            locTextView.text = locationOffsetString
        }

        val locTextView = listItemView.findViewById(R.id.earthquake_data_location_2) as TextView
        locTextView.text = primaryString

        val dateFormatter = SimpleDateFormat("MMM d, yyyy")
        val timeFormatter = SimpleDateFormat("h:mm a")

        val dateTextView = listItemView.findViewById(R.id.earthquake_data_date) as TextView
        dateTextView.text = dateFormatter.format(curItem.dateTime)

        val timeTextView = listItemView.findViewById(R.id.earthquake_data_time) as TextView
        timeTextView.text = timeFormatter.format(curItem.dateTime)

        listItemView.setOnClickListener {
            var intent = Intent(Intent.ACTION_VIEW)
            intent.setData(Uri.parse(curItem.detailsUrl))
            context.startActivity(intent)
        }

        return listItemView
    }

    fun getMagnitudeCircleColor(context: Context, magnitude: Double) : Int {
        var resourceColor = 0
        var magnitudeFloor: Int = Math.floor(magnitude).toInt()
        when (magnitudeFloor) {
            1 -> resourceColor =  R.color.magnitude1
            2 -> resourceColor =  R.color.magnitude2
            3 -> resourceColor =  R.color.magnitude3
            4 -> resourceColor =  R.color.magnitude4
            5 -> resourceColor =  R.color.magnitude5
            6 -> resourceColor =  R.color.magnitude6
            7 -> resourceColor =  R.color.magnitude7
            8 -> resourceColor =  R.color.magnitude8
            9 -> resourceColor =  R.color.magnitude9
            else -> resourceColor =  R.color.magnitude10plus
        }
        return ContextCompat.getColor(context, resourceColor)
    }

}