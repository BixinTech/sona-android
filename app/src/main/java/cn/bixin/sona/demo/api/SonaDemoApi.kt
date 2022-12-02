package cn.bixin.sona.demo.api

import cn.bixin.sona.base.net.ApiServiceManager
import cn.bixin.sona.base.net.RequestParam
import cn.bixin.sona.base.net.ResponseFunc
import cn.bixin.sona.demo.model.ChatRoomInfoModel
import cn.bixin.sona.demo.model.ChatRoomListModel
import cn.bixin.sona.demo.model.GiftListModel
import cn.bixin.sona.util.RxSchedulers
import io.reactivex.Flowable

object SonaDemoApi {

    fun getRoomList(): Flowable<List<ChatRoomListModel>> {
        return ApiServiceManager.getInstance().obtainService(SonaDemoApiService::class.java)
            .getRoomList()
            .map(ResponseFunc())
            .compose(RxSchedulers.subToMain())
    }

    fun getRoomInfo(roomId: String): Flowable<ChatRoomInfoModel> {
        return ApiServiceManager.getInstance().obtainService(SonaDemoApiService::class.java)
            .getRoomInfo(roomId)
            .map(ResponseFunc())
            .compose(RxSchedulers.subToMain())
    }

    fun getGiftList(): Flowable<List<GiftListModel>> {
        return ApiServiceManager.getInstance().obtainService(SonaDemoApiService::class.java)
            .getGiftList()
            .map(ResponseFunc())
            .compose(RxSchedulers.subToMain())
    }

    fun giftReward(roomId: String, fromUid: String, toUid: String, giftId: Int): Flowable<Boolean> {
        return ApiServiceManager.getInstance().obtainService(SonaDemoApiService::class.java)
            .giftReward(
                RequestParam.paramBuilder()
                    .putParam("fromUid", fromUid)
                    .putParam("toUid", toUid)
                    .putParam("roomId", roomId)
                    .putParam("giftId", giftId)
                    .build()
                    .requestBody
            )
            .map(ResponseFunc())
            .compose(RxSchedulers.subToMain())
    }

    fun roomSeatOperate(roomId: String, uid: String, index: Int, operate: Int): Flowable<Boolean> {
        return ApiServiceManager.getInstance().obtainService(SonaDemoApiService::class.java)
            .roomSeatOperate(
                RequestParam.paramBuilder()
                    .putParam("uid", uid)
                    .putParam("roomId", roomId)
                    .putParam("index", index)
                    .putParam("operate", operate)
                    .build()
                    .requestBody
            )
            .map(ResponseFunc())
            .compose(RxSchedulers.subToMain())
    }
}