package io.github.takusan23.tweetreader

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import java.net.HttpURLConnection.HTTP_OK
import java.io.BufferedInputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread
import androidx.core.content.ContextCompat.startActivity


class MediaSave {

    companion object {
        val requestCode = 845
    }


    fun createFile(activity: AppCompatActivity, fileName: String) {
        //Storage Access Framework で画像を保存する
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.type = "*/*";
        intent.putExtra(Intent.EXTRA_TITLE, "$fileName");
        activity.startActivityForResult(intent, requestCode);
    }

    //保存
    fun savePhoto(activity: AppCompatActivity, uri: Uri, bitmap: Bitmap) {
        try {
            //保存する
            val outputStream = activity.contentResolver.openOutputStream(uri)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun saveVideo(activity: AppCompatActivity, address: String, uri: Uri) {
        //Snackbar
        val view = activity.findViewById<View>(android.R.id.content)
        val snackbar = Snackbar.make(
            view,
            activity.getString(R.string.download_progress),
            Snackbar.LENGTH_INDEFINITE
        )
        snackbar.show()
        //動画ダウンロード
        thread {
            var urlConnection: HttpURLConnection? = null
            try {
                val url = URL(address)

                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.readTimeout = 10000
                urlConnection.connectTimeout = 20000
                urlConnection.requestMethod = "GET"
                urlConnection.instanceFollowRedirects = false
                urlConnection.setRequestProperty("Accept-Language", "jp")

                // 接続
                urlConnection.connect()

                if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                    //成功時
                    //保存する
                    val inputStream = urlConnection.inputStream
                    val outputStream = activity.contentResolver.openOutputStream(uri)
                    val buffer = ByteArray(1024)
                    try {
                        while (true) {
                            //いれていく
                            val data = inputStream.read(buffer)
                            if (data == -1) {
                                //Snackbarにだす
                                activity.runOnUiThread {
                                    val snackbar = Snackbar.make(
                                        view,
                                        activity.getString(R.string.video_download_complete),
                                        Snackbar.LENGTH_SHORT
                                    )
                                    snackbar.show()
                                }
                                break
                            }
                            outputStream?.write(buffer, 0, data)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    inputStream.close()
                    outputStream?.close()
                } else {
                    activity.runOnUiThread {
                        //問題発生
                        snackbar.dismiss()
                        Toast.makeText(
                            activity,
                            activity.getString(R.string.error),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                activity.runOnUiThread {
                    //問題発生
                    snackbar.dismiss()
                    Toast.makeText(
                        activity,
                        activity.getString(R.string.error),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            } finally {
                urlConnection?.disconnect()
            }
        }

    }
}