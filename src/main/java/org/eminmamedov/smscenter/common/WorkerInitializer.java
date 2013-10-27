package org.eminmamedov.smscenter.common;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class WorkerInitializer implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public <T> T autowire(T bean) {
        applicationContext.getAutowireCapableBeanFactory().autowireBean(bean);
        return bean;
    }

}
