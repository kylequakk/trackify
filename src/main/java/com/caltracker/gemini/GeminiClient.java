package com.caltracker.gemini;

import com.caltracker.model.Meal;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GeminiClient {

    private final String apiKey;
    private final RestTemplate restTemplate = new RestTemplate();
    private final Gson gson = new Gson();

    public GeminiClient(@Value("${gemini.api.key}") String apiKey) {
        this.apiKey = apiKey;
    }

    public Meal analyzeMeal(String prompt) {
        try {
            String url =
                    "https://generativelanguage.googleapis.com/v1beta/models/"
                            + "gemini-1.5-flash:generateContent?key=" + apiKey;

            // Force Gemini to return JSON matching your Meal model
            String fullPrompt =
                    "You are a nutrition AI. Extract meal data from the following text and return ONLY valid JSON. "
                            + "Use these exact fields: userName, mealName, calories, proteins, carbohydrates, fibers, fats, sugars, timeStamp. "
                            + "Do not include explanations.\n\n"
                            + prompt;

            // Build request body
            JsonObject content = new JsonObject();
            content.addProperty("text", fullPrompt);

            JsonObject part = new JsonObject();
            part.add("text", content.get("text"));

            JsonObject contents = new JsonObject();
            contents.add("parts", gson.toJsonTree(new JsonObject[]{part}));

            JsonObject request = new JsonObject();
            request.add("contents", gson.toJsonTree(new JsonObject[]{contents}));

            // Send request
            String response = restTemplate.postForObject(url, request, String.class);

            // Parse AI response
            JsonObject root = JsonParser.parseString(response).getAsJsonObject();
            String aiText =
                    root.getAsJsonArray("candidates")
                            .get(0).getAsJsonObject()
                            .getAsJsonObject("content")
                            .getAsJsonArray("parts")
                            .get(0).getAsJsonObject()
                            .get("text").getAsString();

            // Convert JSON → Meal object
            return gson.fromJson(aiText, Meal.class);

        } catch (Exception e) {
            throw new RuntimeException("Gemini API error: " + e.getMessage());
        }
    }
}
