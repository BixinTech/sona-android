package com.yupaopao.mercury.library.common;

/**
 * @author qinwei
 */
public enum CommandEnum {
    LOCAL_CONNECT(-100, "本地连接"),

    CLOSE_CHANNEL(-1, "断开连接"),

    LOGIN_AUTH(1, "客户端登录认证"),

    CLIENT_PUSH(2, "客户端上报消息"),

    CHATROOM_JOIN(10, "加入房间"),

    CHATROOM_LEAVE(11, "离开房间"),

    CHATROOM_SEND(12, "发送房间消息"),

    CHATROOM_SIGNAL(13, "发送房间指令");

    private int command;

    private String desc;

    CommandEnum(int command, String desc) {
        this.command = command;
        this.desc = desc;
    }

    public int getCommand() {
        return command;
    }

}
