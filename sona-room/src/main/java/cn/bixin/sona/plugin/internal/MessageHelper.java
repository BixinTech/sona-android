package cn.bixin.sona.plugin.internal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * json工具类
 *
 * @Author luokun
 * @Date 2020/3/17
 */
public class MessageHelper {

    /**
     * json格式的字符串转换为map
     *
     * @param json
     * @return
     */
    public static HashMap<String, Object> getMapFromJsonString(String json) {
        HashMap<String, Object> output_json = new HashMap<>();
        try {
            JSONObject jo = new JSONObject(json);
            Iterator it = jo.keys();
            while (it.hasNext()) {
                String str = (String) it.next();
                if (jo.get(str) instanceof Integer) {
                    output_json.put(str, jo.get(str));
                } else if (jo.get(str) instanceof String) {
                    output_json.put(str, jo.get(str));
                } else if (jo.get(str) instanceof Boolean) {
                    output_json.put(str, jo.get(str));
                } else if (jo.get(str) instanceof JSONObject) {
                    output_json.put(str, getMapFromJsonString(jo.get(str).toString()));
                } else if (jo.get(str) instanceof JSONArray) {
                    ArrayList<Object> data = new ArrayList<Object>();
                    for (int i = 0; i < ((JSONArray) jo.get(str)).length(); i++) {
                        data.add(getObjectFromJsonObject(((JSONArray) jo.get(str)).get(i)));
                    }
                    output_json.put(str, data);

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return output_json;
    }

    public static Object getObjectFromJsonObject(Object json) {
        if (json == null) {
            return null;
        }
        if (json instanceof Integer) {
            return json;
        } else if (json instanceof String) {
            return json;
        } else if (json instanceof JSONObject) {
            return getMapFromJsonString(json.toString());
        } else if (json instanceof JSONArray) {
            ArrayList<Object> data = new ArrayList<>();
            for (int i = 0; i < ((JSONArray) json).length(); i++) {
                try {
                    data.add(getObjectFromJsonObject(((JSONArray) json).get(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return data;
        } else {
            return null;
        }
    }

    /**
     * 字符串转换为json
     *
     * @param content
     * @return
     */
    public static JSONObject getJson(String content) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(content);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
