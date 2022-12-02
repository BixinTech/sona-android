package cn.bixin.sona

/**
 * 插件状态查询
 *
 * @Author luokun
 * @Date 2020/8/21
 */
class PluginResult(
    var action: Action, var code: Int = 1
    /** 0 代表状态是对的**/
) {

    var msg: String? = null

    constructor(action: Action, code: Int, msg: String?) : this(action, code) {
        this.msg = msg
    }

    enum class Action {
        /**
         * 加载状态
         */
        LOAD,

        /**
         * 合法性
         */
        LEGAL
    }
}