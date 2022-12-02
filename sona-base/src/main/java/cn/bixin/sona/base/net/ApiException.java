package cn.bixin.sona.base.net;

import java.io.IOException;
import java.util.Map;

/**
 * https://doc.yupaopao.com/pages/viewpage.action?pageId=5639357
 */
public class ApiException extends IOException {
    //8000 业务请求成功
    public static final String SUCCESS_8000 = "8000";
    public static final String SUCCESS_200 = "200";

    public static final String ERROR_TIMEOUT = "网络请求失败";

    private String code;
    private String message;
    private Object result;
    public Map<String, String> ext;
    /**
     * 原始异常数据
     */
    public Throwable throwable;

    public ApiException() {
    }

    public ApiException(String code, String msg) {
        super(msg);
        this.code = code;
        message = msg;
    }

    public ApiException(String code, String msg, Object result) {
        super(msg);
        this.code = code;
        this.message = msg;
        this.result = result;
    }

    public ApiException(String code, String msg, Object result, Map<String, String> ext) {
        super(msg);
        this.code = code;
        this.message = msg;
        this.result = result;
        this.ext = ext;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Object getObject() {
        return result;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    /**
     * 请求时发生的链接，域名解析等网络请求问题
     *
     * @param e 异常捕获
     */
    public static ApiException catchRequestError(Throwable e) {
        ApiException apiException = new ApiException();
        apiException.setThrowable(e);
        return apiException;
    }

    @Override
    public String toString() {
        return "ApiException{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", result=" + result +
                ", ext=" + ext +
                ", throwable=" + throwable +
                ", cause=" + (throwable == null ? null : throwable.getCause()) +
                '}';
    }
}
