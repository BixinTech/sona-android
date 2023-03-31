package cn.bixin.sona.demo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.bixin.sona.demo.R
import cn.bixin.sona.demo.model.SeatInfoModel
import cn.bixin.sona.demo.util.AvatarUtil
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.chatroom_item_msg_text.view.ivAvatar
import kotlinx.android.synthetic.main.chatroom_item_reward_user.view.*

class RewardUserAdapter(private val context: Context, private val list: List<SeatInfoModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var selectedIndex = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return RewardUserViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.chatroom_item_reward_user, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is RewardUserViewHolder) {
            val url = AvatarUtil.getUserAvatar(list[position].uid ?: "")
            Glide.with(context).load(url).circleCrop().into(holder.itemView.ivAvatar)
            holder.itemView.tvSeatIndex.text = "${list[position].index + 1}麦"
            if (selectedIndex == position) {
                holder.itemView.ivAvatarFrame.visibility = View.VISIBLE
            } else {
                holder.itemView.ivAvatarFrame.visibility = View.GONE
            }

            holder.itemView.guestRoot.setOnClickListener {
                selectedIndex = if (selectedIndex == position) {
                    -1
                } else {
                    position
                }
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun selectUid(): String {
        return list.elementAtOrNull(selectedIndex)?.uid ?: ""
    }

    inner class RewardUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}