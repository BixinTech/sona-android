package cn.bixin.sona;

/**
 * @Author luokun
 * @Date 2020-01-02
 */
enum class RoomEvent(val event: Int) {

    USER_ENTER(70000), // 有人进入房间
    USER_LEAVE(70001), // 有人离开房间
    ROOM_CLOSE(70002), // 房间关闭
}
