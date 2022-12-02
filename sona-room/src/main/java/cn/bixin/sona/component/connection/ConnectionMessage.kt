package cn.bixin.sona.component.connection

class ConnectionMessage(
    var group: MessageGroupEnum = MessageGroupEnum.BASIC,
    var message: String? = null,
    var item: MessageItemEnum = MessageItemEnum.UNKNOWN
) {

}