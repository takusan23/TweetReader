package io.github.takusan23.tweetreader

import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import io.github.takusan23.tweetreader.databinding.StatusFragmentBinding
import twitter4j.Paging
import twitter4j.Status
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.User
import twitter4j.auth.AccessToken
import kotlin.concurrent.thread


class StatusFragment : Fragment() {

    lateinit var pref_setting: SharedPreferences
    lateinit var status_list: ArrayList<Status>
    lateinit var statusRecyclerViewAdapter: StatusRecyclerViewAdapter
    lateinit var recyclerViewLayoutManager: RecyclerView.LayoutManager
    lateinit var snackbarProgress: SnackbarProgress
    var isMediaOnly = false

    //追加読み込み制御用
    var isMoreLoading = false

    //最後のID
    var lastId = 0L

    //ゆーざー
    lateinit var user: User

    private var _binding: StatusFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = StatusFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pref_setting = PreferenceManager.getDefaultSharedPreferences(context)

        //ステータスの配列
        status_list = arrayListOf()
        snackbarProgress = SnackbarProgress()


        //ここから下三行必須
        binding.recyclerView.setHasFixedSize(true)
        val mLayoutManager = LinearLayoutManager(context)
        binding.recyclerView.setLayoutManager(mLayoutManager)
        statusRecyclerViewAdapter = StatusRecyclerViewAdapter(status_list)
        binding.recyclerView.setAdapter(statusRecyclerViewAdapter)
        recyclerViewLayoutManager = binding.recyclerView.getLayoutManager()!!
        //ステータス取得
        getUserStatus(isMediaOnly, null)

        //どこでもスワイプでドロワー開く
        addNavigationOpen()

        //スワイプでリロード
        binding.swipeRefresh.setOnRefreshListener {
            getUserStatus(isMediaOnly, null)
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }

