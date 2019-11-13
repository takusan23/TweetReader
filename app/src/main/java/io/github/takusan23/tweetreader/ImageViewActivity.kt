package io.github.takusan23.tweetreader

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_image_view.*
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.widget.Toast
import java.io.InputStream
import java.net.URL


class ImageViewActivity : AppCompatActivity() {

    lateinit var saveBitmap: Bitmap
    lateinit var saveLink: String
    val mediaSave: MediaSave = MediaSave()

    var statusText = ""
    var pos = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)

        supportActionBar?.hide()

        val imageLinkList = intent.getStringArrayListExtra("images")
        statusText = intent.getStringExtra("status") ?: "test"
        pos = intent.getIntExtra("pos", 0)

        //アニメーションに対応できるように
        imageview_activity_view_pager.transitionName = "photo"

        //ViewPagerによる切り替え機能
        val imageViewFragmentPagerAdapter =
            ImageViewFragmentPagerAdapter(supportFragmentManager, imageLinkList)
        imageview_activity_view_pager.adapter = imageViewFragmentPagerAdapter

        //位置反映
        imageview_activity_view_pager.setCurrentItem(pos, true)

        //閉じるボタン
        imageview_activity_close_button.setOnClickListener {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == MediaSave.requestCode) {
                val uri = data?.data

                if (uri != null) {
                    //保存
                    mediaSave.savePhoto(this, uri, saveBitmap)
                    //保存しました！
                    runOnUiThread {
                        Toast.makeText(
                            this,
                            getString(R.string.save_image_message),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

}

