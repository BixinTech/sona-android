package cn.bixin.sona.plugin;

import cn.bixin.sona.plugin.config.PluginConfig;
import cn.bixin.sona.plugin.observer.PluginObserver;

/**
 * @param <R>
 * @param <T>
 * @author luokun
 */
public interface SonaPlugin<R extends PluginConfig, T extends PluginObserver> {
    /**
     * 设置监听器
     *
     * @param observer
     */
    void observe(T observer);

    /**
     * 配置
     *
     * @param config
     */
    SonaPlugin config(R config);
}
