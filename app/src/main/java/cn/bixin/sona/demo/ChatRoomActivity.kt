package cn.bixin.sona.demo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.SparseArray
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cn.bixin.sona.base.Sona
import cn.bixin.sona.demo.adapter.RoomMsgAdapter
import cn.bixin.sona.demo.base.BaseActivity
import cn.bixin.sona.demo.dialog.GiftRewardDialog
import cn.bixin.sona.demo.dialog.MsgSendDialog
import cn.bixin.sona.demo.dialog.UserManageDialog
import cn.bixin.sona.demo.gift.GiftAnimationHelper
import cn.bixin.sona.demo.model.SeatInfoModel
import cn.bixin.sona.demo.model.SeatOperateEvent
import cn.bixin.sona.demo.msg.*
import cn.bixin.sona.demo.util.PermissionUtil
import cn.bixin.sona.demo.util.ToastUtil
import cn.bixin.sona.demo.viewmodel.ChatRoomViewModel
import cn.bixin.sona.demo.widget.SeatView
import cn.bixin.sona.plugin.PluginCallback
import kotlinx.android.synthetic.main.chatroom_bottom.*
import kotlinx.android.synthetic.main.chatroom_header.*
import kotlinx.android.synthetic.main.chatroom_msg_list.*
import kotlinx.android.synthetic.main.chatroom_reward_animation.*
import kotlinx.android.synthetic.main.chatroom_seat.*
import org.greenrobot.eventbus.EventBus

class ChatRoomActivity : BaseActivity() {

    companion object {
        const val ROOM_ID = "roomId"
        const val ROOM_TITLE = "roomTitle"
    }

    private val roomInfoViewModel by lazy {
        ViewModelProvider(this).get(ChatRoomViewModel::class.java)
    }
    private var roomMsgList = ArrayList<BaseChatRoomMsg>()
    private var roomMsgAdapter: RoomMsgAdapter? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val giftAnimationHelper = GiftAnimationHelper()
    private val seatMap = SparseArray<SeatView>()
    private var seatList: List<SeatInfoModel>? = null
    private var isMicOpen = false

    private var roomId = ""
    private var uid = System.currentTimeMillis().toString()

    override fun getLayoutId(): Int = R.layout.activity_chatroom

    override fun initView() {
        roomId = intent.getStringExtra(ROOM_ID) ?: ""
        rvMsgList.layoutManager = LinearLayoutManager(this)
        roomMsgAdapter = RoomMsgAdapter(this, roomMsgList)
        rvMsgList.adapter = roomMsgAdapter
        giftAnimationHelper.bind(ivGiftAnimation)
        initSeat()
        initListener()

        Sona.initUserInfo(uid)
        initSonaRoom()
        enterRoom()
    }

    private fun enterRoom() {
        val roomTitle = intent.getStringExtra(ROOM_TITLE) ?: ""
        if (roomTitle.isNotEmpty()) {
            ChatRoomManager.createRoom(roomTitle, {
                ToastUtil.showToast(applicationContext, "进房成功")
                roomInfoViewModel.getRoomInfo(ChatRoomManager.roomId)
                ChatRoomManager.audioPlugin()?.startListen(object : PluginCallback {
                    override fun onSuccess() {
                        ToastUtil.showToast(applicationContext, "拉流成功")
                    }

                    override fun onFailure(code: Int, reason: String?) {
                        ToastUtil.showToast(applicationContext, "拉流失败: $reason")
                    }

                })
            }, {
                ToastUtil.showToast(applicationContext, it)
            })
        } else {
            ChatRoomManager.enterRoom(roomId, {
                ToastUtil.showToast(applicationContext, "进房成功")
                roomInfoViewModel.getRoomInfo(ChatRoomManager.roomId)
                ChatRoomManager.audioPlugin()?.startListen(object : PluginCallback {
                    override fun onSuccess() {
                        ToastUtil.showToast(applicationContext, "拉流成功")
                    }

                    override fun onFailure(code: Int, reason: String?) {
                        ToastUtil.showToast(applicationContext, "拉流失败: $reason")
                    }

                })
            }, {
                ToastUtil.showToast(applicationContext, it)
            })
        }
    }

    override fun initViewModel() {
        roomInfoViewModel.roomInfo.observe(this, Observer {
            tvRoomTitle.text = it?.name
            this.seatList = it.seatList
            bindSeat()
        })
    }

    private fun initSeat() {
        seatMap.put(0, seatFirst)
        seatMap.put(1, seatSecond)
        seatMap.put(2, seatThird)
        seatMap.put(3, seatFourth)
        seatMap.put(4, seatFifth)
        seatMap.put(5, seatSixth)
    }

