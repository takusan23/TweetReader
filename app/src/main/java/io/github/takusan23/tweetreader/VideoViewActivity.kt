package io.github.takusan23.tweetreader

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import io.github.takusan23.tweetreader.databinding.ActivityVideoViewBinding

class VideoViewActivity : AppCompatActivity() {

    val mediaSave = MediaSave()
    var videoLink = ""

    private var _binding: ActivityVideoViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityVideoViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        //Uri
        videoLink = intent.getStringExtra("url") ?: ""
        // videoLink = "https://developer.android.com/images/home/android-p-clear-bg-with-shadow.png?hl=JA"
        binding.videoviewActivityVideoview.setVideoURI(videoLink.toUri())
        binding.videoviewActivityVideoview.setMediaController(MediaController(this))
        //再生可能になったら再生
        binding.videoviewActivityVideoview.setOnPreparedListener {
            binding.videoviewActivityVideoview.start()
        }

        val statusText = intent.getStringExtra("status") ?: "test"

        //保存機能？
        binding.videoviewActivitySaveButton.setOnClickListener {
            mediaSave.createFile(this, "$statusText.mp4")
        }
        //閉じるボタン
        binding.videoviewActivityCloseButton.setOnClickListener {
            finish()
        }
        //ブラウザで開くボタン
        binding.videoviewActivityOpenBrowser.setOnClickListener {
            lunchBrowser(videoLink)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
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
