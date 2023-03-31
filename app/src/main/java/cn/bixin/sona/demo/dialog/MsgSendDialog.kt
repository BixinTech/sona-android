package cn.bixin.sona.demo.dialog

import android.content.Context
import android.content.DialogInterface
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import cn.bixin.sona.demo.ChatRoomManager
import cn.bixin.sona.demo.R
import cn.bixin.sona.demo.base.BaseDialogFragment
import cn.bixin.sona.demo.msg.ChatRoomTextMsg
import cn.bixin.sona.demo.util.ToastUtil
import cn.bixin.sona.plugin.PluginCallback
import cn.bixin.sona.plugin.internal.ConnectMessage
import kotlinx.android.synthetic.main.chatroom_dialog_msg_send.view.*

class MsgSendDialog : BaseDialogFragment() {

    private val imm: InputMethodManager? by lazy { activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager? }

    private val handler = Handler(Looper.getMainLooper())

    companion object {
        fun newInstance(): MsgSendDialog {
            return MsgSendDialog()
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.chatroom_dialog_msg_send
    }

    override fun windowMode(): Int {
        return WINDOW_FULL_WIDTH
    }

    override fun gravity(): Int {
        return Gravity.BOTTOM
    }

    override fun initView() {
        super.initView()
        handler.postDelayed({
            showKeyboard()
        }, 200)

        mRootView.tvSend.setOnClickListener {
            val textMsg = ChatRoomTextMsg()
            textMsg.content = mRootView.etInput.text.toString()
            textMsg.name = "用户"
            textMsg.uid = ChatRoomManager.myUid
            ChatRoomManager.connectPlugin()?.sendMessage(
                ConnectMessage.MessageCreator.createCustomMessage(
                    textMsg.toJsonString()
                ).apply {
                    msgType = 901
                }, object : PluginCallback {
                    override fun onSuccess() {
                        dismiss()
                    }

                    override fun onFailure(code: Int, reason: String?) {
                        ToastUtil.showToast(requireContext().applicationContext, reason ?: "")
                    }

                })
        }
    }

    override fun onResume() {
        super.onResume()
        mRootView.etInput.requestFocus()
    }

    private fun showKeyboard() {
        mRootView.etInput.isFocusable = true
        mRootView.etInput.isFocusableInTouchMode = true
        mRootView.etInput.requestFocus()
        imm?.showSoftInput(mRootView.etInput, 0)
    }

    override fun onDismiss(dialog: DialogInterface) {
        handler.removeCallbacksAndMessages(null)
        val view = activity?.window?.currentFocus
        view?.let { imm?.hideSoftInputFromWindow(it.windowToken, 0) }
        super.onDismiss(dialog)
    }

}