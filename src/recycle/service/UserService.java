package com.caltracker.service;

import com.caltracker.model.User;
import com.caltracker.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public String signup(User user) {
        if (repo.findByEmail(user.getEmail()) != null) {
            return "Email already exists";
        }

        user.setPassword(encoder.encode(user.getPassword()));
        repo.save(user);

        return "Account created";
    }

    public String login(User user) {
        User dbUser = repo.findByEmail(user.getEmail());

        if (dbUser == null) return "User not found";
        if (!encoder.matches(user.getPassword(), dbUser.getPassword())) return "Wrong password";

        return "Login successful";
    }
}
