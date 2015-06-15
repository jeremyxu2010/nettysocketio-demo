package personal.jeremy.socketio.test;

import com.alibaba.fastjson.JSONObject;
import personal.jeremy.socketio.annotations.WebRequestHandler;
import personal.jeremy.socketio.annotations.WebRequestMapping;
import personal.jeremy.socketio.api.WebRequstContext;

/**
 * Created by jeremy on 15/6/14.
 */
@WebRequestHandler
@WebRequestMapping("/action")
public class TestWebRequst {

    @WebRequestMapping("/path1")
    public void path1(WebRequstContext ctx){
        JSONObject resBody = new JSONObject();
        resBody.put("success", true);
        ctx.setResBody(resBody);
    }
}
