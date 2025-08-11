package io.mosip.registration.processor.securezone.notification;

import io.mosip.registration.processor.securezone.notification.stage.SecurezoneNotificationStage;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class SecurezoneNotificationApplication {

    /**
     * The main method.
     *
     * @param args
     */
    public static void main(String[] args) {
        AnnotationConfigApplicationContext configApplicationContext = new AnnotationConfigApplicationContext();
        configApplicationContext.scan("io.mosip.registration.processor.core.config",
                "io.mosip.registration.processor.securezone.notification.config",
                "io.mosip.registration.processor.packet.manager.config",
                "io.mosip.registration.processor.status.config", "io.mosip.registration.processor.rest.client.config",
                "io.mosip.registration.processor.core.kernel.beans");
<<<<<<< Updated upstream
        System.out.println("branch ============================> MOSIP-18889-draft");
=======

>>>>>>> Stashed changes
        configApplicationContext.refresh();
        LogManager.getLogManager().reset();
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.FINEST);
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        SecurezoneNotificationStage notificationStage = configApplicationContext.getBean(SecurezoneNotificationStage.class);

        notificationStage.deployVerticle();
    }
}
