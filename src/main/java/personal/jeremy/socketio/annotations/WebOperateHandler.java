package personal.jeremy.socketio.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Created by jeremy on 15/6/14.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface WebOperateHandler {
}
