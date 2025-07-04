package com.nadir.userAuth.controller;

import com.nadir.userAuth.model.User;
import org.springframework.security.core.Authentication;

import com.nadir.userAuth.model.VerificationToken;
import com.nadir.userAuth.repository.UserRepository;
import com.nadir.userAuth.repository.VerificationTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
//import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Controller
public class AuthController {
	@Value("${base.url}")
	private String baseUrl;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository tokenRepository;
//    @Autowired
//    private VerificationTokenRepository verificationTokenRepository;


    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;
    
//    @Autowired
//    VerificationToken verificationToken;

    // ✅ Home page (default route)
    @GetMapping({"/", "/home"})
    public String home() {
        return "home";
    }

    // ✅ Show login form
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    // ✅ Handle login POST manually
    @PostMapping("/login")
    public String loginUser(@RequestParam String email,
                            @RequestParam String password,
                            Model model) {
        User user = userRepository.findByEmail(email);
        if (user == null || !user.isEnabled()) {
            model.addAttribute("error", "Invalid credentials or email not verified");
            return "login";
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));
            if (authentication.isAuthenticated()) {
                return "redirect:/welcome";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Invalid credentials");
        }

        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User()); // Important for form binding
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user,
                               HttpServletRequest request,
                               Model model) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            model.addAttribute("error", "Email already exists");
            model.addAttribute("user", user);
            return "register";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(false);
        userRepository.save(user);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(
                token,
                user,
                LocalDateTime.now().plusMinutes(15)
        );
        tokenRepository.save(verificationToken);

        String verificationUrl = request.getScheme() + "://" + request.getServerName() + ":" +
                request.getServerPort() + "/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Email Verification");
        message.setText("Click this link to verify your account: " + verificationUrl);
        mailSender.send(message);

        model.addAttribute("message", "Verification link has been sent to your email.");
        model.addAttribute("user", new User()); // Reset the form
        return "register";
    }

    // ✅ Verify email
    @GetMapping("/verify")
    public String verifyEmail(@RequestParam("token") String token, Model model) {
        Optional<VerificationToken> optional = tokenRepository.findByToken(token);
        if (optional.isEmpty()) {
            model.addAttribute("error", "Invalid token");
            return "login";
        }

        VerificationToken verificationToken = optional.get();
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "Token expired");
            return "login";
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        tokenRepository.delete(verificationToken);

        model.addAttribute("message", "Email verified. You can now log in.");
        return "login";
    }

    // ✅ Show forgot password form
    @GetMapping("/forgot-password")
    public String showForgotForm() {
        return "forgot-password";
    }
    
     
    @PostMapping("/forgot-password")
    public String processForgot(@RequestParam("email") String email, Model model) {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            // Even if the user doesn't exist, we *can* show a generic message for security.
            model.addAttribute("error", "No user found with this email.");
            return "forgot-password";
        }

        Optional<VerificationToken> existingTokenOpt = tokenRepository.findByUser(user);

        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(15);

        VerificationToken verificationToken;
        if (existingTokenOpt.isPresent()) {
            verificationToken = existingTokenOpt.get();
            verificationToken.setToken(token);
            verificationToken.setExpiryDate(expiry);
        } else {
            verificationToken = new VerificationToken(token, user, expiry);
        }

        tokenRepository.save(verificationToken);

        // ✅ Send password reset mail
        String resetUrl = baseUrl + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Password Reset Request");
        message.setText("Click the link to reset your password: " + resetUrl);
        mailSender.send(message);

        model.addAttribute("message", "Password reset email has been sent to your registered email address.");
        return "forgot-password";
    }





    // ✅ Show reset password form
    @GetMapping("/reset-password")
    public String showResetForm(@RequestParam("token") String token, Model model) {
        Optional<VerificationToken> opt = tokenRepository.findByToken(token);
        if (opt.isEmpty() || opt.get().getExpiryDate().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "Invalid or expired token");
            return "reset-password";
        }

        model.addAttribute("token", token);
        return "reset-password";
    }

    // ✅ Handle reset password
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                                @RequestParam String password,
                                Model model) {
        Optional<VerificationToken> opt = tokenRepository.findByToken(token);
        if (opt.isEmpty()) {
            model.addAttribute("error", "Invalid token");
            return "reset-password";
        }

        VerificationToken verificationToken = opt.get();
        User user = verificationToken.getUser();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        tokenRepository.delete(verificationToken);

        return "redirect:/login?resetSuccess";
    }

    // ✅ Welcome page (after login)
    @GetMapping("/welcome")
    public String showWelcome() {
        return "redirect:/dashboard";

    }
    
    
 // ✅ User Dashboard (after login)
    @GetMapping("/dashboard")
    public String userDashboard(Model model, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);

        if (user != null) {
            model.addAttribute("user", user);
        }

        return "dashboard";  
    }

}


