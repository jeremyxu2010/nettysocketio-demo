package personal.jeremy.socketio.service;

import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.SocketIOClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import personal.jeremy.socketio.api.WebEventBroadcaster;
import personal.jeremy.socketio.constants.MsgType;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by jeremy on 15/6/14.
 */
@Service
public class SocketIOEventBroadcaster implements WebEventBroadcaster {
    @Autowired
    private SocketIOConnectionManager connManager;

    public void broadcastToAllUsers(String eventName, JSONObject eventParams){
        String msg = constructMsg(eventName, eventParams);
        Iterator<SocketIOClient> allConnectionsIterator = connManager.getAllConnections().iterator();
        while(allConnectionsIterator.hasNext()){
            allConnectionsIterator.next().sendEvent("data", msg);
        }
    }

    public void broadcastToLoginUsers(String eventName, JSONObject eventParams){
        String msg = constructMsg(eventName, eventParams);
        Iterator<Set<SocketIOClient>> loginConnectionSetsIterator = connManager.getLoginConnections().iterator();
        while(loginConnectionSetsIterator.hasNext()){
            Iterator<SocketIOClient>  loginConnectionIterator = loginConnectionSetsIterator.next().iterator();
            while(loginConnectionIterator.hasNext()) {
                loginConnectionIterator.next().sendEvent("data", msg);
            }
        }
    }

    public void broadcastToSomeUsers(String eventName, JSONObject eventParams, Set<Integer> userIds){
        String msg = constructMsg(eventName, eventParams);
        Iterator<Integer> userIdsIterator = userIds.iterator();
        while(userIdsIterator.hasNext()){
            Integer userId = userIdsIterator.next();
            Set<SocketIOClient> userConnections = connManager.getUserConnections(userId);
            if(userConnections != null) {
                Iterator<SocketIOClient> userConnectionsIterator = userConnections.iterator();
                while (userConnectionsIterator.hasNext()) {
                    userConnectionsIterator.next().sendEvent("data", msg);
                }
            }
        }
    }

    private String constructMsg(String eventName, JSONObject eventParams){
        JSONObject result = new JSONObject();
        result.put("type", MsgType.EVENT_MSG);
        JSONObject event = new JSONObject();
        event.put("eventName", eventName);
        event.put("eventParams", eventParams);
        result.put("body", event);
        return result.toJSONString();
    }
}
