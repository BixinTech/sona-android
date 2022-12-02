package cn.bixin.sona;

public interface SonaRoomObserver {

    /**
     * 收到房间事件
     *
     * @param roomEvent  事件类型
     * @param roomEntity 事件对应的描述
     */
    void onRoomReceiveEvent(RoomEvent roomEvent, RoomEntity roomEntity);
}
