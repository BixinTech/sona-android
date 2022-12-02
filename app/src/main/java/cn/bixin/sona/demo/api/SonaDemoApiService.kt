package cn.bixin.sona.demo.api

import cn.bixin.sona.base.net.Host
import cn.bixin.sona.base.net.ResponseResult
import cn.bixin.sona.demo.constant.Config
import cn.bixin.sona.demo.model.ChatRoomInfoModel
import cn.bixin.sona.demo.model.ChatRoomListModel
import cn.bixin.sona.demo.model.GiftListModel
import cn.bixin.sona.util.SonaConstant
import io.reactivex.Flowable
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

@Host(Config.DEMO_HOST)
interface SonaDemoApiService {

    @GET("/sona/demo/room/list")
    fun getRoomList(): Flowable<ResponseResult<List<ChatRoomListModel>>>

    @GET("/sona/demo/room/info")
    fun getRoomInfo(@Query("roomId") roomId: String): Flowable<ResponseResult<ChatRoomInfoModel>>

    @GET("/sona/demo/gift/list")
    fun getGiftList(): Flowable<ResponseResult<List<GiftListModel>>>

    @POST("/sona/demo/gift/reward")
    fun giftReward(@Body body: RequestBody): Flowable<ResponseResult<Boolean>>

    @POST("/sona/demo/room/mic")
    fun roomSeatOperate(@Body body: RequestBody): Flowable<ResponseResult<Boolean>>

}