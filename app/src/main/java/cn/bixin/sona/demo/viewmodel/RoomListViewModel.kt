package cn.bixin.sona.demo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cn.bixin.sona.api.ApiSubscriber
import cn.bixin.sona.demo.MainApplication
import cn.bixin.sona.demo.api.SonaDemoApi
import cn.bixin.sona.demo.base.BaseViewModel
import cn.bixin.sona.demo.model.ChatRoomListModel
import cn.bixin.sona.demo.util.ToastUtil


class RoomListViewModel : BaseViewModel() {

    private val _roomListInfo = MutableLiveData<List<ChatRoomListModel>>()
    val roomListInfo: LiveData<List<ChatRoomListModel>>
        get() = _roomListInfo

    fun getRoomList() {
        register(
            SonaDemoApi.getRoomList()
                .subscribeWith(object : ApiSubscriber<List<ChatRoomListModel>>() {
                    override fun onSuccess(t: List<ChatRoomListModel>?) {
                        super.onSuccess(t)
                        t?.let {
                            _roomListInfo.value = it
                        }
                    }

                    override fun onError(t: Throwable?) {
                        super.onError(t)
                        ToastUtil.showToast(MainApplication.getContext(), t?.message ?: "")
                    }
                })
        )
    }

}