package com.cncmes.annotation.processor;

import com.cncmes.annotation.IgnoreException;

import java.lang.reflect.Method;

/**
 * *Zhong
 * *
 */
public class IgnoreExceptionProcessor {
    public static void process(Object obj, String methodName) {
        try {
            Method method = obj.getClass().getMethod(methodName);
            if (method.isAnnotationPresent(IgnoreException.class)) {
                IgnoreException annotation = method.getAnnotation(IgnoreException.class);
                Class<? extends Throwable>[] exceptions = annotation.value();
                for (Class<? extends Throwable> exception : exceptions) {
                    try {
                        method.invoke(obj);
                    } catch (Throwable ex) {
                        if (!exception.isInstance(ex)) {
                            throw ex;
                        }
                    }
                }
            } else {
                method.invoke(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
