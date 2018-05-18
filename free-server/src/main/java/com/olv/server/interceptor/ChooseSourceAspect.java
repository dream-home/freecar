package com.olv.server.interceptor;

import com.olv.common.annotation.SystemControllerLog;
import com.olv.common.util.LogUtils;
import com.olv.server.multysource.DataSource;
import com.olv.server.multysource.JdbcContextHolder;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;


@Aspect
@Component
public class ChooseSourceAspect {

    /**
     * Controller切入点
     */
    @Pointcut("@annotation(com.olv.server.multysource.DataSource)")
    public void chooseSourceAspect() {
    }

    /**
     * 前置通知 用于拦截DataSource层记录用户的操作
     *
     * @param point 切点
     */
    @Before("chooseSourceAspect()")
    public void doBefore(JoinPoint point) {
        try {
            String targetName = point.getTarget().getClass().getName();
            String methodName = point.getSignature().getName();
            Class targetClass = Class.forName(targetName);
            Method[] methods = targetClass.getMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    if (method!=null && method.isAnnotationPresent(DataSource.class)) {
                        DataSource data = method.getAnnotation(DataSource.class);
                        JdbcContextHolder.clearJdbcType();
                        JdbcContextHolder.setJdbcType(data.value().getName());
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            LogUtils.error("切换数据源出错", ex);
        }
    }
}
