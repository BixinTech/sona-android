package cn.bixin.sona.demo.model

class ChatRoomInfoModel : java.io.Serializable {
    var roomId: String? = null
    var name: String? = null
    var seatList: List<SeatInfoModel>? = null
}

class SeatInfoModel : java.io.Serializable {
    var index: Int = 0
    var uid: String? = ""

    fun isEmpty(): Boolean = uid.isNullOrEmpty()
}