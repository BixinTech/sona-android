package cn.bixin.sona;

import java.util.Map;

public interface SonaRoomBasic {

    /**
     * 创建房间
     *
     * @param roomTitle        房间名称
     * @param productCode      产品名称
     * @param password         房间密码，可为空
     * @param ext              扩展信息
     * @param sonaRoomCallback 回调
     */
    void createRoom(String roomTitle, SonaRoomProduct productCode, String password, Map ext, SonaRoomCallback sonaRoomCallback);


    /**
     * 打开房间
     *
     * @param roomId           房间id
     * @param sonaRoomCallback 回调
     */
    void openRoom(String roomId, SonaRoomCallback sonaRoomCallback);

    /**
     * 关闭房间
     *
     * @param roomId           房间id
     * @param sonaRoomCallback 回调
     */
    void closeRoom(String roomId, SonaRoomCallback sonaRoomCallback);

    /**
     * 进入房间
     *
     * @param roomId           房间id
     * @param productCode      产品名称
     * @param password         房间密码
     * @param ext              其他参数，业务根据需要传入
     * @param sonaRoomCallback 回调
     */
    void enterRoom(String roomId, SonaRoomProduct productCode, String password, Map ext, SonaRoomCallback sonaRoomCallback);

    /**
     * 进入房间
     *
     * @param roomId           房间id
     * @param productCode      产品名称
     * @param password         房间密码
     * @param ext              其他参数，业务根据需要传入
     * @param sonaRoomCallback 回调
     */
    void enterRoom(String roomId, String productCode, String password, Map ext, SonaRoomCallback sonaRoomCallback);

    /**
     * 更改房间密码
     *
     * @param roomId           房间id
     * @param oldPassword      老密码
     * @param newPassword      新密码
     * @param sonaRoomCallback 回调
     */
    void updateRoomPassword(String roomId, String oldPassword, String newPassword, SonaRoomCallback sonaRoomCallback);

    /**
     * 离开房间
     *
     * @param sonaRoomCallback 回调
     */
    void leaveRoom(SonaRoomCallback sonaRoomCallback);

}
