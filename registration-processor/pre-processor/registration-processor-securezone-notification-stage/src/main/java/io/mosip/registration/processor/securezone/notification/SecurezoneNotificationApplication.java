package io.mosip.registration.processor.securezone.notification;

import io.mosip.registration.processor.securezone.notification.stage.SecurezoneNotificationStage;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.logging.LogManager;

public class SecurezoneNotificationApplication {

    /**
     * The main method.
     *
     * @param args
     */
    public static void main(String[] args) {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        AnnotationConfigApplicationContext configApplicationContext = new AnnotationConfigApplicationContext();
        configApplicationContext.scan("io.mosip.registration.processor.core.config",
                "io.mosip.registration.processor.securezone.notification.config",
                "io.mosip.registration.processor.packet.manager.config",
                "io.mosip.registration.processor.status.config", "io.mosip.registration.processor.rest.client.config",
                "io.mosip.registration.processor.core.kernel.beans");
        configApplicationContext.refresh();

        SecurezoneNotificationStage notificationStage = configApplicationContext.getBean(SecurezoneNotificationStage.class);

        notificationStage.deployVerticle();
    }
}
