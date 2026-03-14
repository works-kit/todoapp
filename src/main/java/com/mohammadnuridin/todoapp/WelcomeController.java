package com.mohammadnuridin.todoapp;

import com.mohammadnuridin.todoapp.core.response.ApiResponse;
import com.mohammadnuridin.todoapp.core.util.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WelcomeController {

    private final MessageService msg;

    // ── GET /api/ ─────────────────────────────────────────────
    @GetMapping("/")
    public ResponseEntity<ApiResponse<String>> hello() {
        return ResponseEntity.ok(
                ApiResponse.ok("success", "Hello, welcome to the Todolist API!"));
    }

    // ── GET /api/welcome ────────────────────────────────────────
    // Locale otomatis dari Accept-Language header via MessageService
    @GetMapping("/welcome")
    public ResponseEntity<ApiResponse<String>> welcome() {
        return ResponseEntity.ok(
                ApiResponse.ok("success", msg.get("welcome")));
    }

}