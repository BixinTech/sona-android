package com.yupaopao.mercury.library.common;

import com.alibaba.fastjson.JSON;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 1. req/res  : 1 是 request 请求， 0 是 response 响应
 * 2. twoWay : 是否需要响应，1 需要， 0 不需要
 * 3. heartbeat : 是否心跳，1是 ， 0 不是
 * 4. version : 版本号
 * 5. id ： 请求或响应的 id（从1 开始递增，单连接不重复就行）， 如果是request请求并且 twoWay 是 false ，直接设置 0
 * 6. cmd ： command 命令
 * 7. length ： 所有 header + body  的大小  （varint ）
 * 8. header size :  header 的个数 （varint ）
 * 9. header type ： header 的类型
 * 10. header length ： header data 的大小 （varint）
 * 11. header data ： header 的数据内容
 * 12. body ：消息体
 */
public class AccessMessage {

    private boolean req;

    private boolean twoWay;

    private boolean heartbeat;

    private int version;

    private int id;

    private int cmd;

    private int length;

    private List<Header> headers;

    private byte[] body;

    public JSONObject data;

    public AccessMessage() {
    }

    public AccessMessage(AccessMessage message) {
        this.req = message.req;
        this.twoWay = message.twoWay;
        this.heartbeat = message.heartbeat;
        this.version = message.version;
        this.id = message.id;
        this.cmd = message.cmd;
        if (message.headers != null) {
            this.headers = new ArrayList<>(message.headers);
        }
        this.body = message.body;
    }

    public void addHeader(Header header) {
        if (headers == null) {
            headers = new ArrayList<>();
        }
        headers.add(header);
    }

    public boolean isReq() {
        return req;
    }

    public void setReq(boolean req) {
        this.req = req;
    }

    public boolean isTwoWay() {
        return twoWay;
    }

    public void setTwoWay(boolean twoWay) {
        this.twoWay = twoWay;
    }

    public boolean isHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(boolean heartbeat) {
        this.heartbeat = heartbeat;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }




    @Override
    public String toString() {
        return "AccessMessage{" +
                "req=" + req +
                ", twoWay=" + twoWay +
                ", heartbeat=" + heartbeat +
                ", version=" + version +
                ", id=" + id +
                ", cmd=" + cmd +
                ", length=" + length +
                ", headers=" + headers +
                ", body=" + (body == null ? null : new String(body, StandardCharsets.UTF_8)) +
                '}';
    }
}
