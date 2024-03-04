package io.github.takusan23.tweetreader

import android.app.Activity
import android.database.sqlite.SQLiteDatabase
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.util.Pair
import com.google.android.material.snackbar.Snackbar
import com.woxthebox.draglistview.DragItemAdapter

internal class ItemAdapter(
    list: ArrayList<Pair<Long, String>>,
    private val mLayoutId: Int,
    private val mGrabHandleId: Int,
    private val mDragOnLongPress: Boolean,
    private val activity: Activity
) : DragItemAdapter<Pair<Long, String>, ItemAdapter.ViewHolder>() {


    private var helper: AccountsSQLiteHelper? = null
    private var db: SQLiteDatabase? = null

    init {
        itemList = list
    }

    internal inner class ViewHolder(itemView: View) :
        DragItemAdapter.ViewHolder(itemView, mGrabHandleId, mDragOnLongPress) {
        var mText: TextView
        var deleteText: TextView

        init {
            mText = itemView.findViewById<View>(R.id.text) as TextView
            deleteText = itemView.findViewById<View>(R.id.drag_item_delete) as TextView
        }

        override fun onItemClicked(view: View?) {
            //Toast.makeText(view.getContext(), "Item clicked", Toast.LENGTH_SHORT).show();
        }

        override fun onItemLongClicked(view: View?): Boolean {
            //Toast.makeText(view.getContext(), "Item long clicked", Toast.LENGTH_SHORT).show();
            return true
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(mLayoutId, parent, false)
        return ViewHolder(view)
    }

    override fun getUniqueItemId(position: Int): Long {
        return mItemList[position].first!!
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        //テキスト設定
        val text = mItemList[position].second
        holder.mText.text = text
        holder.itemView.tag = mItemList[position]

        //データベース用意
        if (helper == null) {
            helper = AccountsSQLiteHelper(holder.mText.context)
        }
        if (db == null) {
            db = helper!!.writableDatabase
            db!!.disableWriteAheadLogging()
        }

        //削除ボタン
        holder.deleteText.setOnClickListener {
            //確認Snackbar
            Snackbar.make(holder.mText, activity.getString(R.string.delete_message), Snackbar.LENGTH_LONG).setAction(activity.getString(R.string.delete), View.OnClickListener {
                //押した時
                db?.delete("account_db", "name=?", arrayOf(text))
                //再読込
                (activity as MainActivity).setAccountList()
                //一覧
                val fragment = AccountListFragment()
                val transaction = activity.supportFragmentManager.beginTransaction()
                transaction.replace(R.id.activity_fragment, fragment)
                transaction.commit()
                //削除したよ！
                Toast.makeText(activity,activity.getString(R.string.delete_ok),Toast.LENGTH_SHORT).show()
            }).show()
        }
    }
}