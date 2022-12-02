package cn.bixin.sona;

public interface SonaRoomCallback {

    /**
     * 成功
     *
     * @param roomId 房间id
     */
    void onSuccess(String roomId);

    /**
     * 失败
     *
     * @param code   错误码
     * @param reason 错误原因
     */
    void onFailed(int code, String reason);
}
