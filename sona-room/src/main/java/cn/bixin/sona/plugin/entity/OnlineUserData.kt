package cn.bixin.sona.plugin.entity

import java.io.Serializable

/**
 *
 * @Author luokun
 * @Date 2019-12-27
 */

class OnlineUserData : Serializable {
    var end: Boolean = false
    var list: List<OnlineUserEntity>? = null
    var anchor: String? = "0"

}