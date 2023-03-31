package cn.bixin.sona.demo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.bixin.sona.demo.R
import cn.bixin.sona.demo.msg.BaseChatRoomMsg
import cn.bixin.sona.demo.msg.ChatRoomGiftRewardTipsMsg
import cn.bixin.sona.demo.msg.ChatRoomTextMsg
import cn.bixin.sona.demo.util.AvatarUtil
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.chatroom_item_gift_reward_tips.view.*
import kotlinx.android.synthetic.main.chatroom_item_msg_text.view.*

class RoomMsgAdapter(private val context: Context, private val list: List<BaseChatRoomMsg>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        val TYPE_TEXT = 1
        val TYPE_REWARD = 2
    }

    override fun getItemViewType(position: Int): Int {
        if (list[position] is ChatRoomTextMsg) {
            return TYPE_TEXT
        } else if (list[position] is ChatRoomGiftRewardTipsMsg) {
            return TYPE_REWARD
        }
        return 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_TEXT) {
            TextViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.chatroom_item_msg_text, parent, false)
            )
        } else {
            GiftRewardTipsViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.chatroom_item_gift_reward_tips, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TextViewHolder) {
            holder.itemView.tvName.text = "用户${(list[position] as? ChatRoomTextMsg)?.uid}"
            holder.itemView.tvContent.text = (list[position] as? ChatRoomTextMsg)?.content
            val url = AvatarUtil.getUserAvatar((list[position] as? ChatRoomTextMsg)?.uid ?: "")
            Glide.with(context).load(url).circleCrop().into(holder.itemView.ivAvatar)
        } else if (holder is GiftRewardTipsViewHolder) {
            val msg = list[position] as? ChatRoomGiftRewardTipsMsg
            holder.itemView.tvRewardTips.text = "用户${msg?.fromUid} 打赏了 用户${msg?.toUid} ${msg?.giftName} x1"
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    inner class GiftRewardTipsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}