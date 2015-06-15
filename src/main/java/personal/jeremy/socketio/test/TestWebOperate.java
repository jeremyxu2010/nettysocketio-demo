package personal.jeremy.socketio.test;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import personal.jeremy.socketio.annotations.WebOperateHandler;
import personal.jeremy.socketio.annotations.WebOperateMapping;
import personal.jeremy.socketio.api.WebEventBroadcaster;
import personal.jeremy.socketio.api.WebOperateContext;

/**
 * Created by jeremy on 15/6/14.
 */
@WebOperateHandler
@WebOperateMapping("/action")
public class TestWebOperate {

    @Autowired
    private WebEventBroadcaster broadcaster;

    @WebOperateMapping("/fireGlobalEvent")
    public void fireGlobalEvent(WebOperateContext ctx){
        JSONObject paramsObj = new JSONObject();
        paramsObj.put("eventParam1", "any");
        broadcaster.broadcastToAllUsers("globalEvent", paramsObj);
    }
}
