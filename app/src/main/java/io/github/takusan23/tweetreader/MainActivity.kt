package io.github.takusan23.tweetreader

import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import io.github.takusan23.tweetreader.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var pref_setting: SharedPreferences
    lateinit var helper: AccountsSQLiteHelper
    lateinit var db: SQLiteDatabase

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // このアプリは終了しました
        // Twitter API が利用できなくなったためです
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.shutdown_dialog_title)
            .setMessage(R.string.shutdown_dialog_message)
            .setPositiveButton(R.string.shutdown_dialog_close) { _, _ -> }
            .show()

        //データベース用意
        helper = AccountsSQLiteHelper(this)
        db = helper.writableDatabase
        db.disableWriteAheadLogging()


        pref_setting = PreferenceManager.getDefaultSharedPreferences(this)

        // もうログインできない
        /*
                if (pref_setting.getString("token", null) == null) {
                    //ログウイン画面に飛ばす
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }
        */

        //アカウント追加の共有を受けとる
        val intentAction = intent.action
        if (intentAction.equals(Intent.ACTION_SEND)) {
            val bundle = intent.extras
            if (bundle != null) {
                val url = bundle.getCharSequence(Intent.EXTRA_TITLE)
                //正規表現でとりだす
                val id = url.toString().replace("@[0-9a-zA-Z_]".toRegex(), "")
                val addAccountBottomFragment = AddAccountBottomFragment()
                val argment = Bundle()
                argment.putString("id", id)
                addAccountBottomFragment.arguments = argment
                addAccountBottomFragment.show(supportFragmentManager, "add_account")
            }
        }

        //追加ボタン
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            val addAccountBottomFragment = AddAccountBottomFragment()
            addAccountBottomFragment.show(supportFragmentManager, "add_account")
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        //最後に開いたアカウントを表示する
        setLastOpenFragment()
        //メニュー
        setAccountList()

    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    // 左上のメニューで選んだやつ
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.menu_setting -> {

            }

            R.id.menu_login -> {
                //ログイン画面を開く
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }

            R.id.menu_license -> {
                //ライセンス画面
                val intent = Intent(this, LicenseActivity::class.java)
                startActivity(intent)
            }

            R.id.menu_sourcecode -> {
                //ChromeCustomTab起動
                val url = "https://github.com/takusan23/TweetReader"
                val builder = CustomTabsIntent.Builder()
                builder.setShowTitle(true)
                val customTabsIntent = builder.build()
                customTabsIntent.launchUrl(this@MainActivity, Uri.parse(url))
            }

            R.id.lunch_app -> {
                //Twitterアプリで開く
                val fragment = supportFragmentManager.findFragmentById(R.id.activity_fragment)
                if (fragment is StatusFragment) {
                    val url = "https://twitter.com/${fragment.user.screenName}"
                    val builder = CustomTabsIntent.Builder()
                    builder.setShowTitle(true)
                    val customTabsIntent = builder.build()
                    customTabsIntent.launchUrl(this@MainActivity, Uri.parse(url))
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.drawer_sort -> {
                //一覧
                val fragment = AccountListFragment()
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.activity_fragment, fragment)
                transaction.commit()
            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun setLastOpenFragment() {
        val last_open = pref_setting.getString("last_open", null)
        if (last_open != null) {
            val bundle = Bundle()
            bundle.putString("id", last_open)
            val fragment = StatusFragment()
            fragment.arguments = bundle
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.activity_fragment, fragment)
            transaction.commit()
        }
    }

    /**
     * ドロワー再読込
     * */
    fun setAccountList() {
        //メニュー入れる
        val cursor = db.query(
            "account_db",
            arrayOf("user_id", "name"),
            null,
            null,
            null,
            null,
            null
        )
        binding.navView.menu.clear()
        binding.navView.inflateMenu(R.menu.activity_main_drawer)
        cursor.moveToFirst()
        for (i in 0 until cursor.count) {
            val user_id = cursor.getString(0)
            val name = cursor.getString(1)
            binding.navView.menu.add(name).setIcon(R.drawable.ic_account_circle_black_24dp)
                .setOnMenuItemClickListener {
                    val bundle = Bundle()
                    bundle.putString("id", user_id)
                    val fragment = StatusFragment()
                    fragment.arguments = bundle
                    val transaction = supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.activity_fragment, fragment)
                    transaction.commit()
                    //保存する
                    val editor = pref_setting.edit()
                    editor.putString("last_open", user_id)
                    editor.apply()
                    false
                }
            cursor.moveToNext()
        }
        //閉じる
        cursor.close()
    }

}
