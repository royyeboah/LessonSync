package com.adbdti.lessonsync;

import com.adbdti.lessonsync.Config.GoogleCloudCredentials;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LessonSyncApplication {

    public static void main(String[] args) {
        GoogleCloudCredentials.installFromEnvironment();
        SpringApplication.run(LessonSyncApplication.class, args);
    }


}
