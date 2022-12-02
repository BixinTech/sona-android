package cn.bixin.sona.plugin.internal

interface OnlineUserNumberCallback {

    /**
     * 成功
     *
     * @param count 在线人员数量
     */
    fun onSuccess(count: Int)

    /**
     * 错误
     *
     * @param code 错误码
     * @param reason 错误原因
     */
    fun onFailure(code: Int, reason: String?)
}