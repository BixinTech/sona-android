package cn.bixin.sona.base.net;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;

public class ResponseResult<T> implements Serializable {

    private String code;//8000 正常业务
    private String msg;


    @SerializedName(value = "result", alternate = {"data"})
    private T result;
    private Map<String, String> ext;

    public static boolean isSuccess(String code) {
        return TextUtils.equals(ApiException.SUCCESS_8000, code);
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return result;
    }

    public Map<String, String> getExt() {
        return ext;
    }

    public static <T> ResponseResult<T> create(String code, String msg, T result, Map<String, String> ext){
        ResponseResult<T> responseResult = new ResponseResult<T>();
        responseResult.code = code;
        responseResult.msg = msg;
        responseResult.result = result;
        responseResult.ext = ext;
        return responseResult;
    }
}
