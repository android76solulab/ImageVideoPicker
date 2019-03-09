package com.solulab.libs.adapters


import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.Glide
import com.solulab.libs.R
import com.solulab.libs.model.MediaModel

class MediaAdapter(private val mediaList: List<MediaModel>, private val context: Context) : RecyclerView.Adapter<MediaAdapter.MyViewHolder>() {
    private val inflater: LayoutInflater
    init {
        inflater = LayoutInflater.from(context)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = inflater.inflate(R.layout.item_media_picker, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load("file://" + mediaList[position].mediaPath)
            .transition(DrawableTransitionOptions().crossFade())
            .apply(
                RequestOptions()
                    .centerCrop()
                    .dontAnimate()
                    .skipMemoryCache(true)
            )
            .into(holder.thumbnail)
        try {
            if (mediaList[position].dur != null) {
                holder.tvDuration.visibility = View.VISIBLE
                var secDuration: Int = mediaList[position].dur!!.toInt() / 1000
                val hr = secDuration / 3600
                secDuration %= 3600
                val min = secDuration / 60
                val sec = secDuration % 60
                if (hr > 0) {
                    holder.tvDuration.text = String.format("%02d:%02d:%02d", hr, min, sec)
                } else {
                    holder.tvDuration.text = String.format("%02d:%02d", min, sec)
                }
            } else {
                holder.tvDuration.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (mediaList[position].isSelected) {
            holder.check.visibility = View.VISIBLE
            holder.check.imageAlpha = 150
        } else {
            holder.check.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return mediaList.size
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var thumbnail: ImageView
        var check: ImageView
        var tvDuration: TextView

        init {
            thumbnail = view.findViewById<View>(R.id.image) as ImageView
            check = view.findViewById<View>(R.id.image2) as ImageView
            tvDuration = view.findViewById<View>(R.id.tvDuration) as TextView
        }
    }
}