package cn.bixin.sona.demo

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Rect
import android.view.View
import android.widget.EditText
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.bixin.sona.demo.adapter.ChatRoomListAdapter
import cn.bixin.sona.demo.base.BaseActivity
import cn.bixin.sona.demo.util.ScreenUtil
import cn.bixin.sona.demo.util.ToastUtil
import cn.bixin.sona.demo.viewmodel.RoomListViewModel
import kotlinx.android.synthetic.main.activity_chatroom_list.*

class ChatRoomListActivity : BaseActivity() {

    private val roomListViewModel by lazy {
        ViewModelProvider(this).get(RoomListViewModel::class.java)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_chatroom_list
    }

    override fun initView() {
        super.initView()
        rvRoomList.layoutManager = GridLayoutManager(this, 2)
        rvRoomList.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)
                val params = view.layoutParams as GridLayoutManager.LayoutParams
                val spanIndex = params.spanIndex
                if (spanIndex % 2 == 0) {
                    outRect.left = ScreenUtil.dp2px(this@ChatRoomListActivity, 10f)
                    outRect.right = ScreenUtil.dp2px(this@ChatRoomListActivity, 5f)
                } else {
                    outRect.left = ScreenUtil.dp2px(this@ChatRoomListActivity, 5f)
                    outRect.right = ScreenUtil.dp2px(this@ChatRoomListActivity, 10f)
                }
                outRect.bottom = ScreenUtil.dp2px(this@ChatRoomListActivity, 8f)
            }
        })
        roomListViewModel.roomListInfo.observe(this, Observer {
            if (it.isNullOrEmpty()) {
                ToastUtil.showToast(this@ChatRoomListActivity.applicationContext, "暂无数据")
                return@Observer
            }
            rvRoomList.adapter = ChatRoomListAdapter(this@ChatRoomListActivity, it)
        })
        roomListViewModel.getRoomList()

        tvCreateRoom.setOnClickListener {
            showInputDialog()
        }
    }

    private fun showInputDialog() {
        val editText = EditText(this)
        editText.hint = "请输入房间标题"
        val inputDialog: AlertDialog.Builder = AlertDialog.Builder(this)
        inputDialog.setTitle("创建房间").setView(editText)
        inputDialog.setPositiveButton(
            "确定"
        ) { _, _ ->
            if (editText.text.toString().isEmpty()) {
                ToastUtil.showToast(MainApplication.getContext(), "请输入房间标题")
                return@setPositiveButton
            }
            startActivity(Intent(this, ChatRoomActivity::class.java).apply {
                putExtra(ChatRoomActivity.ROOM_TITLE, editText.text.toString())
            })
        }.show()
    }
}