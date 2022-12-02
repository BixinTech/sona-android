package cn.bixin.sona.demo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import cn.bixin.sona.demo.R
import cn.bixin.sona.demo.model.UserManageModel
import kotlinx.android.synthetic.main.chatroom_item_user_manage.view.*

class ChatRoomUserManageAdapter(
    private val context: Context,
    private val list: List<UserManageModel>
) :
    RecyclerView.Adapter<ViewHolder>() {

    var onItemClick: ((position: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ChatRoomUserManageViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.chatroom_item_user_manage, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is ChatRoomUserManageViewHolder) {
            holder.itemView.tvName.text = list[position].name
            holder.itemView.ivIcon.setImageResource(list[position].res)
            holder.itemView.clRoomList.setOnClickListener {
                onItemClick?.invoke(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ChatRoomUserManageViewHolder(itemView: View) : ViewHolder(itemView)

}