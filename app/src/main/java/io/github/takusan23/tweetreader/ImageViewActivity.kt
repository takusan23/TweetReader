package io.github.takusan23.tweetreader

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.github.takusan23.tweetreader.databinding.ActivityImageViewBinding

class ImageViewActivity : AppCompatActivity() {

    lateinit var saveBitmap: Bitmap
    lateinit var saveLink: String
    val mediaSave: MediaSave = MediaSave()

    var statusText = ""
    var pos = 0

    private var _binding: ActivityImageViewBinding? = null
    val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityImageViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val imageLinkList = intent.getStringArrayListExtra("images")!!
        statusText = intent.getStringExtra("status") ?: "test"
        pos = intent.getIntExtra("pos", 0)

        //アニメーションに対応できるように
        binding.imageviewActivityViewPager.transitionName = "photo"

        //ViewPagerによる切り替え機能
        val imageViewFragmentPagerAdapter =
            ImageViewFragmentPagerAdapter(supportFragmentManager, imageLinkList)
        binding.imageviewActivityViewPager.adapter = imageViewFragmentPagerAdapter

        //位置反映
        binding.imageviewActivityViewPager.setCurrentItem(pos, true)

        //閉じるボタン
        binding.imageviewActivityCloseButton.setOnClickListener {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
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

