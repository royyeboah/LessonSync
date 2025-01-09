package com.adbdti.lessonsync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class LessonSyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(LessonSyncApplication.class, args);
    }


}
