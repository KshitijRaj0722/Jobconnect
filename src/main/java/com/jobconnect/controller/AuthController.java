package com.jobconnect.controller;

import com.jobconnect.dto.UserRegisterDto;
import com.jobconnect.model.Role;
import com.jobconnect.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegisterDto());
        model.addAttribute("roles", Role.values());
        return "register";
    }

    @PostMapping("/register")
    public String registerUserAccount(@Valid @ModelAttribute("user") UserRegisterDto registrationDto,
                                      BindingResult result,
                                      Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "register";
        }

        try {
            userService.registerUser(registrationDto);
        } catch (IllegalArgumentException e) {
            result.rejectValue("email", "error.user", e.getMessage());
            model.addAttribute("roles", Role.values());
            return "register";
        }

        return "redirect:/login?success";
    }
}
