package io.github.takusan23.tweetreader

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar

class SnackbarProgress() {
    lateinit var snackbar: Snackbar
    fun showSnackbarProgress(context: Context, view: View) {
        snackbar = Snackbar.make(view, context.getString(R.string.loading), Snackbar.LENGTH_INDEFINITE)
        val snackBer_viewGrop = snackbar.getView().findViewById<TextView>(R.id.snackbar_text).getParent() as ViewGroup
        //SnackBerを複数行対応させる
        val snackBer_textView = snackBer_viewGrop.findViewById(R.id.snackbar_text) as TextView
        snackBer_textView.maxLines = 2
        //複数行対応させたおかげでずれたので修正
        val progressBar = ProgressBar(context)
        val progressBer_layoutParams =
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        progressBer_layoutParams.gravity = Gravity.CENTER
        progressBar.layoutParams = progressBer_layoutParams
        snackBer_viewGrop.addView(progressBar, 0)
        snackbar.show()
    }

    fun dissmiss() {
        if (snackbar != null) {
            snackbar.dismiss()
        }
    }

}