            override fun onTabSelected(p0: TabLayout.Tab?) {
                //本当はtagで分けるつもりだった
                when (p0?.text) {
                    getString(R.string.all_tweet) -> {
                        isMediaOnly = false
                        getUserStatus(isMediaOnly, null)
                    }

                    getString(R.string.Media) -> {
                        isMediaOnly = true
                        getUserStatus(isMediaOnly, null)
                    }
                }
            }
        })

        //タイトルをユーザー名に
        getUser()

        //追加読み込み
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val firstVisibleItem =
                    (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                val visibleItemCount =
                    (recyclerView.layoutManager as LinearLayoutManager).childCount
                val totalItemCount = (recyclerView.layoutManager as LinearLayoutManager).itemCount
                //最後までスクロールしたときの処理
                if (firstVisibleItem + visibleItemCount == totalItemCount && !isMoreLoading) {
                    // isMoreLoadingで連続で呼ばれるのを対策している
                    isMoreLoading = true
                    //追加読み込み
                    getUserStatus(isMediaOnly, lastId)
                }
            }
        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun getUser() {
        val pref_setting = PreferenceManager.getDefaultSharedPreferences(context)
        val token = pref_setting.getString("token", null)
        val token_secret = pref_setting.getString("token_secret", null)
        val consumer_key = pref_setting.getString("consumer_key", null)
        val consumer_secret = pref_setting.getString("consumer_secret", null)
        val id = arguments?.getString("id", "")
        //アカウント情報があるかチェック
        if (token != null) {
            //あった
            //twitterオブジェクトの作成
            val tw = TwitterFactory().instance
            //AccessTokenオブジェクトの作成
            val at = AccessToken(token, token_secret)
            //Consumer keyとConsumer key secretの設定
            tw.setOAuthConsumer(consumer_key, consumer_secret)
            //AccessTokenオブジェクトを設定
            tw.oAuthAccessToken = at

            //非同期処理
            thread {
                try {
                    val user = tw.showUser(id?.toLong() ?: 0)
                    //タイトルバーに入れる
                    activity?.runOnUiThread {
                        //UIスレッド
                        (activity as AppCompatActivity).supportActionBar?.title = user.name
                        (activity as AppCompatActivity).supportActionBar?.subtitle =
                            "@" + user.screenName
                    }
                } catch (e: TwitterException) {
                    activity?.runOnUiThread {
                        if (e.isCausedByNetworkIssue) {
                            Toast.makeText(
                                context,
                                getString(R.string.network_error),
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                getString(R.string.error),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }

        }
    }

    /**
     * @param mediaOnly 画像のみ
     * @param maxId 追加読み込み用。追加読込しないときはnull入れていいよ
     * */
    fun getUserStatus(mediaOnly: Boolean, maxId: Long?) {

        //くるくる
        if (context != null) {
            snackbarProgress.showSnackbarProgress(context!!, binding.recyclerView)
        }

        if (maxId == null) {
            //リスト消す:q

            status_list.clear()
            statusRecyclerViewAdapter.notifyDataSetChanged()
        }

        val pref_setting = PreferenceManager.getDefaultSharedPreferences(context)
        val token = pref_setting.getString("token", null)
        val token_secret = pref_setting.getString("token_secret", null)
        val consumer_key = pref_setting.getString("consumer_key", null)
        val consumer_secret = pref_setting.getString("consumer_secret", null)
        //アカウント情報があるかチェック
        if (token != null) {
            //あった
            //twitterオブジェクトの作成
            val tw = TwitterFactory().instance
            //AccessTokenオブジェクトの作成
            val at = AccessToken(token, token_secret)
            //Consumer keyとConsumer key secretの設定
            tw.setOAuthConsumer(consumer_key, consumer_secret)
            //AccessTokenオブジェクトを設定
            tw.oAuthAccessToken = at

            object : AsyncTask<Void, Void, Void>() {
                override fun doInBackground(vararg p0: Void?): Void? {
                    // 通信
                    try {
                        val id = arguments?.getString("id", "")
                        val paging = Paging()
                        paging.count = 200
                        //追加読み込み
                        if (maxId != null) {
                            paging.maxId = maxId
                        }
                        //取得
                        val homeTl = tw.timelines().getUserTimeline(id?.toLong()!!, paging)

                        //アカウント入れとく
                        user = homeTl[0].user

                        //一個ずつ
                        for (status in homeTl) {
                            if (mediaOnly) {
                                //メディア付きのみ
                                if (status.mediaEntities.size > 0) {
                                    status_list.add(status)
                                }
                            } else {
                                status_list.add(status)
                            }
                        }
                        //追加読み込み用に
                        lastId = homeTl[homeTl.size - 1].id
                    } catch (e: TwitterException) {
                        e.printStackTrace()
                        activity?.runOnUiThread {
                            if (e.isCausedByNetworkIssue) {
                                Toast.makeText(
                                    context,
                                    getString(R.string.network_error),
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    getString(R.string.error),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                    return null;
                }

                override fun onPostExecute(result: Void?) {
                    super.onPostExecute(result)
                    //UIスレッド
                    statusRecyclerViewAdapter.notifyDataSetChanged()
                    binding.swipeRefresh?.isRefreshing = false
                    snackbarProgress.dissmiss()
                    isMoreLoading = false
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }
    }

    /**
     * Android 10の新しいジェスチャーで戻るジェスチャーとドロワー開くジェスチャーをかぶらないようにする
     * 端からスワイプ以外でも動作するようにする
     */
    private fun addNavigationOpen() {
        //すたーと
        val start = floatArrayOf(0f)
        val end = floatArrayOf(0f)
        val y_start = floatArrayOf(0f)
        val y_end = floatArrayOf(0f)
        binding.recyclerView?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    start[0] = event.x
                    y_start[0] = event.y
                }

                MotionEvent.ACTION_UP -> {
                    end[0] = event.x
                    y_end[0] = event.y
                    //System.out.println("end : " + y_end[0]);
                    //System.out.println("final : " + (y_start[0] - y_end[0]));
                    //両方揃ったら比較開始
                    if (start[0] != end[0]) {
                        //なんとなく400以上の誤差がないとうごかないように　と　縦スクロールが大きいと動作しないようにする（100から-100までのみ）
                        if (end[0] - start[0] > 400 && y_start[0] - y_end[0] < 100 && y_start[0] - y_end[0] > -100) {
                            //ドロワー開く。getActivity()あってよかた
                            val drawer =
                                activity?.findViewById<View>(R.id.drawer_layout) as DrawerLayout
                            drawer?.openDrawer(Gravity.LEFT)
                        }
                    }
                }
            }
            false
        }
    }
}