    private fun initListener() {
        ivClose.setOnClickListener {
            finish()
        }
        tvChat.setOnClickListener {
            MsgSendDialog.newInstance().show(supportFragmentManager)
        }
        ivMic.setOnClickListener {
            handleMic()
        }

        ivGift.setOnClickListener {
            GiftRewardDialog.newInstance(ArrayList(seatList ?: emptyList())).show(supportFragmentManager)
        }

        ivMore.setOnClickListener {
            UserManageDialog.newInstance(ChatRoomManager.myUid).show(supportFragmentManager)
        }
        tvUpOrDownSeat.setOnClickListener {
            if ((seatList?.size ?: 0) == 0) return@setOnClickListener
            if (isOnSeat()) {
                roomInfoViewModel.roomSeatOperate(ChatRoomManager.roomId, ChatRoomManager.myUid, getOnSeatIndex(), 0)
            } else {
                val emptySeatIndex = getEmptySeatIndex()
                if (emptySeatIndex < 0) {
                    ToastUtil.showToast(MainApplication.getContext(), "麦位已满")
                    return@setOnClickListener
                }
                roomInfoViewModel.roomSeatOperate(ChatRoomManager.roomId, ChatRoomManager.myUid, emptySeatIndex, 1)
            }
        }
    }

    private fun initSonaRoom() {
        ChatRoomManager.roomId = roomId
        ChatRoomManager.myUid = uid
        ChatRoomManager.initSonaRoom()
        ChatRoomManager.onReceiveMsg = {
            mainHandler.post {
                when (it) {
                    is ChatRoomTextMsg, is ChatRoomGiftRewardTipsMsg  -> {
                        roomMsgList.add(it)
                        roomMsgAdapter?.notifyDataSetChanged()
                    }
                    is ChatRoomGiftRewardMsg -> {
                        giftAnimationHelper.play(it.giftId ?: 0)
                    }
                    is ChatRoomSeatOperateMsg -> {
                        seatList?.forEach { seatInfoModel ->
                            if (it.index == seatInfoModel.index) {
                                seatInfoModel.uid = if (it.isOnSeatOperate()) it.uid else ""
                                return@forEach
                            }
                        }
                        bindSeat()
                        EventBus.getDefault().post(SeatOperateEvent(seatList))
                    }
                }
            }
        }

        ChatRoomManager.onSoundLevelInfo = {
            mainHandler.post {
                it?.forEach { soundLevelInfo ->
                    if ((soundLevelInfo.soundLevel ?: 0f) > 1) {
                        seatList?.forEach { seatInfo ->
                            if (TextUtils.equals(soundLevelInfo.uid, seatInfo.uid)) {
                                seatMap[seatInfo.index].startSpeakAnimation()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun isOnSeat(): Boolean {
        seatList?.forEach {
            if (TextUtils.equals(uid, it.uid)) {
                return true
            }
        }
        return false
    }

    private fun getOnSeatIndex(): Int {
        seatList?.forEach {
            if (TextUtils.equals(uid, it.uid)) {
                return it.index
            }
        }
        return -1
    }

    private fun getEmptySeatIndex(): Int {
        seatList?.forEach {
            if (TextUtils.isEmpty(it.uid)) {
                return it.index
            }
        }
        return -1
    }

    private fun bindSeat() {
        seatList?.forEachIndexed { index, seatInfoModel ->
            seatMap[index]?.bind(seatInfoModel)
        }
        if (isOnSeat()) {
            tvUpOrDownSeat.text = "我要下麦"
            ivMic.visibility = View.VISIBLE
            if (PermissionUtil.checkPermission(this, arrayOf(Manifest.permission.RECORD_AUDIO), 10001)) {
                pushStream()
            }
        } else {
            tvUpOrDownSeat.text = "我要上麦"
            ivMic.visibility = View.GONE
            ChatRoomManager.audioPlugin()?.stopSpeak(object : PluginCallback {
                override fun onSuccess() {

                }

                override fun onFailure(code: Int, reason: String?) {

                }

            })
        }
    }

    private fun handleMic() {
        if (isMicOpen) {
            isMicOpen = false
            ivMic.setImageResource(R.mipmap.chatroom_ic_bottom_bar_micro_off)
            switchMic(false)
        } else {
            ivMic.setImageResource(R.mipmap.chatroom_ic_bottom_bar_micro_open)
            isMicOpen = true
            switchMic(true)
        }
        seatList?.forEach {
            if (TextUtils.equals(ChatRoomManager.myUid, it.uid)) {
                seatMap.get(it.index)?.handleMic(isMicOpen)
                return@forEach
            }
        }
    }

    private fun unInitSonaRoom() {
        ChatRoomManager.unInitSonaRoom()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 10001) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pushStream()
            } else {
                ToastUtil.showToast(MainApplication.getContext(), "请开启麦克风权限")
            }
        }
    }

    private fun pushStream() {
        ChatRoomManager.audioPlugin()?.startSpeak(object : PluginCallback {
            override fun onSuccess() {
                ToastUtil.showToast(MainApplication.getContext(), "推流成功")
            }

            override fun onFailure(code: Int, reason: String?) {

            }

        })
        switchMic(false)
    }

    private fun switchMic(on: Boolean) {
        ChatRoomManager.audioPlugin()?.switchMic(on, object : PluginCallback {
            override fun onSuccess() {

            }

            override fun onFailure(code: Int, reason: String?) {

            }

        })
    }

    override fun onDestroy() {
        super.onDestroy()
        mainHandler.removeCallbacksAndMessages(null)
        unInitSonaRoom()
    }
}