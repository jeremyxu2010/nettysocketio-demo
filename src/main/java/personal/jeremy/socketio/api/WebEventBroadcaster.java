package personal.jeremy.socketio.api;

import com.alibaba.fastjson.JSONObject;

import java.util.Set;

/**
 * Created by jeremy on 15/6/14.
 */
public interface WebEventBroadcaster {
    void broadcastToAllUsers(String eventName, JSONObject eventParams);

    void broadcastToLoginUsers(String eventName, JSONObject eventParams);

    void broadcastToSomeUsers(String eventName, JSONObject eventParams, Set<Integer> userIds);
}
