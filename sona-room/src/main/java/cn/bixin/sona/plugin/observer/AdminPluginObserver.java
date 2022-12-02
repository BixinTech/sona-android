package cn.bixin.sona.plugin.observer;

import cn.bixin.sona.plugin.entity.MuteEntity;
import cn.bixin.sona.plugin.entity.PluginEntity;

public interface AdminPluginObserver extends PluginObserver {

    /**
     * 管理员变更
     *
     * @param admin  1 设置管理员，0 取消管理员
     * @param entity 操作用户
     */
    void onAdministratorChange(int admin, PluginEntity entity);

    /**
     * 用户拉黑或取消拉黑
     *
     * @param block  1 拉黑，0 取消拉黑
     * @param entity
     */
    void onUserBlockChange(int block, PluginEntity entity);

    /**
     * 用户禁言或者取消禁言
     *
     * @param mute   1 禁言，0 取消禁言
     * @param entity
     */
    void onUserMuteChange(int mute, MuteEntity entity);

    /**
     * 用户被踢出，只有自己被踢会收到
     *
     * @param entity
     */
    void onUserKick(PluginEntity entity);
}
