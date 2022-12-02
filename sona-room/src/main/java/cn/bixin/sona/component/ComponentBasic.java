package cn.bixin.sona.component;

public interface ComponentBasic {

    /**
     * 装配
     */
    void assembling();

    /**
     * 卸载
     */
    void unAssembling();

    /**
     * 分发消息
     *
     * @param roomMessage 消息类型
     * @param message     消息携带的数据
     */
    void dispatchMessage(ComponentMessage roomMessage, Object message);
}
