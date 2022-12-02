package cn.bixin.sona.demo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cn.bixin.sona.api.ApiSubscriber
import cn.bixin.sona.demo.MainApplication
import cn.bixin.sona.demo.api.SonaDemoApi
import cn.bixin.sona.demo.base.BaseViewModel
import cn.bixin.sona.demo.model.ChatRoomInfoModel
import cn.bixin.sona.demo.util.ToastUtil


class ChatRoomViewModel : BaseViewModel() {

    private val _roomInfo = MutableLiveData<ChatRoomInfoModel>()
    val roomInfo: LiveData<ChatRoomInfoModel>
        get() = _roomInfo

    fun getRoomInfo(roomId: String) {
        register(
            SonaDemoApi.getRoomInfo(roomId)
                .subscribeWith(object : ApiSubscriber<ChatRoomInfoModel>() {
                    override fun onSuccess(t: ChatRoomInfoModel?) {
                        super.onSuccess(t)
                        t?.let {
                            _roomInfo.value = it
                        }
                    }

                    override fun onError(t: Throwable?) {
                        super.onError(t)
                        ToastUtil.showToast(MainApplication.getContext(), t?.message ?: "")
                    }
                })
        )
    }

    fun roomSeatOperate(roomId: String, uid: String, index: Int, operate: Int) {
        register(
            SonaDemoApi.roomSeatOperate(roomId, uid, index, operate)
                .subscribeWith(object : ApiSubscriber<Boolean>() {
                    override fun onSuccess(t: Boolean?) {
                        super.onSuccess(t)

                    }

                    override fun onError(t: Throwable?) {
                        super.onError(t)
                        ToastUtil.showToast(MainApplication.getContext(), t?.message ?: "")
                    }
                })
        )
    }

}