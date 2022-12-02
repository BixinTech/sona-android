package cn.bixin.sona.plugin;

/**
 * 插件通用回调接口
 */
public interface PluginCallback {

    /**
     * 成功
     */
    void onSuccess();

    /**
     * 失败
     *
     * @param code   错误码
     * @param reason 失败原因
     */
    void onFailure(int code, String reason);
}
