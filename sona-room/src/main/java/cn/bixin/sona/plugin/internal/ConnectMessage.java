package cn.bixin.sona.plugin.internal;

import androidx.annotation.IntDef;

import org.json.JSONObject;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ConnectMessage {

    public static final int CUSTOM = 100;
    public static final int TXT = 101;
    public static final int IMAGE = 102;
    public static final int EMOJI = 103;
    public static final int AUDIO = 104;
    public static final int VIDEO = 105;

    @IntDef({CUSTOM, TXT, IMAGE, EMOJI, AUDIO, VIDEO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MessageType {
    }

    @MessageType
    public int sonaType;
    public String message;
    public String attach;
    public File file;
    public int msgType;// 消息业务类型

    ConnectMessage(@MessageType int type, String text, String attach) {
        this.sonaType = type;
        this.message = text;
        this.attach = attach;
    }

    ConnectMessage(@MessageType int type, String text) {
        this.sonaType = type;
        this.message = text;
    }

    ConnectMessage(@MessageType int type, File file, String attach) {
        this.sonaType = type;
        this.file = file;
        this.attach = attach;
    }

    public static class MessageCreator {
        /**
         * 构建文本消息
         *
         * @param text       文本内容
         * @param attachment 附加信息，可为空
         * @return
         */
        public static ConnectMessage createTextMessage(String text, String attachment) {
            return new ConnectMessage(ConnectMessage.TXT, text, attachment);
        }

        /**
         * 构建图片消息
         *
         * @param file       图片对应的文件
         * @param attachment 附加信息，可为空
         * @return
         */
        public static ConnectMessage createImageMessage(File file, String attachment) {
            return new ConnectMessage(ConnectMessage.IMAGE, file, attachment);
        }

        /**
         * 构建emoji熊熊
         *
         * @param text
         * @param attachment 附加信息，可为空
         * @return
         */
        public static ConnectMessage createEmojiMessage(String text, String attachment) {
            return new ConnectMessage(ConnectMessage.EMOJI, text, attachment);
        }

        /**
         * 构建音频消息
         *
         * @param file       音频文件
         * @param attachment 附加信息，可为空
         * @return
         */
        public static ConnectMessage createAudioMessage(File file, String attachment) {
            return new ConnectMessage(ConnectMessage.AUDIO, file, attachment);
        }

        /**
         * 构建视频消息
         *
         * @param file       视频文件
         * @param attachment 附加信息，可为空
         * @return
         */
        public static ConnectMessage createVideoMessage(File file, String attachment) {
            return new ConnectMessage(ConnectMessage.VIDEO, file, attachment);
        }

        /**
         * 构建自定义的消息
         *
         * @param json 格式自己定义，必须为json格式
         * @return
         */
        public static ConnectMessage createCustomMessage(String json) {
            try {
                return new ConnectMessage(ConnectMessage.CUSTOM, json);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
