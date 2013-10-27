package org.eminmamedov.smscenter;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Main class to start SMSCenter Server application
 *
 * @author Emin Mamedov
 *
 */
public class SMSCenterServer {

    @SuppressWarnings("resource")
    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("classpath:org/eminmamedov/smscenter/smsserver-context.xml").registerShutdownHook();
    }

}
