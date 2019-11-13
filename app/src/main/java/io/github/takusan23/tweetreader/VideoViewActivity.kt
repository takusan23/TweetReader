package io.github.takusan23.tweetreader

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.MediaController
import android.widget.Toast
import androidx.core.net.toUri
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_image_view.*
import kotlinx.android.synthetic.main.activity_video_view.*
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.net.Uri


class VideoViewActivity : AppCompatActivity() {

    val mediaSave = MediaSave()
    var videoLink = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_view)

        supportActionBar?.hide()

        //Uri
        videoLink = intent.getStringExtra("url") ?: ""
        // videoLink = "https://developer.android.com/images/home/android-p-clear-bg-with-shadow.png?hl=JA"
        videoview_activity_videoview.setVideoURI(videoLink.toUri())
        videoview_activity_videoview.setMediaController(MediaController(this))
        //再生可能になったら再生
        videoview_activity_videoview.setOnPreparedListener {
            videoview_activity_videoview.start()
        }

        val statusText = intent.getStringExtra("status") ?: "test"

        //保存機能？
        videoview_activity_save_button.setOnClickListener {
            mediaSave.createFile(this, "$statusText.mp4")
        }
        //閉じるボタン
        videoview_activity_close_button.setOnClickListener {
            finish()
        }
        //ブラウザで開くボタン
        videoview_activity_open_browser.setOnClickListener {
            lunchBrowser(videoLink)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == MediaSave.requestCode) {
                val uri = data?.data
                if (uri != null) {
                    mediaSave.saveVideo(this, videoLink, uri)
                }
            }
        }
    }

    private fun lunchBrowser(url: String?) {
        //CustomTab起動
        val builder = CustomTabsIntent.Builder()
        builder.setShowTitle(true)
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(this, Uri.parse(url))
    }
}
