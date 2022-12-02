package cn.bixin.sona.demo.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import cn.bixin.sona.demo.ChatRoomActivity
import cn.bixin.sona.demo.R
import cn.bixin.sona.demo.model.ChatRoomListModel
import kotlinx.android.synthetic.main.chatroom_item_room_list.view.*

class ChatRoomListAdapter(private val context: Context, private val list: List<ChatRoomListModel>) :
    RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ChatRoomListViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.chatroom_item_room_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is ChatRoomListViewHolder) {
            holder.itemView.tvTitle.text = list[position].name
            holder.itemView.tvDescription.text = list[position].roomId

            holder.itemView.clRoomList.setOnClickListener {
                context.startActivity(Intent(context, ChatRoomActivity::class.java).apply {
                    putExtra(ChatRoomActivity.ROOM_ID, list[position].roomId)
                })
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ChatRoomListViewHolder(itemView: View) : ViewHolder(itemView)

}