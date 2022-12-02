package cn.bixin.sona.demo.dialog

import android.os.Bundle
import android.view.Gravity
import androidx.recyclerview.widget.GridLayoutManager
import cn.bixin.sona.demo.ChatRoomManager
import cn.bixin.sona.demo.R
import cn.bixin.sona.demo.adapter.ChatRoomUserManageAdapter
import cn.bixin.sona.demo.base.BaseDialogFragment
import cn.bixin.sona.demo.model.UserManageModel
import cn.bixin.sona.plugin.PluginCallback
import kotlinx.android.synthetic.main.chatroom_dialog_user_manage.view.*

class UserManageDialog : BaseDialogFragment() {

    private var uid: String = ""

    companion object {
        fun newInstance(uid: String): UserManageDialog {
            val dialog = UserManageDialog()
            dialog.arguments = Bundle().apply {
                putString("uid", uid)
            }
            return dialog
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.chatroom_dialog_user_manage
    }

    override fun initView() {
        super.initView()
        uid = arguments?.getString("uid") ?: ""

        mRootView.rvUserManage.layoutManager = GridLayoutManager(requireContext(), 4)
        mRootView.rvUserManage.adapter = ChatRoomUserManageAdapter(
            requireContext(),
            listOf(
                UserManageModel("禁麦", R.mipmap.chatroom_ic_mute_seat),
                UserManageModel("开麦", R.mipmap.chatroom_ic_cancel_mute_seat),
                UserManageModel("拉黑", R.mipmap.chatroom_ic_block_user),
                UserManageModel("禁言", R.mipmap.chatroom_ic_mute_user),
                UserManageModel("请出房间", R.mipmap.chatroom_ic_kick_user)
            )
        ).apply {
            onItemClick = {
                when (it) {
                    0 -> {
                        ChatRoomManager.adminPlugin()?.silent(listOf(uid), object : PluginCallback {
                            override fun onSuccess() {

                            }

                            override fun onFailure(code: Int, reason: String?) {

                            }

                        })
                    }
                    1 -> {
                        ChatRoomManager.adminPlugin()
                            ?.cancelSilent(listOf(uid), object : PluginCallback {
                                override fun onSuccess() {

                                }

                                override fun onFailure(code: Int, reason: String?) {

                                }

                            })
                    }
                    2 -> {
                        ChatRoomManager.adminPlugin()?.black(uid, "广告", object : PluginCallback {
                            override fun onSuccess() {

                            }

                            override fun onFailure(code: Int, reason: String?) {

                            }

                        })
                    }
                    3 -> {
                        ChatRoomManager.adminPlugin()?.mute(uid, 5, object : PluginCallback {
                            override fun onSuccess() {

                            }

                            override fun onFailure(code: Int, reason: String?) {

                            }

                        })
                    }
                    4 -> {
                        ChatRoomManager.adminPlugin()?.kick(uid, object : PluginCallback {
                            override fun onSuccess() {

                            }

                            override fun onFailure(code: Int, reason: String?) {

                            }

                        })
                    }
                }
            }
        }
    }

    override fun windowMode(): Int {
        return WINDOW_FULL_WIDTH
    }

    override fun gravity(): Int {
        return Gravity.BOTTOM
    }

    override fun needBottomAnimator(): Boolean {
        return true
    }
}