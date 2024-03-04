package io.github.takusan23.tweetreader

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.woxthebox.draglistview.DragListView
import io.github.takusan23.tweetreader.databinding.AccountListFragmentBinding

class AccountListFragment : Fragment() {

    private var helper: AccountsSQLiteHelper? = null
    private var db: SQLiteDatabase? = null
    private var arrayList: ArrayList<String>? = null
    private var nameStringArrayList: ArrayList<String>? = null
    private var dragListView: DragListView? = null

    private var _binding: AccountListFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = AccountListFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //SQLite
        if (helper == null) {
            helper = AccountsSQLiteHelper(context!!)
        }
        if (db == null) {
            db = helper!!.writableDatabase
            db!!.disableWriteAheadLogging()
        }

        //読み込み
        loadSQLite()
        //ListViewドラッグとか
        setDragListView()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * SQLite読み込み
     */
    private fun loadSQLite() {
        val testArrayList = ArrayList<Pair<Long, String>>()
        arrayList = ArrayList()
        nameStringArrayList = ArrayList()
        val cursor = db!!.query(
            "account_db",
            arrayOf("user_id", "name"), null, null, null, null, null
        )
        cursor.moveToFirst()
        for (i in 0 until cursor.count) {
            arrayList!!.add(cursor.getString(1))
            nameStringArrayList!!.add(cursor.getString(1))
            testArrayList.add(Pair(i.toLong(), cursor.getString(1)))
            cursor.moveToNext()
        }
        binding.dragListview.setLayoutManager(LinearLayoutManager(context))
        val listAdapter =
            ItemAdapter(testArrayList, R.layout.drag_list_item, R.id.image, false, activity!!)
        binding.dragListview.setAdapter(listAdapter, true)
        binding.dragListview.setCanDragHorizontally(false)
        cursor.close()
    }

    /**
     * DragListViewとか
     */
    private fun setDragListView() {
        binding.dragListview.setDragListListener(object : DragListView.DragListListener {
            override fun onItemDragStarted(position: Int) {
                //Toast.makeText(getContext(), "Start - position: " + position, Toast.LENGTH_SHORT).show();
            }

            override fun onItemDragging(itemPosition: Int, x: Float, y: Float) {

            }

            override fun onItemDragEnded(fromPosition: Int, toPosition: Int) {
                //Toast.makeText(getContext(), "End - position: " + toPosition + "\n" + "Start - position: " + fromPosition, Toast.LENGTH_SHORT).show();
                setSortMenu(fromPosition, toPosition)
            }
        })
    }


    /**
     * 新置き換えシステム
     * かいせんどんから持ってきた
     */
    private fun setSortMenu(start: Int, end: Int) {
        //startを一時保存アンド削除
        val start_item = nameStringArrayList!![start]
        nameStringArrayList!!.remove(start_item)
        //入れる
        nameStringArrayList!!.add(end, start_item)
        //一時的にSQLiteの内容を配列に入れる
        val name_List = ArrayList<String>()
        val value_List = ArrayList<String>()
        for (i in nameStringArrayList!!.indices) {
            //Step 1.name/valueを取得する
            name_List.add(nameStringArrayList!![i])
            value_List.add(getSQLiteDBValue(nameStringArrayList!![i])!!)
        }

        //Step 2.SQLite更新
        //最初にDB全クリアする
        db!!.delete("account_db", null, null)
        for (i in name_List.indices) {
            writeSQLiteDB(name_List[i], value_List[i], "")
        }
        //メニュー再読み込み
        (activity as MainActivity).setAccountList()
    }

    /**
     * SQLiteから指定した名前の値を返します
     */
    private fun getSQLiteDBValue(name: String): String? {
        var value: String? = null
        val cursor = db!!.query(
            "account_db",
            arrayOf("user_id", "name"),
            "name=?",
            arrayOf(name), null, null, null
        )
        if (cursor != null && cursor.moveToFirst()) {
            value = cursor.getString(0)
            cursor.close()
        }
        return value
    }

    /**
     * SQLite書き込む
     */
    private fun writeSQLiteDB(name: String, user_id: String, value: String) {
        //入れる
        val values = ContentValues()
        values.put("name", name)
        values.put("user_id", user_id)
        values.put("setting", value)
        db!!.insert("account_db", "", values)
    }


}