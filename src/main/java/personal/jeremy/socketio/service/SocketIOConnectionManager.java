package personal.jeremy.socketio.service;

import com.corundumstudio.socketio.SocketIOClient;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by jeremy on 15/6/14.
 */
@Service
public class SocketIOConnectionManager {
    private ConcurrentMap<UUID, SocketIOClient> allConnections = new ConcurrentHashMap<UUID, SocketIOClient>();
    private ConcurrentMap<Integer, Set<SocketIOClient>> loginUserConnections = new ConcurrentHashMap<Integer, Set<SocketIOClient>>();

    private static String KEY_USER_ID = "__USER_ID__";

    public void regConnection(SocketIOClient client, Integer userId){
        allConnections.putIfAbsent(client.getSessionId(), client);
        if(userId != null) {
            client.set(KEY_USER_ID, userId);
            Set<SocketIOClient> added = new CopyOnWriteArraySet<SocketIOClient>();
            added.add(client);
            Set<SocketIOClient> pre = loginUserConnections.putIfAbsent(userId, added);
            if(pre != null){
                pre.add(client);
            }
        }
    }

    public void unregConnection(SocketIOClient client){
        allConnections.remove(client.getSessionId());
        String userId = client.get(KEY_USER_ID);
        if(userId != null){
            Set<SocketIOClient> list = loginUserConnections.get(userId);
            if(list != null){
                list.remove(client);
            }
        }
    }

    public Collection<SocketIOClient> getAllConnections() {
        return allConnections.values();
    }

    public Collection<Set<SocketIOClient>> getLoginConnections() {
        return loginUserConnections.values();
    }

    public Set<SocketIOClient> getUserConnections(Integer userId) {
        return loginUserConnections.get(userId);
    }
}
