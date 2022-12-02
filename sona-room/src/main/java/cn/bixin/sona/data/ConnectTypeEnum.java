package cn.bixin.sona.data;

/**
 * 长连会话类型枚举
 *
 * @author luokun
 */
public enum ConnectTypeEnum {
    /**
     * 聊天室
     */
    CHATROOM("CHATROOM"),
    /**
     * 群组
     */
    TEAM("GROUP");

    private String value;

    ConnectTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
