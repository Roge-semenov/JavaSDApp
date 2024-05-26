package unuversity.ImageGeneratorService.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import unuversity.ImageGeneratorService.model.User;
import unuversity.ImageGeneratorService.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/image")
public class ImageController {

    @Autowired
    private UserService userService;

    private RestTemplate restTemplate = new RestTemplate();
    private ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateImage(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        String username = request.get("username");

        User user = userService.findByUsername(username);
        if (user == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "User not found");
            return ResponseEntity.badRequest().body(response);
        }

        String url = "https://sinkin.ai/api/inference";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", "d80a33b42462421bb844a57e8933325b");
        params.add("model_id", "r2La2w2");
        params.add("prompt", prompt);
        params.add("width", "512");
        params.add("height", "768");
        params.add("negative_prompt", "(worst quality, low quality, normal quality, lowres, low details, oversaturated, undersaturated, overexposed, underexposed, grayscale, bw, bad photo, bad photography, bad art:1.4), (nude,naked, without clothes), (watermark, signature, text font, username, error, logo, words, letters, digits, autograph, trademark, name:1.2), (blur, blurry, grainy), morbid, ugly, asymmetrical, mutated malformed, mutilated, poorly lit, bad shadow, draft, cropped, out of frame, cut off, censored, jpeg artifacts, out of focus, glitch, duplicate, (airbrushed, cartoon, anime, semi-realistic, cgi, render, blender, digital art, manga, amateur:1.3), (3D ,3D Game, 3D Game Scene, 3D Character:1.1), (bad hands, bad anatomy, bad body, bad face, bad teeth, bad arms, bad legs, deformities:1.3), disfigured, kitsch, ugly, oversaturated, grain, low-res, Deformed, blurry, bad anatomy, disfigured, poorly drawn face, mutation, mutated, extra limb, ugly, poorly drawn hands, missing limb, blurry, floating limbs, disconnected limbs, malformed hands, blur, out of focus, long neck, long body, ugly, disgusting, poorly drawn, childish, mutilated, mangled, old, surreal, calligraphy, sign, writing, watermark, text, body out of frame, extra legs, extra arms, extra feet, out of frame, poorly drawn feet, cross-eye, blurry, bad anatomy");
        params.add("use_default_neg", "false");
        params.add("scale", "6.0");
        params.add("num_images", "1");
        params.add("scheduler", "K_EULER_ANCESTRAL");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            // Проверка на редирект
            if (response.getStatusCode().is3xxRedirection()) {
                String redirectUrl = response.getHeaders().getLocation().toString();
                if (!redirectUrl.startsWith("http")) {
                    redirectUrl = "https://sinkin.ai" + redirectUrl;
                }
                response = restTemplate.exchange(redirectUrl, HttpMethod.POST, requestEntity, String.class);
            }

            // Обработка успешного ответа
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                int errorCode = jsonResponse.get("error_code").asInt();
                if (errorCode == 0) {
                    JsonNode images = jsonResponse.get("images");
                    if (images.isArray() && images.size() > 0) {
                        String imageUrl = images.get(0).asText();
                        saveGeneratedImage(user, imageUrl);

                        Map<String, String> responseBody = new HashMap<>();
                        responseBody.put("imageUrl", imageUrl);
                        return ResponseEntity.ok(responseBody);
                    } else {
                        Map<String, String> responseBody = new HashMap<>();
                        responseBody.put("message", "No images found in the response.");
                        return ResponseEntity.badRequest().body(responseBody);
                    }
                } else {
                    Map<String, String> responseBody = new HashMap<>();
                    responseBody.put("message", "Error generating image: " + jsonResponse.get("message").asText());
                    return ResponseEntity.badRequest().body(responseBody);
                }
            } else {
                Map<String, String> responseBody = new HashMap<>();
                responseBody.put("message", "Error generating image: " + response.getStatusCode().toString());
                return ResponseEntity.badRequest().body(responseBody);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", "Error generating image: " + e.getMessage());
            return ResponseEntity.badRequest().body(responseBody);
        }
    }
    private void saveGeneratedImage(User user, String imageUrl) {
        List<String> images = user.getGeneratedImages();
        if (images == null) {
            images = new ArrayList<>();
        }
        if (images.size() >= 20) {
            images.remove(0); // Удаляем самое старое изображение, если их больше 20
        }
        images.add(imageUrl);
        user.setGeneratedImages(images);
        userService.saveUser(user);
    }

    @PostMapping("/user-images")
    public ResponseEntity<Map<String, Object>> getUserGeneratedImages(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        User user = userService.findByUsername(username);
        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User not found");
            return ResponseEntity.badRequest().body(response);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("images", user.getGeneratedImages());
        return ResponseEntity.ok(response);
    }
}
