package cn.bixin.sona.plugin.entity

/**
 *
 * @Author luokun
 * @Date 2019-12-28
 */
enum class RoleEnum(val value: Int) {
    /** 普通用户 **/
    USER(0),

    /** 管理员 **/
    ADMINISTRATOR(4),

    /** 房主 **/
    HOST(5);

    companion object Role {
        fun map(value: Int): RoleEnum {
            val values = values();
            values.forEach {
                if (it.value == value) {
                    return it
                }
            }
            return USER
        }
    }
}