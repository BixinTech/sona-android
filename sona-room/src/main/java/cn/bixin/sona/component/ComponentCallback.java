package cn.bixin.sona.component;

public interface ComponentCallback {

    /**
     * 执行成功
     */
    void executeSuccess();

    /**
     * 执行失败
     *
     * @param code   错误码
     * @param reason 错误原因
     */
    void executeFailure(int code, String reason);
}