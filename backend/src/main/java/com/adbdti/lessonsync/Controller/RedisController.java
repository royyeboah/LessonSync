package com.adbdti.lessonsync.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/redis")
public class RedisController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String STRING_KEY_PREFIX = "redi2read:strings:";

    @PostMapping("/strings")
    @ResponseStatus(HttpStatus.CREATED)
    public Map.Entry<String, String> setString(@RequestBody Map.Entry<String, String> kvp) {

        redisTemplate.opsForValue().set(STRING_KEY_PREFIX +kvp.getKey(), kvp.getValue());
        return kvp;
    }

}
