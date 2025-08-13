package io.mosip.registration.processor.quality.classifier;

import io.mosip.registration.processor.quality.classifier.stage.QualityClassifierStage;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.logging.LogManager;

/**
 * The Class QualityClassifierApplication.
 *
 * @author M1048358 Alok Ranjan
 */
public class QualityClassifierApplication {

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        configureJulToSlf4j();
        removeBraveConsoleHandlers();
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.scan("io.mosip.registration.processor.quality.classifier.config",
                "io.mosip.kernel.packetmanager.config",
                "io.mosip.registration.processor.status.config", "io.mosip.registration.processor.rest.client.config",
                "io.mosip.registration.processor.packet.storage.config", "io.mosip.registration.processor.core.config",
                "io.mosip.registration.processor.core.kernel.beans",
                "io.mosip.registration.processor.packet.manager.config");
        ctx.refresh();
        QualityClassifierStage qualityClassifierStage = ctx.getBean(QualityClassifierStage.class);
        qualityClassifierStage.deployVerticle();
    }

    private static void configureJulToSlf4j() {
        // Remove all existing handlers from root JUL logger
        java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
        for (java.util.logging.Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        // Reset JUL configuration and install SLF4J bridge
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    private static void removeBraveConsoleHandlers() {
        java.util.logging.Logger braveLogger =
                java.util.logging.Logger.getLogger("brave.Tracing");

        for (java.util.logging.Handler handler : braveLogger.getHandlers()) {
            braveLogger.removeHandler(handler);
        }

        // Don't send Brave logs to JUL's parent handlers
        braveLogger.setUseParentHandlers(false);
    }

}
