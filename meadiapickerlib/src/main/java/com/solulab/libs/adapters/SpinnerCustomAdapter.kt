package com.solulab.libs.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.solulab.libs.R

class SpinnerCustomAdapter(context: Context, textViewResourceId: Int, private val itemList: Array<String>) :
    ArrayAdapter<String>(context, textViewResourceId, itemList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        @SuppressLint("ViewHolder")
        val row = LayoutInflater.from(context).inflate(R.layout.item_media_picker_spinner, parent, false)
        val v = row.findViewById<TextView>(R.id.tv_spinner_item)
        try {
            v.text = itemList[position]
            v.setTextColor(Color.WHITE)
            v.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.v_ic_mediapicker_spnr_arrow, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return row
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row = LayoutInflater.from(context).inflate(R.layout.item_media_picker_spinner, parent, false)
        val v = row.findViewById<TextView>(R.id.tv_spinner_item)
        try {
            v.text = itemList[position]
            v.setTextColor(Color.BLACK)
            v.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        v.setPadding(25, 50, 25, 50)
        return row
    }
}