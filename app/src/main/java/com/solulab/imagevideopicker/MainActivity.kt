package com.solulab.imagevideopicker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.solulab.libs.ui.MediaPickerActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileNotFoundException

class MainActivity : AppCompatActivity() {

    private val PER_CODE = 5001
    private val REQ_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        setButtonTint(fab, ContextCompat.getColorStateList(applicationContext, R.color.fabColor)!!)
        fab.setOnClickListener { view ->
            if (!permissionIfNeeded()) {
                val intent = Intent(this, MediaPickerActivity::class.java)
                intent.putExtra("limit",10)
                startActivityForResult(intent, REQ_CODE)
            }
        }
    }

    fun setButtonTint(button: FloatingActionButton, tint: ColorStateList) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            button.backgroundTintList = tint
        } else {
            ViewCompat.setBackgroundTintList(button, tint)
        }
    }

    private fun permissionIfNeeded(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Explain to the user why we need to read the contacts
                }
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),PER_CODE)
                return true
            }

        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val selectionResult = data.getStringArrayListExtra("result")
                selectionResult.forEach {
                    try {
                        val uriFromPath = Uri.fromFile(File(it))
                        Log.d("MyApp", "Image URI : " + uriFromPath)
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}
