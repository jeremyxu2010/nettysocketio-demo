package personal.jeremy.socketio.service;

import com.alibaba.fastjson.JSONObject;
import net.sf.corn.cps.CPScanner;
import net.sf.corn.cps.ClassFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import personal.jeremy.socketio.annotations.WebOperateHandler;
import personal.jeremy.socketio.annotations.WebOperateMapping;
import personal.jeremy.socketio.annotations.WebRequestHandler;
import personal.jeremy.socketio.annotations.WebRequestMapping;
import personal.jeremy.socketio.api.WebOperateContext;
import personal.jeremy.socketio.api.WebRequstContext;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by jeremy on 15/6/14.
 */
public class WebAnnotationInvoker implements ApplicationContextAware {

    private List<String> pkgs;
    private static ApplicationContext applicationContext;
    private static Logger logger = LoggerFactory.getLogger(WebAnnotationInvoker.class);

    private ConcurrentMap<String, InvokeMappingInfo> webRequestMappingInfos = new ConcurrentHashMap<String, InvokeMappingInfo>();

    private ConcurrentMap<String, InvokeMappingInfo> webOperateMappingInfos = new ConcurrentHashMap<String, InvokeMappingInfo>();

    @PostConstruct
    public void start(){
        if(this.pkgs != null){
            for(String pkg : pkgs){
                List<Class<?>> webRequestHandlers = CPScanner.scanClasses(new ClassFilter().packageName(pkg).annotation(WebRequestHandler.class));
                for(Class clazz : webRequestHandlers){
                    boolean hasClassMapping = false;
                    Annotation[] clzAnnos = clazz.getDeclaredAnnotations();
                    for(Annotation clzAnno : clzAnnos){
                        if(clzAnno instanceof WebRequestMapping){
                            String[] clzMappingValue = ((WebRequestMapping) clzAnno).value();
                            if(clzMappingValue != null && clzMappingValue.length == 1){
                                hasClassMapping = true;
                                String clzMappingPath = clzMappingValue[0];
                                Method[] methods = clazz.getDeclaredMethods();
                                for(Method method : methods){
                                    WebRequestMapping methodAnno = method.getAnnotation(WebRequestMapping.class);
                                    String[] methodMappingValue = methodAnno.value();
                                    if(methodMappingValue != null && methodMappingValue.length == 1){
                                        String methodMappingPath = methodMappingValue[0];
                                        String path = clzMappingPath + methodMappingPath;
                                        InvokeMappingInfo pre = webRequestMappingInfos.putIfAbsent(path, new InvokeMappingInfo(applicationContext.getBean(clazz), method));
                                        if(pre != null){
                                            logger.warn("WebRequestMapping with path=" + path + " duplicate, the following one ignore");
                                        }
                                    }
                                }

                            }
                        }
                    }

                    if(!hasClassMapping){
                        Method[] methods = clazz.getDeclaredMethods();
                        for(Method method : methods){
                            WebRequestMapping methodAnno = method.getAnnotation(WebRequestMapping.class);
                            String[] methodMappingValue = methodAnno.value();
                            if(methodMappingValue != null && methodMappingValue.length == 1){
                                String methodMappingPath = methodMappingValue[0];
                                String path = methodMappingPath;
                                InvokeMappingInfo pre = webRequestMappingInfos.putIfAbsent(path, new InvokeMappingInfo(applicationContext.getBean(clazz), method));
                                if(pre != null){
                                    logger.warn("WebRequestMapping with path=" + path + " duplicate, the following one ignore");
                                }
                            }
                        }
                    }
                }

                List<Class<?>> WebOperateHandlers = CPScanner.scanClasses(new ClassFilter().packageName(pkg).annotation(WebOperateHandler.class));
                for(Class clazz : WebOperateHandlers){
                    boolean hasClassMapping = false;
                    Annotation[] clzAnnos = clazz.getDeclaredAnnotations();
                    for(Annotation clzAnno : clzAnnos){
                        if(clzAnno instanceof WebOperateMapping){
                            String[] clzMappingValue = ((WebOperateMapping) clzAnno).value();
                            if(clzMappingValue != null && clzMappingValue.length == 1){
                                hasClassMapping = true;
                                String clzMappingPath = clzMappingValue[0];
                                Method[] methods = clazz.getDeclaredMethods();
                                for(Method method : methods){
                                    WebOperateMapping methodAnno = method.getAnnotation(WebOperateMapping.class);
                                    String[] methodMappingValue = methodAnno.value();
                                    if(methodMappingValue != null && methodMappingValue.length == 1){
                                        String methodMappingPath = methodMappingValue[0];
                                        String path = clzMappingPath + methodMappingPath;
                                        InvokeMappingInfo pre = webOperateMappingInfos.putIfAbsent(path, new InvokeMappingInfo(applicationContext.getBean(clazz), method));
                                        if(pre != null){
                                            logger.warn("WebOperateMapping with path=" + path + " duplicate, the following one ignore");
                                        }
                                    }
                                }

                            }
                        }
                    }

                    if(!hasClassMapping){
                        Method[] methods = clazz.getDeclaredMethods();
                        for(Method method : methods){
                            WebOperateMapping methodAnno = method.getAnnotation(WebOperateMapping.class);
                            String[] methodMappingValue = methodAnno.value();
                            if(methodMappingValue != null && methodMappingValue.length == 1){
                                String methodMappingPath = methodMappingValue[0];
                                String path = methodMappingPath;
                                InvokeMappingInfo pre = webOperateMappingInfos.putIfAbsent(path, new InvokeMappingInfo(applicationContext.getBean(clazz), method));
                                if(pre != null){
                                    logger.warn("WebOperateMapping with path=" + path + " duplicate, the following one ignore");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public JSONObject invokeWebRequst(int reqId, String reqPath, JSONObject reqParams){
        InvokeMappingInfo info = webRequestMappingInfos.get(reqPath);
        if(info != null) {
            try {
                WebRequstContextImpl ctx = new WebRequstContextImpl();
                ctx.setReqParams(reqParams);
                ctx.setResBody(new JSONObject());
                info.getMethod().invoke(info.getTarget(), ctx);
                return ctx.getResBody();
            } catch (Exception e) {
                logger.error("invoke web request failed.",e);
            }
        }
        return null;
    }

    public void invokeWebOperate(String opPath, JSONObject opParams){
        InvokeMappingInfo info = webOperateMappingInfos.get(opPath);
        if(info != null) {
            try {
                WebOperateContextImpl ctx = new WebOperateContextImpl();
                ctx.setReqParams(opParams);
                info.getMethod().invoke(info.getTarget(), ctx);
            } catch (Exception e) {
                logger.error("invoke web request failed.",e);
            }
        }
    }

    public List<String> getPkgs() {
        return pkgs;
    }

    public void setPkgs(List<String> pkgs) {
        this.pkgs = pkgs;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private class InvokeMappingInfo {
        private Object target;
        private Method method;
        public InvokeMappingInfo(Object target, Method method){
            this.target = target;
            this.method = method;
        }

        public Object getTarget() {
            return target;
        }

        public Method getMethod() {
            return method;
        }

    }

    private class WebRequstContextImpl implements WebRequstContext {
        private JSONObject reqParams;
        private JSONObject resBody;

        public void setReqParams(JSONObject reqParams) {
            this.reqParams = reqParams;
        }

        public JSONObject getReqParams() {
            return this.reqParams;
        }

        public void setResBody(JSONObject resBody) {
            this.resBody = resBody;
        }

        public JSONObject getResBody() {
            return resBody;
        }
    }

    private class WebOperateContextImpl implements WebOperateContext {
        private JSONObject reqParams;

        public void setReqParams(JSONObject reqParams) {
            this.reqParams = reqParams;
        }

        public JSONObject getReqParams() {
            return this.reqParams;
        }
    }
}
