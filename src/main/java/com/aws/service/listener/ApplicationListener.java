package com.aws.service.listener;

import com.aws.service.logging.Logger;
import io.sentry.Sentry;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@WebListener
public class ApplicationListener extends Logger implements ServletContextListener {

    private static String version;

    public ApplicationListener() throws IOException {
        InputStream in = getClass().getClassLoader().getResourceAsStream("../build.properties");
        Properties properties = new Properties();
        properties.load(in);
        version = properties.get("projectVersion").toString();

        FileInputStream fileProperties = readFileProperties("sentry.properties");

        if (fileProperties != null) {
            properties.load(fileProperties);

            Sentry.init(options -> {
                options.setDsn(properties.getProperty("dsn"));
                options.setEnvironment(properties.getProperty("environment"));
                options.setRelease(properties.getProperty("release"));
                options.setTracesSampleRate(Double.parseDouble(properties.getProperty("traces-sample-rate")));
                options.setDebug(Boolean.parseBoolean(properties.getProperty("debug")));
            });
        } else {
            Sentry.init();
        }
    }

    /**
     * Method used to read the properties file
     * @param fileName file name
     * @return properties file
     */
    public static FileInputStream readFileProperties(String fileName) {
        try {
            File configDir = new File(System.getProperty("catalina.home"), "config");
            File configFile = new File(configDir, fileName);
            return new FileInputStream(configFile);
        } catch (Exception e) {
            printInfo("Config file not found in tomcat", e.getMessage());
            return null;
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        printInfo("----------------------------------------------");
        printInfo("-- Start aws-service-hub API version " + version + " ---");
        printInfo("----------------------------------------------");
    }
}
