package io.github.takusan23.tweetreader

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.add_account_bottom_fragment.*
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken

class AddAccountBottomFragment : BottomSheetDialogFragment() {

    lateinit var helper: AccountsSQLiteHelper
    lateinit var db: SQLiteDatabase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.add_account_bottom_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //データベース用意
        helper = AccountsSQLiteHelper(context!!)
        db = helper.writableDatabase
        db.disableWriteAheadLogging()

        account_id_string_textinputedittext.setText(arguments?.getString("id"))

        //追加ボタン
        add_account_button.setOnClickListener {
            getAccountID()
        }
    }

    fun getAccountID() {
        //認証
        val pref_setting = PreferenceManager.getDefaultSharedPreferences(context)
        val token = pref_setting.getString("token", null)
        val token_secret = pref_setting.getString("token_secret", null)
        val consumer_key = pref_setting.getString("consumer_key", null)
        val consumer_secret = pref_setting.getString("consumer_secret", null)
        //アカウント情報があるかチェック
        if (token != null) {
            //twitterオブジェクトの作成
            val tw = TwitterFactory().instance
            //AccessTokenオブジェクトの作成
            val at = AccessToken(token, token_secret)
            //Consumer keyとConsumer key secretの設定
            tw.setOAuthConsumer(consumer_key, consumer_secret)
            //AccessTokenオブジェクトを設定
            tw.oAuthAccessToken = at

            //InoutLayoutの中身取得
            val id_string = account_id_string_textinputedittext.text.toString()

            //非同期処理
            object : AsyncTask<Void, Void, Void>() {
                override fun doInBackground(vararg p0: Void?): Void? {
                    try{
                        val user = tw.showUser(id_string)
                        //アカウントがあるかチェック
                        if (user.screenName != null) {
                            //保存
                            val contentValues = ContentValues()
                            contentValues.put("name", user.name)
                            contentValues.put("user_id", user.id)
                            contentValues.put("setting", "") //将来使うかも
                            db.insert("account_db", null, contentValues)
                            //閉じる
                            add_account_button.post {
                                dismiss()
                                Toast.makeText(context, getString(R.string.add_account_ok), Toast.LENGTH_SHORT).show()
                                //ドロワー再読込
                                (activity as MainActivity).setAccountList()
                            }
                        } else {
                            add_account_button.post {
                                Toast.makeText(context, getString(R.string.account_not_found), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }catch (e: TwitterException){
                        add_account_button.post {
                            Toast.makeText(context, getString(R.string.error), Toast.LENGTH_SHORT).show()
                        }
                    }
                    return null
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }
    }
}