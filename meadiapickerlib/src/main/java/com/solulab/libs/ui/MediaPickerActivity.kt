package com.solulab.libs.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.GestureDetector
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.solulab.libs.R
import com.solulab.libs.adapters.MediaAdapter
import com.solulab.libs.adapters.SpinnerCustomAdapter
import com.solulab.libs.model.MediaModel
import kotlinx.android.synthetic.main.activity_media_picker.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList



class MediaPickerActivity : AppCompatActivity() {

    private var mediaList: MutableList<MediaModel> = ArrayList()
    private var imagesSelected = ArrayList<String>()
    private var mAdapter: MediaAdapter? = null

    private val imageFolderProjection = arrayOf(MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA)
    private val imageDetailsProjection = arrayOf(MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA)

    private val videoFolderProjection = arrayOf(MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media.DATA)
    private val videoDetailsProjection = arrayOf(MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.DATA, MediaStore.Video.Media.DURATION)

    private var bucketNames: MutableList<String> = ArrayList()

    private var spinnerAdapter: SpinnerCustomAdapter? = null

    private var LIMIT = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_picker)
        setSupportActionBar(toolbar)
        setResult(Activity.RESULT_CANCELED)

        LIMIT = intent.getIntExtra("limit", 0)

        if (LIMIT == 0) {
            finishAll()
            Toast.makeText(this,"Limit is zero!",Toast.LENGTH_LONG).show()
            return
        }
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.v_ic_arrow_back_white)
        }
        imagesSelected.clear()
        mediaList.clear()
        bucketNames.clear()
        bucketNames.add(getString(R.string.all_media_spiner))
        getPicBuckets()

        spinnerAdapter = SpinnerCustomAdapter(
            this,
            R.id.tv_spinner_item,
            bucketNames.toTypedArray()
        )
        anchor_action.adapter = spinnerAdapter

        anchor_action?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                mediaList.clear()
                if (position == 0) {
                    //For All media list
                    val allMedia = AllMediaFetch(bucketNames, this@MediaPickerActivity)
                    allMedia.execute()
                } else {
                    getPictures(bucketNames[position])
                    //#TODO Video In next Phase
                    getVideos(bucketNames[position])
                }
                mAdapter!!.notifyDataSetChanged()
            }
        }

        mAdapter = MediaAdapter(mediaList, applicationContext)
        val mLayoutManager = GridLayoutManager(applicationContext, 3)
        recyclerView!!.layoutManager = mLayoutManager
        recyclerView!!.itemAnimator!!.changeDuration = 0

        recyclerView.adapter = mAdapter

        recyclerView!!.addOnItemTouchListener(RecyclerTouchListener(this, recyclerView, object : ClickListener {
            override fun onClick(view: View, position: Int) {

                if (mediaList[position].isSelected) {
                    imagesSelected.removeAt(imagesSelected.indexOf(mediaList[position].mediaPath))
                    mediaList[position].isSelected = !mediaList[position].isSelected
                } else if (imagesSelected.size == LIMIT) {
                    //Todo Need to set limit from previous activity.
                    Toast.makeText(
                        this@MediaPickerActivity,
                        "Max items limit reached\nYou can select $LIMIT items",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                } else {
                    if (mediaList[position].dur != null) {
                        mediaList[position].isVideo = true
                        imagesSelected.add(mediaList[position].mediaPath!!)
                        mediaList[position].isSelected = !mediaList[position].isSelected
                    } else {
                        mediaList[position].isVideo = false
                        imagesSelected.add(mediaList[position].mediaPath!!)
                        mediaList[position].isSelected = !mediaList[position].isSelected
                    }

                    if (LIMIT == 1) {
                        tvDoneSelection.callOnClick()
                        return
                    }
                }
                mAdapter!!.notifyItemChanged(position)
                when {
                    imagesSelected.size == LIMIT -> {
                        tvDoneSelection.setText(R.string.done)
                        tvDoneSelection.visibility = View.VISIBLE
                    }
                    imagesSelected.size == 0 -> tvDoneSelection.visibility = View.GONE
                    else -> {
                        tvDoneSelection.text = "Select(${LIMIT}/${imagesSelected.count()})"
                        tvDoneSelection.visibility = View.VISIBLE
                    }
                }
            }

            override fun onLongClick(view: View?, position: Int) {

            }
        }))
        tvDoneSelection.visibility = View.GONE
        tvDoneSelection.setOnClickListener {
            setMediaResult()
        }
    }

    private fun setMediaResult() {
        val intent = Intent()
        intent.putExtra("result", imagesSelected)
        setResult(Activity.RESULT_OK, intent)
        finishAll()
    }

    private class AllMediaFetch(private val bucketNames: MutableList<String>,private val mediaPickerActivity: MediaPickerActivity) : AsyncTask<Void, Void, String>() {

        override fun doInBackground(vararg params: Void?): String? {
            for (i in 1 until bucketNames.size) {
                mediaPickerActivity.getPictures(bucketNames[i])
                mediaPickerActivity.getVideos(bucketNames[i])
            }
            return ""
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            mediaPickerActivity.mAdapter!!.notifyDataSetChanged()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAll()
    }

    private interface ClickListener {
        fun onClick(view: View, position: Int)

        fun onLongClick(view: View?, position: Int)
    }

    private class RecyclerTouchListener(
        context: Context,
        recyclerView: RecyclerView,
        private val clickListener: ClickListener?
    ) : RecyclerView.OnItemTouchListener {
        private val gestureDetector: GestureDetector

        init {
            gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    return true
                }

                override fun onLongPress(e: MotionEvent) {
                    val child = recyclerView.findChildViewUnder(e.x, e.y)
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child))
                    }
                }
            })
        }

        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            val child = rv.findChildViewUnder(e.x, e.y)
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child))
            }
            return false
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
    }

    private fun getPicBuckets() {
        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                imageFolderProjection,
                null,
                null,
                MediaStore.Images.Media.DATE_MODIFIED
            )
            val bucketNamesTEMP = ArrayList<String>(cursor!!.count)
            val bitmapListTEMP = ArrayList<String>(cursor.count)
            val albumSet = HashSet<String>()
            var file: File
            if (cursor.moveToLast()) {
                do {
                    if (Thread.interrupted()) {
                        return
                    }
                    val album = cursor.getString(cursor.getColumnIndex(imageFolderProjection[0]))
                    val image = cursor.getString(cursor.getColumnIndex(imageFolderProjection[1]))
                    file = File(image)
                    if (file.exists() && !albumSet.contains(album)) {
                        bucketNamesTEMP.add(album)
                        bitmapListTEMP.add(image)
                        albumSet.add(album)
                    }
                } while (cursor.moveToPrevious())
            }
            cursor.close()

            bucketNames.addAll(bucketNamesTEMP)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor!!.close()
        }
        getVideoBuckets()
    }

    private fun getPictures(bucket: String) {
        var cursor: Cursor? = null
        try {
            cursor = contentResolver
                .query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageDetailsProjection,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " =?",
                    arrayOf(bucket),
                    MediaStore.Images.Media.DATE_MODIFIED
                )
            val albumSet = HashSet<String>()
            var file: File
            if (cursor!!.moveToLast()) {
                do {
                    if (Thread.interrupted()) {
                        return
                    }
                    val path = cursor.getString(cursor.getColumnIndex(imageDetailsProjection[1]))
                    file = File(path)
                    if (file.exists() && !albumSet.contains(path)) {
                        val mode = MediaModel()
                        mode.mediaPath = path
                        mode.isSelected = imagesSelected.contains(path)

                        mediaList.add(mode)
                        albumSet.add(path)
                    }
                } while (cursor.moveToPrevious())
            }
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor!!.close()
        }
    }

    private fun getVideoBuckets() {
        var cursor: Cursor? = null
        try {
            val orderBy = MediaStore.Video.Media.DATE_MODIFIED

            cursor = contentResolver
                .query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoFolderProjection,
                    null, null,
                    orderBy
                )

            val albumSet = HashSet<String>()
            var file: File
            if (cursor!!.moveToLast()) {
                do {
                    if (Thread.interrupted()) {
                        return
                    }
                    val album = cursor.getString(cursor.getColumnIndex(videoFolderProjection[0]))
                    val image = cursor.getString(cursor.getColumnIndex(videoFolderProjection[1]))
                    file = File(image)
                    if (file.exists() && !albumSet.contains(album)) {
                        if (!bucketNames.contains(album)) {
                            bucketNames.add(album)
                        }
                        albumSet.add(album)
                    }
                } while (cursor.moveToPrevious())
            }
            cursor.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            cursor!!.close()
        }
    }

    private fun getVideos(bucket: String) {
        var cursor: Cursor? = null
        try {
            val whereArgs = arrayOf(bucket)
            val orderBy = MediaStore.Video.Media.DATE_MODIFIED
            val where = (MediaStore.Video.Media.BUCKET_DISPLAY_NAME + " =?" )

            cursor = contentResolver
                .query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    videoDetailsProjection,
                    where,
                    whereArgs,
                    orderBy
                )
            val albumSet = HashSet<String>()
            var file: File
            if (cursor!!.moveToLast()) {
                do {
                    if (Thread.interrupted()) {
                        return
                    }
                    val path = cursor.getString(cursor.getColumnIndex(videoDetailsProjection[1]))
                    val duration = cursor.getString(cursor.getColumnIndex(videoDetailsProjection[2]))

                    file = File(path)
                    Log.d("file.extension=",""+file.extension)
                    if (file.exists() && !albumSet.contains(path)) {
                        val mode = MediaModel()
                        mode.mediaPath = path
                        mode.dur = duration
                        mode.isSelected = imagesSelected.contains(path)
                        mediaList.add(mode)
                    }
                } while (cursor.moveToPrevious())
            }
            cursor.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            cursor!!.close()
        }
    }

    private fun finishAll() {
        finish()
    }
}