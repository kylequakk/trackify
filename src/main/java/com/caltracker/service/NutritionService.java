package com.caltracker.service;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.google.genai.types.GenerateContentResponse;
import com.caltracker.model.Meal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Date;

import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NutritionService {

    // Use the same property name you use elsewhere (change if your property differs)
    @Value("${gemini.api.key}")
    private String apiKey;

    // Keep the signature your controller expects
    public Meal analyzeFoodImage(byte[] imageBytes) {
        try (Client client = Client.builder().apiKey(apiKey).build()) {

            // Build prompt and parts
            String promptText = "Identify food and calories. Return EXACTLY: Name: [name], Calories: [number]. "
                    + "Return only that line, no extra commentary.";

            Part textPart = Part.builder().text(promptText).build();

            // The SDK may accept raw bytes or base64 depending on version.
            // Here we send base64 string inside inlineData.data to match many SDK examples.
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            Part imagePart = Part.builder()
                    .inlineData(Part.InlineData.builder()
                            .mimeType("image/jpeg")
                            .data(base64)
                            .build())
                    .build();

            Content content = Content.builder().parts(List.of(textPart, imagePart)).build();

            GenerateContentResponse response = client.getModels().generateContent(
                    "gemini-1.5-flash",
                    List.of(content),
                    null
            );

            // Defensive checks on response structure
            if (response == null
                    || response.getCandidates() == null
                    || response.getCandidates().isEmpty()) {
                throw new RuntimeException("Empty response from Gemini");
            }

            var candidate = response.getCandidates().get(0);
            if (candidate.getContent() == null
                    || candidate.getContent().getParts() == null
                    || candidate.getContent().getParts().isEmpty()) {
                throw new RuntimeException("Malformed Gemini response: missing content/parts");
            }

            String result = candidate.getContent().getParts().get(0).getText();
            if (result == null || result.isBlank()) {
                throw new RuntimeException("Gemini returned empty text");
            }

            // Robust extraction using regex (handles small variations and whitespace)
            String name = extractField(result, "Name");
            Integer calories = extractIntField(result, "Calories");

            if (name == null || calories == null) {
                throw new RuntimeException("Failed to extract name or calories from AI response: " + result);
            }

            // Build Meal defensively (use setters so constructor differences don't break you)
            Meal meal = new Meal();
            meal.setMealName(name);
            meal.setCalories(calories);
            meal.setUserName("unknown");
            meal.setProteins(0);
            meal.setCarbohydrates(0);
            meal.setFibers(0);
            meal.setFats(0);
            meal.setSugars(0);
            meal.setTimeStamp(new Date());

            return meal;

        } catch (RuntimeException re) {
            // rethrow runtime exceptions as-is so controller can map them to 4xx/5xx
            throw re;
        } catch (Exception e) {
            // Wrap checked exceptions with a clear runtime exception
            throw new RuntimeException("NutritionService failed: " + e.getMessage(), e);
        }
    }

    // Extract a string field like "Name: Burrito" robustly
    private static String extractField(String text, String fieldName) {
        Pattern p = Pattern.compile(fieldName + "\\s*:\\s*([^,\\n]+)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        if (m.find()) {
            return m.group(1).trim();
        }
        return null;
    }

    // Extract an integer field like "Calories: 720" robustly
    private static Integer extractIntField(String text, String fieldName) {
        Pattern p = Pattern.compile(fieldName + "\\s*:\\s*([0-9]+)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }
}
