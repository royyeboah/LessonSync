package com.adbdti.lessonsync.Controller;

import com.adbdti.lessonsync.Services.VertexAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
public class AIController {

    private final VertexAIService vertexAIService;

    @Autowired
    public AIController(VertexAIService vertexAIService) {
        this.vertexAIService = vertexAIService;
    }

    @GetMapping("/")
    public String getAnswers(@RequestParam("file") MultipartFile file){

        return vertexAIService.generateContent(file);
    }

    @GetMapping("/debug")
    public String getDebugInfo(){
        return vertexAIService.getRawResponse();
    }
}
