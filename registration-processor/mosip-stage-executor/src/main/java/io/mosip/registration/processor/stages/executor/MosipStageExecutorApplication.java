package io.mosip.registration.processor.stages.executor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.mosip.registration.processor.core.logger.RegProcessorLogger;
import io.mosip.kernel.core.logger.spi.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.slf4j.bridge.SLF4JBridgeHandler;


import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.registration.processor.core.abstractverticle.MosipVerticleAPIManager;

import io.mosip.registration.processor.stages.executor.config.StagesConfig;
import io.mosip.registration.processor.stages.executor.util.StageClassesUtil;
import java.util.logging.LogManager;

/**
 * The Class MosipStageExecutorApplication.
 */
public class MosipStageExecutorApplication {
	
	/** The Constant regProcLogger. */
	private static final Logger regProcLogger = RegProcessorLogger.getLogger(MosipStageExecutorApplication.class);

	/**
	 * main method to launch external stage application.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		regProcLogger.info("Starting mosip-stage-executor...");
		LogManager.getLogManager().reset();
		SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
		//This context is closed after deploying the stages
		try (AnnotationConfigApplicationContext stageInfoApplicationContext = new AnnotationConfigApplicationContext(
				new Class<?>[] { StagesConfig.class });) {
			StagesConfig stagesConfig = stageInfoApplicationContext.getBean(StagesConfig.class);
			MutablePropertySources propertySources = stagesConfig.getCloudPropertySources();
			
			List<String> stageBeansBasePackages = StageClassesUtil.getStageBeansBasePackages(stagesConfig, propertySources);
			
			if(!stageBeansBasePackages.isEmpty()) {
				
				regProcLogger.info("Base packages for stage beans from configuration: {}", stageBeansBasePackages);
				
				List<Class<MosipVerticleAPIManager>> stageClasses = StageClassesUtil.getStageBeanClasses(stageBeansBasePackages);
				
				regProcLogger.info("Stage classes identified: {}", stageClasses.stream().map(Class::getCanonicalName).collect(Collectors.joining(", ")));
	
				Class<?>[] entrypointConfigClasses = Stream.concat(Stream.of(StagesConfig.class), stageClasses.stream())
						.toArray(size -> new Class<?>[size]);
	
				//This context should not be closed and to be kept for consumption by the verticles
				AnnotationConfigApplicationContext mainApplicationContext = new PropertySourcesCustomizingApplicationContext(
						entrypointConfigClasses) {
							@Override
							public MutablePropertySources getPropertySources() {
								return propertySources;
							};
						};
				
				if (!stageClasses.isEmpty()) {
					ExecutorService executorService = Executors.newFixedThreadPool(stageClasses.size());
					stageClasses.forEach(stageClass -> executorService.execute(() -> {
						try {
							regProcLogger.info("Executing Stage: {}", stageClass.getCanonicalName());
							MosipVerticleAPIManager stageBean = StageClassesUtil.getStageBean(mainApplicationContext, stageClass);
							stageBean.deployVerticle();
						} catch (Exception e) {
							regProcLogger.error("Exception occured while loading verticles. Please make sure correct verticle name was passed from deployment script. \n {}",
									ExceptionUtils.getStackTrace(e));
						}
					}));
					executorService.close();
				} else {
					regProcLogger.error("No stage class is found. Please make sure correct correct stage class base packages are specified in properties and stages are added to classpath/dependencies.");
				}
			} else {
				regProcLogger.error("No base packages configured for stages.");
			}
		}
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



