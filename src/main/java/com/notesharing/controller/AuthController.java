package com.notesharing.controller;

import com.notesharing.model.User;
import com.notesharing.repository.UserRepository;
import com.notesharing.service.EmailService;
import com.notesharing.service.OtpService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpService otpService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, 
                        HttpSession session, Model model) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null && user.getPassword().equals(password)) {
            session.setAttribute("user", user);
            return "redirect:/dashboard";
        }
        model.addAttribute("error", "Invalid email or password");
        return "login";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, Model model) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            model.addAttribute("error", "Email not registered.");
            return "forgot-password";
        }

        String otp = otpService.generateOtp(email);
        try {
            emailService.sendOtpEmail(email, otp);
            model.addAttribute("success", "OTP sent to your email.");
            model.addAttribute("email", email);
            return "reset-password";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to send email. Please try again.");
            return "forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String email, 
                                       @RequestParam String otp, 
                                       @RequestParam String newPassword, 
                                       Model model) {
        if (otpService.validateOtp(email, otp)) {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                user.setPassword(newPassword);
                userRepository.save(user);
                model.addAttribute("success", "Password reset successfully. Please login.");
                return "login";
            }
        }
        model.addAttribute("error", "Invalid or expired OTP.");
        model.addAttribute("email", email);
        return "reset-password";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String name, @RequestParam String email, 
                           @RequestParam String password, @RequestParam String university, 
                           Model model) {
        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Email already exists");
            return "register";
        }
        User user = new User(UUID.randomUUID().toString(), name, email, password, university);
        userRepository.save(user);
        model.addAttribute("success", "Registration successful. Please login.");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/delete-account")
    public String deleteAccount(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            userRepository.deleteById(user.getId());
            session.invalidate();
        }
        return "redirect:/login";
    }
}
