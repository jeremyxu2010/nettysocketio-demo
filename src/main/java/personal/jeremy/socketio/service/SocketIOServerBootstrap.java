package personal.jeremy.socketio.service;

import com.corundumstudio.socketio.SocketIOServer;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Created by jeremy on 15/6/13.
 */
@Component
public class SocketIOServerBootstrap {

    private static Logger logger = LoggerFactory.getLogger(SocketIOServerBootstrap.class);
    @Autowired
    private SocketIOServer server;

    @Autowired
    private SocketIOHandlerService handlerService;

    @PostConstruct
    public void start(){
        server.addListeners(handlerService);
        server.startAsync().addListener(new GenericFutureListener<Future<? super Void>>() {
            public void operationComplete(Future<? super Void> future) throws Exception {
                if(future.isSuccess()){
                    logger.info("socketio server started.");
                } else {
                    logger.error("socketio server start failed.", future.cause());
                }
            }
        });
    }

    @PreDestroy
    public void stop(){
        server.stop();
        logger.info("socketio server stopped.");
    }
}
