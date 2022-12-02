package cn.bixin.sona.component.internal.audio.zego.handler;

public class PullStreamRetryEntity {
    public String streamId;
    public int retryCount;
    public int code;
    public boolean isMixStream;

    public PullStreamRetryEntity(String streamId, int retryCount, int code, boolean isMixStream) {
        this.streamId = streamId;
        this.retryCount = retryCount;
        this.code = code;
        this.isMixStream = isMixStream;
    }
}
