package cn.bixin.sona.plugin;

import cn.bixin.sona.plugin.anotation.SonaPluginAnnotation;
import cn.bixin.sona.plugin.config.ConnectConfig;
import cn.bixin.sona.plugin.entity.PluginEnum;
import cn.bixin.sona.plugin.internal.ConnectMessage;
import cn.bixin.sona.plugin.observer.ConnectPluginObserver;

/**
 * 长连插件
 * 提供长连的相关能力
 *
 * @author luokun
 */
@SonaPluginAnnotation(PluginEnum.CONNECT)
public interface ConnectPlugin extends SonaPlugin<ConnectConfig, ConnectPluginObserver> {

    /**
     * 发送消息
     *
     * @param connectMessage MessageCreator 创建消息
     * @param pluginCallback
     */
    void sendMessage(ConnectMessage connectMessage, PluginCallback pluginCallback);

    /**
     * 发送消息
     *
     * @param connectMessage
     * @param needToSave     是否需要服务器保存消息
     * @param pluginCallback
     */
    void sendMessage(ConnectMessage connectMessage, int needToSave, PluginCallback pluginCallback);

    /**
     * 开始重连长连
     *
     * @param pluginCallback
     */
    void startReconnect(PluginCallback pluginCallback);
}
