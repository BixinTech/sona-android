package cn.bixin.sona.demo.dialog

import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import cn.bixin.sona.demo.ChatRoomManager
import cn.bixin.sona.demo.MainApplication
import cn.bixin.sona.demo.R
import cn.bixin.sona.demo.adapter.RewardGiftAdapter
import cn.bixin.sona.demo.adapter.RewardUserAdapter
import cn.bixin.sona.demo.base.BaseDialogFragment
import cn.bixin.sona.demo.model.GiftListModel
import cn.bixin.sona.demo.model.SeatInfoModel
import cn.bixin.sona.demo.util.ToastUtil
import cn.bixin.sona.demo.viewmodel.GiftViewModel
import kotlinx.android.synthetic.main.chatroom_dialog_gift_reward.view.*

class GiftRewardDialog : BaseDialogFragment() {

    private val giftViewModel by lazy {
        ViewModelProvider(this).get(GiftViewModel::class.java)
    }

    private var rewardGiftAdapter: RewardGiftAdapter? = null
    private var giftList = ArrayList<GiftListModel>()
    private var seatList: ArrayList<SeatInfoModel>? = null

    companion object {
        fun newInstance(seatList: ArrayList<SeatInfoModel>?): GiftRewardDialog {
            val dialog = GiftRewardDialog()
            seatList?.let {
                dialog.arguments = Bundle().apply {
                    putSerializable("seatList", it)
                }
            }
            return dialog
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.chatroom_dialog_gift_reward
    }

    override fun needBottomAnimator(): Boolean {
        return true
    }

    override fun windowMode(): Int {
        return WINDOW_FULL_WIDTH
    }

    override fun gravity(): Int {
        return Gravity.BOTTOM
    }

    override fun initView() {
        super.initView()
        seatList = arguments?.getSerializable("seatList") as? ArrayList<SeatInfoModel>
        val rewardUserAdapter = RewardUserAdapter(requireContext(), seatList?.filter { !TextUtils.isEmpty(it.uid) } ?: emptyList())
        mRootView.rvUsers.adapter = rewardUserAdapter
        mRootView.rvGift.layoutManager = GridLayoutManager(context, 4)
        rewardGiftAdapter = RewardGiftAdapter(
            requireContext(), giftList
        )
        mRootView.rvGift.adapter = rewardGiftAdapter
        observeViewModel()
        giftViewModel.getGiftInfo()
        mRootView.btnReward.setOnClickListener {
            if ((rewardGiftAdapter?.selectedIndex ?: -1) < 0) {
                ToastUtil.showToast(MainApplication.getContext(), "请选择礼物")
                return@setOnClickListener
            }
            if ((rewardUserAdapter.selectedIndex) < 0) {
                ToastUtil.showToast(MainApplication.getContext(), "请选择送礼用户")
                return@setOnClickListener
            }
            giftViewModel.reward(ChatRoomManager.roomId, ChatRoomManager.myUid, rewardUserAdapter.selectUid(),
                giftList[rewardGiftAdapter?.selectedIndex ?: 0].giftId)
        }
    }

    private fun observeViewModel() {
        giftViewModel.giftInfo.observe(this, Observer {
            it?.let { list ->
                giftList.clear()
                giftList.addAll(list)
                rewardGiftAdapter?.notifyDataSetChanged()
            }
        })
    }
}