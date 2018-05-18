package com.olv.server.interceptor;

import com.olv.common.util.LogUtils;
import com.olv.server.multysource.DataSource;
import com.olv.server.multysource.DataSourceType;
import com.olv.server.multysource.JdbcContextHolder;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;


//@Aspect
@Component
public class ChooseDefaultSourceAspect {

    /**
     * Controller切入点
     */
//    @Pointcut("execution(* com.olv.server.app.impl.*.*(..)) or execution(* com.olv.server.web.impl.*.*(..))")
    public void chooseDefaultSourceAspect() {
    }

    /**
     * 前置通知 用于拦截DataSource层记录用户的操作
     *
     * @param point 切点
     */
//    @Before("chooseDefaultSourceAspect()")
    public void doBefore(JoinPoint point) {
        try {
            String targetName = point.getTarget().getClass().getName();
            String methodName = point.getSignature().getName();
            Class targetClass = Class.forName(targetName);
            Method[] methods = targetClass.getMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    if (method!=null && !method.isAnnotationPresent(DataSource.class)) {
                        JdbcContextHolder.clearJdbcType();
                        JdbcContextHolder.setJdbcType(DataSourceType.Master.getName());
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            LogUtils.error("切换到主库数据源出错", ex);
        }
    }
}
