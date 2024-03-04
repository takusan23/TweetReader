package io.github.takusan23.tweetreader

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import io.github.takusan23.tweetreader.databinding.FragmentImageViewBinding
import kotlin.concurrent.thread

/**
 * A simple [Fragment] subclass.
 */
class ImageViewFragment : Fragment() {

    val mediaSave: MediaSave = MediaSave()

    private var _binding: FragmentImageViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentImageViewBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val url = arguments?.getString("link")

        Glide.with(binding.imageviewFragmentImageview)
            .load(url)
            .into(binding.imageviewFragmentImageview)

        //保存ボタン
        (activity as ImageViewActivity).binding.imageviewActivitySaveButton.setOnClickListener {
            //画像保存
            savePhoto()
        }

        (activity as ImageViewActivity).binding.imageviewActivityOpenBrowser.setOnClickListener {
            lunchBrowser(url)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    fun savePhoto() {
        //インターネット接続するので非同期処理
        thread {
            val url = arguments?.getString("link")
            (activity as ImageViewActivity).saveBitmap =
                Glide.with(binding.imageviewFragmentImageview)
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
