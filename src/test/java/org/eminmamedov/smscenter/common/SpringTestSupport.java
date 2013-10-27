package org.eminmamedov.smscenter.common;

import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:org/eminmamedov/smscenter/smsserver-context.xml",
        "classpath:org/eminmamedov/smscenter/test-override-context.xml" })
public abstract class SpringTestSupport implements ApplicationContextAware {

    protected ApplicationContext spring;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.spring = applicationContext;
    }

}
