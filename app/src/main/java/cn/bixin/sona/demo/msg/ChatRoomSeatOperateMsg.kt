package cn.bixin.sona.demo.msg

class ChatRoomSeatOperateMsg : BaseChatRoomMsg() {
    var index: Int = 0
    var uid: String = ""
    var operate: Int = 0

    fun isOnSeatOperate(): Boolean = operate == 1
}