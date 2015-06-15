package personal.jeremy.socketio.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import personal.jeremy.socketio.api.WebEventBroadcaster;
import personal.jeremy.socketio.constants.MsgType;

/**
 * Created by jeremy on 15/6/13.
 */
@Service
public class SocketIOHandlerService {

    @Autowired
    private SocketIOConnectionManager connManager;

    @Autowired
    private WebEventBroadcaster broadcaster;

    @Autowired
    private WebAnnotationInvoker annotationInvoker;

    @OnConnect
    public void onConnectHandler(SocketIOClient client) {
        connManager.regConnection(client, null);
    }

    @OnDisconnect
    public void onDisconnectHandler(SocketIOClient client) {
        connManager.unregConnection(client);
    }

    @OnEvent("data")
    public void onDataEventHandler(SocketIOClient client, String data, AckRequest ackRequest) {
        JSONObject msg = JSON.parseObject(data);
        int type = msg.getIntValue("type");
        JSONObject result = null;
        switch (type){
            case MsgType.PING_MSG:
                String pingBody = msg.getString("body");
                result = new JSONObject();
                result.put("type", MsgType.PONG_MSG);
                result.put("body", pingBody);
                client.sendEvent("data", result.toJSONString());
                break;
            case MsgType.REQ_MSG:
                int reqId = msg.getIntValue("reqId");
                JSONObject reqBody = msg.getJSONObject("body");
                String reqPath = reqBody.getString("reqPath");
                JSONObject reqParams = reqBody.getJSONObject("reqParams");
                JSONObject resBody = annotationInvoker.invokeWebRequst(reqId, reqPath, reqParams);
                if(resBody != null) {
                    result = new JSONObject();
                    result.put("type", MsgType.RES_MSG);
                    result.put("reqId", reqId);
                    result.put("body", resBody);
                    client.sendEvent("data", result.toJSONString());
                }
                break;
            case MsgType.OP_MSG:
                JSONObject opBody = msg.getJSONObject("body");
                String opPath = opBody.getString("opPath");
                JSONObject opParams = opBody.getJSONObject("opParams");
                if("/auth/sendWebToken".equals(opPath)){
                    //TODO should parse userId from webToken
                    Integer userId = opParams.getInteger("webToken");
                    connManager.regConnection(client, userId);
                } else{
                    annotationInvoker.invokeWebOperate(opPath, opParams);
                }
                break;

        }
    }
}
