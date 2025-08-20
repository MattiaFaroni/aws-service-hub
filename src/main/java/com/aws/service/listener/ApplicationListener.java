package com.aws.service.listener;

import com.aws.service.tools.time.Timestamp;
import io.sentry.Sentry;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WebListener
public class ApplicationListener implements ServletContextListener {

    private String version = "unknown";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        initializeApplication(sce);

        log.info("==============================================");
        log.info("Starting aws-service-hub application");
        log.info("Version: {}", version);
        log.info("Timestamp: {}", new Timestamp());
        log.info("==============================================");
    }

    // spotless:off
    /**
     * Initializes the application configurations, including loading build properties,
     * initializing Sentry configurations, and setting up related resources.
     *
     * @param sce The ServletContextEvent containing the servlet context.
     */
    private void initializeApplication(ServletContextEvent sce) {
        Properties buildProperties = new Properties();
        Properties sentryProperties = new Properties();

        try (InputStream in = sce.getServletContext().getResourceAsStream("/WEB-INF/build.properties")) {
            if (in != null) {
                buildProperties.load(in);
                version = buildProperties.getProperty("projectVersion", "unknown");
            } else {
                log.warn("File build.properties not found in classpath");
            }
        } catch (Exception e) {
            log.error("Error reading file build.properties", e);
        }

        String sentryConfigPath = System.getProperty("sentry.config.path");

        InputStream sentryInputStream = null;
        try {
            if (sentryConfigPath != null) {
                File sentryFile = new File(sentryConfigPath);
                if (sentryFile.exists() && sentryFile.isFile()) {
                    sentryInputStream = new FileInputStream(sentryFile);
                } else {
                    log.warn("The sentry.properties file specified in sentry.config.path does not exist: {}", sentryConfigPath);
                }
            }

            if (sentryInputStream == null) {
                sentryInputStream = getClass().getClassLoader().getResourceAsStream("sentry.properties");
                if (sentryInputStream == null) {
                    log.warn("File sentry.properties not found in classpath");
                }
            }

            if (sentryInputStream != null) {
                sentryProperties.load(sentryInputStream);
                Sentry.init(options -> {
                    options.setDsn(sentryProperties.getProperty("dsn"));
                    options.setEnvironment(sentryProperties.getProperty("environment"));
                    options.setRelease(sentryProperties.getProperty("release"));
                    options.setTracesSampleRate(Double.parseDouble(sentryProperties.getProperty("traces-sample-rate", "1.0")));
                    options.setDebug(Boolean.parseBoolean(sentryProperties.getProperty("debug", "false")));
                });
            } else {
                log.warn("No sentry.properties configuration found. Initializing Sentry with default configuration.");
                Sentry.init();
            }

        } catch (Exception e) {
            log.error("Error initializing Sentry.", e);
            Sentry.init();
        } finally {
            if (sentryInputStream != null) {
                try {
                    sentryInputStream.close();
                } catch (Exception e) {
                    log.warn("Error closing InputStream of sentry.properties", e);
                }
            }
        }
    }
    // spotless:on
}
