package io.github.takusan23.tweetreader


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_image_view.*
import kotlinx.android.synthetic.main.fragment_image_view.*
import kotlin.concurrent.thread

/**
 * A simple [Fragment] subclass.
 */
class ImageViewFragment : Fragment() {

    val mediaSave: MediaSave = MediaSave()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val url = arguments?.getString("link")

        Glide.with(imageview_fragment_imageview)
            .load(url)
            .into(imageview_fragment_imageview)

        //保存ボタン
        (activity as ImageViewActivity).imageview_activity_save_button.setOnClickListener {
            //画像保存
            savePhoto()
        }

        (activity as ImageViewActivity).imageview_activity_open_browser.setOnClickListener {
            lunchBrowser(url)
        }

    }


    fun savePhoto() {
        //インターネット接続するので非同期処理
        thread {
            val url = arguments?.getString("link")
            (activity as ImageViewActivity).saveBitmap =
                Glide.with(imageview_fragment_imageview)
                    .asBitmap()
                    .load(url)
                    .submit()
                    .get()
            //保存
            activity?.runOnUiThread {
                //UIスレッド
                //画像保存
                mediaSave.createFile(
                    (activity as AppCompatActivity),
                    (activity as ImageViewActivity).statusText + "(" + (activity as ImageViewActivity).pos + ").jpg"
                )
                //画像取得
                (activity as ImageViewActivity).saveLink = url ?: ""
            }
        }
    }

    private fun lunchBrowser(url: String?) {
        //CustomTab起動
        val builder = CustomTabsIntent.Builder()
        builder.setShowTitle(true)
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(context, Uri.parse(url))

    }


}
