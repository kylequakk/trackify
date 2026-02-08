package com.caltracker.controller;

import com.caltracker.ai.aiResult;
import com.caltracker.model.Meal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class AnalyzeController {

    @PostMapping("/analyze")
    public Meal analyzeImage(@RequestParam("image") MultipartFile image) throws Exception {

        // Convert image to Base64
        String base64 = java.util.Base64.getEncoder().encodeToString(image.getBytes());

        // Call your AI service
        String json = aiResult.callVisionAPI(base64);

        // Convert JSON → Meal object
        return new com.google.gson.Gson().fromJson(json, Meal.class);
    }
}
