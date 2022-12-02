package cn.bixin.sona.base.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class RequestParam {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final HashMap<String, Object> mParams;

    public RequestParam(Builder builder) {
        mParams = builder.mParams;
    }

    public static Builder paramBuilder() {
        return new Builder();
    }

    public static Builder paramBuilder(Map<String, Object> params) {
        return new Builder();
    }

    public RequestBody getRequestBody() {
        return RequestBody.create(JSON, mParams == null || mParams.isEmpty() ? "" : toJson(mParams));
    }

    public static final class Builder {
        private HashMap<String, Object> mParams = new HashMap<>();

        private Builder() {
        }

        private Builder(Map<String, Object> params) {
            strToObject(params);
        }


        public Builder putParam(String key, Object value) {
            mParams.put(key, value);
            return this;
        }

        public Builder putParam(Map<String, Object> params) {
            strToObject(params);
            return this;
        }

        public RequestParam build() {
            return new RequestParam(this);
        }

        private void strToObject(Map<String, Object> params) {
            if (params != null) {
                for (String key : params.keySet()) {
                    mParams.put(key, params.get(key));
                }
            }
        }

    }

    private static <T> String toJson(T t) {
        if (t == null) {
            return "";
        }

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        try {
            return gson.toJson(t);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }
}
