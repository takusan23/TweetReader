package io.github.takusan23.tweetreader

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import io.github.takusan23.tweetreader.databinding.ActivityLoginBinding
import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken
import twitter4j.auth.RequestToken
import twitter4j.conf.ConfigurationBuilder

class LoginActivity : AppCompatActivity() {

    var consumerKey = ""
    var consumerSecret = ""
    lateinit var twitterFactory: TwitterFactory
    lateinit var twitter: Twitter
    lateinit var request_token: RequestToken

    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //コンシューマーキー（別ファイルにあります。バージョン管理システムから外せるように）
        consumerKey = getString(R.string.consumer_key)
        consumerSecret = getString(R.string.consumer_secret)

        //自分のコンシューマーキーを利用する設定有効時
        binding.myConsumerKey.setOnFocusChangeListener { view, b ->
            if (b) {
                //有効
                binding.consumerKeyLayout.visibility = View.VISIBLE
            } else {
                //無効
                binding.consumerKeyLayout.visibility = View.INVISIBLE
            }
        }

        //ログイン画面
        getLoginScreen()
        //PINいれてアクセストークン取得
        getAccessToken()
    }

    /*ログイン画面*/
    private fun getLoginScreen() {
        binding.lunchTwitterButton.setOnClickListener {
            //自分のコンシューマーキーを利用する設定有効時
            if (binding.myConsumerKey.isChecked) {
                consumerKey = binding.consumerKeyTextinput.text.toString()
                consumerSecret = binding.consumerSecretTextinput.text.toString()
            }
            val cb = ConfigurationBuilder()
            cb.setDebugEnabled(true)
                .setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret)
                .setOAuthAccessToken(null)
                .setOAuthAccessTokenSecret(null)

            twitterFactory = TwitterFactory(cb.build())
            twitter = twitterFactory.getInstance()

            object : AsyncTask<Void, Void, Void>() {
                override fun doInBackground(vararg aVoid: Void): Void? {
                    try {
                        request_token = twitter.getOAuthRequestToken()
                        val url = request_token.getAuthenticationURL()
                        //ChromeCustomTab起動
                        val builder = CustomTabsIntent.Builder()
                        builder.setShowTitle(true)
                        val customTabsIntent = builder.build()
                        customTabsIntent.launchUrl(this@LoginActivity, Uri.parse(url))
                    } catch (e: TwitterException) {
                        e.printStackTrace()
                    }
                    return null
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

        }
    }

    /*アクセストークン取得*/
    private fun getAccessToken() {
        binding.loginButton.setOnClickListener(View.OnClickListener {
            object : AsyncTask<Void, Void, Void>() {
                @SuppressLint("WrongThread")
                override fun doInBackground(vararg aVoid: Void): Void? {
                    var accessToken: AccessToken? = null
                    try {
                        val pin = binding.loginNumberTextinput.getText().toString()
                        if (pin.length > 0) {
                            accessToken = twitter.getOAuthAccessToken(pin)
                        } else {
                            accessToken = twitter.oAuthAccessToken
                        }
                        //できた！
                        val finalAccessToken = accessToken
                        val token = finalAccessToken!!.token
                        val secret = finalAccessToken.tokenSecret
                        //SharedPreferenceに保存
                        val pref_setting =
                            androidx.preference.PreferenceManager.getDefaultSharedPreferences(this@LoginActivity)
                        val editor = pref_setting.edit()
                        //consumerKeyも保存？
                        editor.putString("consumer_key", consumerKey)
                        editor.putString("consumer_secret", consumerSecret)
                        editor.putString("token", token)
                        editor.putString("token_secret", secret)
                        editor.apply()
                    } catch (e: TwitterException) {
                        e.printStackTrace()
                    }
                    return null
                }

                override fun onPostExecute(result: Void?) {
                    super.onPostExecute(result)
                    //画面を戻す
                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.login_success),
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        })
    }


}

