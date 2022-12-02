package cn.bixin.sona.demo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.bixin.sona.demo.R
import cn.bixin.sona.demo.model.GiftListModel
import cn.bixin.sona.demo.util.ScreenUtil
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.chatroom_item_reward_gift.view.*

class RewardGiftAdapter(private val context: Context, private val list: List<GiftListModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var selectedIndex = 0

    private val mItemWidth by lazy {
        ScreenUtil.getScreenWidth(context) / 4
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return RewardGiftViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.chatroom_item_reward_gift, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is RewardGiftViewHolder) {
            holder.itemView.clGift.layoutParams?.let {
                it.width = mItemWidth
                it.height = ScreenUtil.dp2px(context, 100f)
            }
            holder.itemView.ivGift.setImageResource(R.mipmap.chatroom_img_gift_1)

            holder.itemView.tvGiftName.text = list[position].giftName
            holder.itemView.tvGiftPrice.text = "${list[position].price}"

            holder.itemView.clGift.isSelected = selectedIndex == position

            holder.itemView.clGift.setOnClickListener {
                selectedIndex = position
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class RewardGiftViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}