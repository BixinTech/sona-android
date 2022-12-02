package cn.bixin.sona.plugin.internal

import cn.bixin.sona.plugin.entity.OnlineUserData

interface OnlineUserCallback {

    /**
     * 成功
     *
     * @param users 在线人员信息列表
     */
    fun onSuccess(onlineUserData: OnlineUserData)

    /**
     * 错误
     *
     * @param code 错误码
     * @param reason 错误原因
     */
    fun onFailure(code: Int, reason: String?)
}