package cn.bixin.sona.plugin.observer;

import androidx.annotation.WorkerThread;

import cn.bixin.sona.plugin.entity.MessageEntity;


public interface ConnectPluginObserver extends PluginObserver {

    /**
     * 收到消息，工作线程
     *
     * @param messageEntity
     */
    @WorkerThread
    void onReceiveMessage(MessageEntity messageEntity);

    /**
     * 断开连接，只是暂时断开连接，内部会进行重试
     */
    void onDisconnect();

    /**
     * 重连成功，内部重试成功
     */
    void onReconnect();

    /**
     * 连接异常且不可逆，建议退出房间，或者重新连接IM组件
     *
     * @param code 错误码
     */
    void onConnectError(int code);

}
