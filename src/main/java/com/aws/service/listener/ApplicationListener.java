package com.aws.service.listener;

import io.sentry.Sentry;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class ApplicationListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationListener.class);
    private String version = "unknown";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("----------------------------------------------");
        initializeApplication();
        logger.info("-- Start aws-service-hub API version {} ---", version);
        logger.info("----------------------------------------------");
    }

    /**
     * Initializes the application by loading configuration files.
     */
    private void initializeApplication() {
        Properties properties = new Properties();

        try (InputStream in = getClass().getClassLoader().getResourceAsStream("../build.properties")) {
            properties.load(in);
            version = properties.getProperty("projectVersion", "unknown");
        } catch (Exception e) {
            logger.error("Error reading file build.properties", e);
        }

        File sentryFile = new File(System.getProperty("catalina.home"), "config/sentry.properties");
        try (FileInputStream fis = new FileInputStream(sentryFile)) {
            properties.load(fis);
            Sentry.init(options -> {
                options.setDsn(properties.getProperty("dsn"));
                options.setEnvironment(properties.getProperty("environment"));
                options.setRelease(properties.getProperty("release"));
                options.setTracesSampleRate(Double.parseDouble(properties.getProperty("traces-sample-rate", "1.0")));
                options.setDebug(Boolean.parseBoolean(properties.getProperty("debug", "false")));
            });
        } catch (Exception e) {
            logger.error("Failed to initialize Sentry from sentry.properties. Using default Sentry config.", e);
            Sentry.init();
        }
    }
}
