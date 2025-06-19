package com.component.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author qinverse
 * @date 2025/6/19 15:59
 * @description SpringContextUtil 类描述
 */
public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext context;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
    public static <T> T getBeanByName(String name) {
        return (T)context.getBean(name);
    }

    public static  <T> T getByClassName(Class<T> tClass) {
        return context.getBean(tClass);
    }

}
