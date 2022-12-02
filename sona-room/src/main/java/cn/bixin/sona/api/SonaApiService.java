package cn.bixin.sona.api;

import cn.bixin.sona.base.net.Host;
import cn.bixin.sona.base.net.ResponseResult;
import cn.bixin.sona.data.entity.AppInfo;
import cn.bixin.sona.data.entity.RoomInfo;
import cn.bixin.sona.data.entity.SonaConfigInfo;
import cn.bixin.sona.plugin.entity.OnlineUserData;
import cn.bixin.sona.util.SonaConstant;
import io.reactivex.Flowable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

@Host(SonaConstant.API_RELEASE)
public interface SonaApiService {

    /**
     * 创建房间
     */
    @POST("/sona/room/create")
    Flowable<ResponseResult<RoomInfo>> createRoom(@Body RequestBody body);

    /**
     * 关闭房间
     */
    @POST("/sona/room/open")
    Flowable<ResponseResult<RoomInfo>> openRoom(@Body RequestBody body);

    /**
     * 关闭房间
     */
    @POST("/sona/room/close")
    Flowable<ResponseResult<Boolean>> closeRoom(@Body RequestBody body);

    /**
     * 进入房间
     */
    @POST("/sona/room/enter")
    Flowable<ResponseResult<RoomInfo>> enterRoom(@Body RequestBody body);

    /**
     * 离开房间
     */
    @POST("/sona/room/leave")
    Flowable<ResponseResult<Boolean>> leaveRoom(@Body RequestBody body);

    /**
     * 更改房间密码
     */
    @POST("/sona/room/password/update")
    Flowable<ResponseResult<Boolean>> updateRoomPassword(@Body RequestBody body);

    /**
     * 发送文本消息
     */
    @POST("/sona/message/send")
    Flowable<ResponseResult<Boolean>> sendMessage(@Body RequestBody body);

    /**
     * 设置管理员
     */
    @POST("/sona/room/admin/set")
    Flowable<ResponseResult<Boolean>> setAdmin(@Body RequestBody body);

    /**
     * 取消管理员
     */
    @POST("/sona/room/admin/cancel")
    Flowable<ResponseResult<Boolean>> cancelAdmin(@Body RequestBody body);

    /**
     * 拉黑
     */
    @POST("/sona/room/block")
    Flowable<ResponseResult<Boolean>> black(@Body RequestBody body);

    /**
     * 取消拉黑
     */
    @POST("/sona/room/block/cancel")
    Flowable<ResponseResult<Boolean>> cancelBlack(@Body RequestBody body);

    /**
     * 禁言
     */
    @POST("/sona/room/mute")
    Flowable<ResponseResult<Boolean>> mute(@Body RequestBody body);

    /**
     * 取消禁言
     */
    @POST("/sona/room/mute/cancel")
    Flowable<ResponseResult<Boolean>> cancelMute(@Body RequestBody body);

    /**
     * 静音
     */
    @POST("/sona/stream/mute")
    Flowable<ResponseResult<Boolean>> silent(@Body RequestBody body);

    /**
     * 取消静音
     */
    @POST("/sona/stream/mute/cancel")
    Flowable<ResponseResult<Boolean>> cancelSilent(@Body RequestBody body);

    /**
     * 踢人
     */
    @POST("/sona/room/kick")
    Flowable<ResponseResult<Boolean>> kick(@Body RequestBody body);

    /**
     * 获取在线人员列表
     */
    @GET("/sona/room/member/list")
    Flowable<ResponseResult<OnlineUserData>> getOnlineUser(@Query("uid") String uid,
                                                           @Query("roomId") String roomId,
                                                           @Query("anchor") String anchor,
                                                           @Query("limit") int limit);

    /**
     * 获取在线人员数量
     */
    @GET("/sona/room/member/count")
    Flowable<ResponseResult<Integer>> getOnlineNumber(@Query("uid") String uid, @Query("roomId") String roomId);

    /**
     * 获取userSig
     *
     * @return
     */
    @GET("/sona/stream/gen/userSig")
    Flowable<ResponseResult<AppInfo>> getUserSig(@Query("roomId") String roomId, @Query("uid") String uid);

    /**
     * 获取sona配置
     *
     * @return
     */
    @GET("/sona/stream/sync/config")
    Flowable<ResponseResult<SonaConfigInfo>> syncConfig(@Query("roomId") String roomId, @Query("uid") String uid);

}