//
//@Controller
//public class AuthController {
//
//    @Autowired
//    private UserRepository userRepo;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @Autowired
//    private EmailService emailService;
//
//    @Autowired
//    private VerificationTokenRepository tokenRepo;
//
//    // Home page
//    @GetMapping("/")
//    public String homePage() {
//        return "home";
//    }
//
//    // Show registration form
//    @GetMapping("/register")
//    public String showRegister(Model model) {
//        model.addAttribute("user", new User());
//        return "register";
//    }
//
//    // Handle registration
//    @PostMapping("/register")
//    public String registerUser(@ModelAttribute User user, Model model) {
//        if (userRepo.findByUsername(user.getUsername()) != null) {
//            model.addAttribute("msg", "Username already exists!");
//            return "register";
//        }
//        if (userRepo.findByEmail(user.getEmail()) != null) {
//            model.addAttribute("msg", "Email already registered!");
//            return "register";
//        }
//
//        user.setPassword(passwordEncoder.encode(user.getPassword()));
//        user.setEnabled(false); // Disable until verification
//        userRepo.save(user);
//
//        // Create verification token
//        String token = UUID.randomUUID().toString();
//        VerificationToken verificationToken = new VerificationToken(token, user);
//        tokenRepo.save(verificationToken);
//
//        // Send email
//        String link = "http://localhost:8080/verify?token=" + token;
//        emailService.sendVerificationEmail(user.getEmail(), token);
//
//        model.addAttribute("msg", "Registration successful! Please verify your email.");
//        return "login";
//    }
//
//    // Email verification
//    @GetMapping("/verify")
//    public String verifyUser(@RequestParam("token") String token, Model model) {
//    	VerificationToken verificationToken = tokenRepo.findByToken(token).orElse(null);
//
//        if (verificationToken == null) {
//            model.addAttribute("msg", "Invalid verification token!");
//            return "login";
//        }
//
//        User user = verificationToken.getUser();
//        user.setEnabled(true);
//        userRepo.save(user);
//        tokenRepo.delete(verificationToken);
//
//        model.addAttribute("msg", "Account verified! You can now login.");
//        return "login";
//    }
//
//    // Show login page
//    @GetMapping("/login")
//    public String showLogin() {
//        return "login";
//    }
//
//    // Welcome page after login
//    @GetMapping("/welcome")
//    public String welcome(Model model, Principal principal) {
//        if (principal != null) {
//            model.addAttribute("name", principal.getName());
//        } else {
//            model.addAttribute("name", "Guest");
//        }
//        return "welcome";
//    }
//
//    // Show forgot password page
//    @GetMapping("/forgot-password")
//    public String forgotPasswordForm(Model model) {
//        model.addAttribute("user", new User());
//        return "forgot-password";
//    }
//
//    // Process forgot password
//    @PostMapping("/forgot-password")
//    public String processForgotPassword(@ModelAttribute User user, HttpSession session, Model model) {
//        User dbUser = userRepo.findByEmail(user.getEmail());
//        if (dbUser != null) {
//            session.setAttribute("resetUserEmail", dbUser.getEmail());
//            return "redirect:/reset-password";
//        } else {
//            model.addAttribute("error", "No user found with this email.");
//            return "forgot-password";
//        }
//    }
//
//    // Show reset password form
//    @GetMapping("/reset-password")
//    public String resetPasswordForm(Model model, HttpSession session) {
//        String email = (String) session.getAttribute("resetUserEmail");
//        if (email == null) {
//            return "redirect:/forgot-password";
//        }
//        model.addAttribute("email", email);
//        return "reset-password";
//    }
//
//    // Process reset password
//    @PostMapping("/reset-password")
//    public String resetPassword(@RequestParam("email") String email,
//                                @RequestParam("newPassword") String newPassword,
//                                HttpSession session) {
//        User dbUser = userRepo.findByEmail(email);
//        if (dbUser != null) {
//            dbUser.setPassword(passwordEncoder.encode(newPassword));
//            userRepo.save(dbUser);
//            session.removeAttribute("resetUserEmail");
//            return "redirect:/login?resetSuccess=true";
//        } else {
//            return "redirect:/reset-password?error=true";
//        }
//    }
//
//    // Logout
//    @GetMapping("/logout")
//    public String logout(HttpSession session) {
//        session.invalidate();
//        return "redirect:/login?logout=true";
//    }
//}
