package cn.bixin.sona.plugin;

import java.util.List;

import cn.bixin.sona.plugin.anotation.SonaPluginAnnotation;
import cn.bixin.sona.plugin.config.AdminConfig;
import cn.bixin.sona.plugin.entity.PluginEnum;
import cn.bixin.sona.plugin.internal.OnlineUserCallback;
import cn.bixin.sona.plugin.internal.OnlineUserNumberCallback;
import cn.bixin.sona.plugin.observer.AdminPluginObserver;

/**
 * 管理插件
 * 提供管理相关能力
 *
 * @author luokun
 */
@SonaPluginAnnotation(PluginEnum.ADMIN)
public interface AdminPlugin extends SonaPlugin<AdminConfig, AdminPluginObserver> {

    /**
     * 设置管理员
     *
     * @param uid
     * @param pluginCallback
     */
    void setAdmin(String uid, PluginCallback pluginCallback);

    /**
     * 取消管理员
     *
     * @param uid
     * @param pluginCallback
     */
    void cancelAdmin(String uid, PluginCallback pluginCallback);

    /**
     * 拉黑
     *
     * @param uid
     * @param reason
     * @param pluginCallback
     */
    void black(String uid, String reason, PluginCallback pluginCallback);

    /**
     * 取消拉黑
     *
     * @param uid
     * @param reason
     * @param pluginCallback
     */
    void cancelBlack(String uid, String reason, PluginCallback pluginCallback);

    /**
     * 禁言
     *
     * @param uid
     * @param minute
     * @param pluginCallback
     */
    void mute(String uid, int minute, PluginCallback pluginCallback);

    /**
     * 取消禁言
     *
     * @param uid
     * @param pluginCallback
     */
    void cancelMute(String uid, PluginCallback pluginCallback);

    /**
     * 批量静音
     *
     * @param uids
     * @param pluginCallback
     */
    void silent(List<String> uids, PluginCallback pluginCallback);

    /**
     * 取消批量静音
     *
     * @param uids
     * @param pluginCallback
     */
    void cancelSilent(List<String> uids, PluginCallback pluginCallback);

    /**
     * 获取在线人员列表
     *
     * @param anchor             锚点
     * @param limit              数量
     * @param onlineUserCallback
     */
    void queryOnlineUsers(String anchor, int limit, OnlineUserCallback onlineUserCallback);

    /**
     * 获取在线人员数量
     *
     * @param onlineUserNumberCallback
     */
    void queryOnlineUserNumber(OnlineUserNumberCallback onlineUserNumberCallback);

    /**
     * 踢人
     *
     * @param uid
     * @param pluginCallback
     */
    void kick(String uid, PluginCallback pluginCallback);
}
