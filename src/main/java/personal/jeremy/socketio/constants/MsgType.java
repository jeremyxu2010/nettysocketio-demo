package personal.jeremy.socketio.constants;

/**
 * Created by jeremy on 15/6/13.
 */
public interface MsgType {
    public static final int REQ_MSG = 0;
    public static final int RES_MSG = 1;
    public static final int EVENT_MSG = 2;
    public static final int PING_MSG = 3;
    public static final int PONG_MSG = 4;
    public static final int OP_MSG = 5;
}
