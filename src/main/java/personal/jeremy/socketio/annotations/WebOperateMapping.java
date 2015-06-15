package personal.jeremy.socketio.annotations;

import java.lang.annotation.*;

/**
 * Created by jeremy on 15/6/14.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebOperateMapping {
    String[] value() default {};
}
