package cn.bixin.sona.util;

import java.util.UUID;

public class MsgIdCreateHelper {

    public static String createMessageId() {
        return UUID.randomUUID().toString();
    }
}
