package io.github.takusan23.tweetreader

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import java.util.ArrayList

class ImageViewFragmentPagerAdapter(
    fragmentManager: FragmentManager,
    val arrayList: ArrayList<String>
) : FragmentPagerAdapter(
    fragmentManager,
    BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
) {
    override fun getItem(position: Int): Fragment {
        val bundle = Bundle()
        bundle.putString("link", arrayList[position])
        val imageViewFragment = ImageViewFragment()
        imageViewFragment.arguments = bundle
        return imageViewFragment
    }

    override fun getCount(): Int {
        return arrayList.size
    }

}