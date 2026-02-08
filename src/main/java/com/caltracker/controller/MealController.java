package com.caltracker.controller;

import com.caltracker.model.Meal;
import com.caltracker.repository.MealRepository;
import com.caltracker.service.NutritionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/meals")
@CrossOrigin(origins = "*")
public class MealController {

    private static final Logger logger = LoggerFactory.getLogger(MealController.class);

    private final NutritionService nutritionService;
    private final MealRepository mealRepository;

    public MealController(NutritionService nutritionService, MealRepository mealRepository) {
        this.nutritionService = nutritionService;
        this.mealRepository = mealRepository;
    }

    /**
     * Receives an image file, analyzes it, saves the resulting Meal, and returns the saved entity.
     * Expects multipart/form-data.
     */
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analyzeMeal(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing or empty file");
        }

        try {
            byte[] imageBytes = file.getBytes();

            // Defensive: ensure service exists
            if (nutritionService == null) {
                logger.error("NutritionService is not available");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server misconfiguration");
            }

            Meal meal = nutritionService.analyzeFoodImage(imageBytes);

            if (meal == null) {
                logger.warn("NutritionService returned null meal for uploaded image");
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("AI analysis returned no result");
            }

            if (mealRepository == null) {
                logger.error("MealRepository is not available");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server misconfiguration");
            }

            Meal saved = mealRepository.save(meal);
            return ResponseEntity.ok(saved);

        } catch (IOException e) {
            logger.error("Failed to read uploaded file", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to read uploaded file");
        } catch (Exception e) {
            logger.error("AI Analysis failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("AI Analysis failed");
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<Meal>> getMealHistory() {
        if (mealRepository == null) {
            logger.error("MealRepository is not available");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        List<Meal> all = mealRepository.findAll();
        return ResponseEntity.ok(all);
    }
}
