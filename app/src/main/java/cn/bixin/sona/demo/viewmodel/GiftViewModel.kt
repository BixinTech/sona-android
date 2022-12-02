package cn.bixin.sona.demo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cn.bixin.sona.api.ApiSubscriber
import cn.bixin.sona.demo.MainApplication
import cn.bixin.sona.demo.api.SonaDemoApi
import cn.bixin.sona.demo.base.BaseViewModel
import cn.bixin.sona.demo.model.GiftListModel
import cn.bixin.sona.demo.util.ToastUtil


class GiftViewModel : BaseViewModel() {

    private val _giftInfo = MutableLiveData<List<GiftListModel>>()
    val giftInfo: LiveData<List<GiftListModel>>
        get() = _giftInfo

    fun getGiftInfo() {
        register(
            SonaDemoApi.getGiftList().subscribeWith(object : ApiSubscriber<List<GiftListModel>>() {
                override fun onSuccess(t: List<GiftListModel>?) {
                    super.onSuccess(t)
                    t?.let {
                        _giftInfo.value = it
                    }
                }

                override fun onError(t: Throwable?) {
                    super.onError(t)
                    ToastUtil.showToast(MainApplication.getContext(), t?.message ?: "")
                }
            })
        )
    }

    fun reward(roomId: String, fromUid: String, toUid: String, giftId: Int) {
        register(
            SonaDemoApi.giftReward(roomId, fromUid, toUid, giftId).subscribeWith(object : ApiSubscriber<Boolean>() {
                override fun onSuccess(t: Boolean?) {
                    super.onSuccess(t)
                    if (t == true) {
                        ToastUtil.showToast(MainApplication.getContext(), "打赏成功")
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