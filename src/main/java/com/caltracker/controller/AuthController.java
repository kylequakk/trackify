package com.caltracker.controller;

import com.caltracker.model.User;
import com.caltracker.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService service;

    public AuthController(UserService service) {
        this.service = service;
    }

    @PostMapping("/signup")
    public String signup(@RequestBody User user) {
        return service.signup(user);
    }

    @PostMapping("/login")
    public String login(@RequestBody User user) {
        return service.login(user);
    }
}
