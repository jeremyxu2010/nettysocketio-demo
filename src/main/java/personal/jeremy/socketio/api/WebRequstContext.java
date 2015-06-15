package personal.jeremy.socketio.api;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by jeremy on 15/6/14.
 */
public interface WebRequstContext {
    JSONObject getReqParams();
    void setResBody(JSONObject resBody);
}
