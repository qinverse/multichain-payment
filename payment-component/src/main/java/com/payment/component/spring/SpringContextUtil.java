package com.payment.component.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * @author qinverse
 * @date 2025/6/19 15:59
 * @description SpringContextUtil 类描述
 */
@Service
@Slf4j
public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext context;

    public SpringContextUtil() {
        log.info("------初始化SpringContextUtil----");
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
    public static <T> T getBeanByName(String name) {
        return (T)SpringContextUtil.context.getBean(name);
    }

    public static  <T> T getByClassName(Class<T> tClass) {
        return SpringContextUtil.context.getBean(tClass);
    }

}
