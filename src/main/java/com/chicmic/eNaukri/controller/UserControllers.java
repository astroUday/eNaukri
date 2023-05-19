package com.chicmic.eNaukri.controller;
import com.chicmic.eNaukri.Dto.UsersDto;
import com.chicmic.eNaukri.service.PasswordResetService;
import com.chicmic.eNaukri.service.UsersService;
import com.chicmic.eNaukri.model.PasswordResetToken;
import com.chicmic.eNaukri.model.Users;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
public class UserControllers {
    private Logger logger= LoggerFactory.getLogger(UserControllers.class);
    public static final String imagePath= "/home/chicmic/Downloads/JobPortal/src/main/resources/static/assets/img/";
    public static final String resumePath = "/home/chicmic/Downloads/JobPortal/src/main/resources/static/assets/files/";
    @Autowired
    UsersService usersService;
    @Autowired
    PasswordResetService passwordResetService;
    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public String register(UsersDto dto,@RequestParam("imgUrl") MultipartFile imgFile,
                           @RequestParam("resumeUrl") MultipartFile resumeFile) throws IOException {
        String userToken=UUID.randomUUID().toString();
        String fullName=dto.getFullName();
        String email= dto.getEmail();
        String phone=dto.getPhoneNumber();
        String password=dto.getPassword();
        String bio=dto.getBio();
        String imgFolder = imagePath;
        String resumeFolder=resumePath;
        System.out.println(imagePath);
        Users user=Users.builder()
                .fullName(fullName)
                .email(email)
                .phoneNumber(phone)
                .password(passwordEncoder.encode(password))
                .uuid(userToken)
                .bio(bio)
                .enableNotification(true)
                .build();
        if(!imgFile.isEmpty()&&!resumeFile.isEmpty()){
            byte[] imgFileBytes = imgFile.getBytes();
            byte[] resumeFileBytes= resumeFile.getBytes();
            Path imgPath = Paths.get(imgFolder +  imgFile.getOriginalFilename());
            Path resumePath=Paths.get(resumeFolder+resumeFile.getOriginalFilename());
            logger.info(imgPath.toString()+resumePath.toString());
            Files.write(imgPath, imgFileBytes);
            Files.write(resumePath,resumeFileBytes);
            user=Users.builder()
                .ppPath("/static/assets/img" +imgFile.getOriginalFilename())
                    .cvPath("/static/assets/files" +resumeFile.getOriginalFilename()).build();
        }
        usersService.saveUser(user);
        usersService.register(user);
        System.out.println("lalalalalala");

        return "";
    }
    @PostMapping("/set-new-password")
    public void setPassword(HttpServletRequest request) throws MessagingException, UnsupportedEncodingException {
        String email = request.getParameter("email");
        Users user = usersService.getUserByEmail(email);
        //String resetPasswordLink="http://localhost:8081/reset-email";
        passwordResetService.createPasswordResetTokenForUser(user);
    }
    @GetMapping("/enterNewPassword/{token}/{uuid}")
    public String Enter(HttpServletRequest
                                request, @PathVariable("token") String token, @PathVariable("uuid") String uuid, Model model) {
        //String newPassword=request.getParameter("password");
        PasswordResetToken passwordResetRequest = passwordResetService.findByToken(token);
//        User user= passwordResetRequest.getUser();
        if (passwordResetRequest == null || passwordResetRequest.getExpiryDate().isBefore(LocalDateTime.now())) {
            return "redirect:/login?error=InvalidToken";
        }
        model.addAttribute("token", token);
        return "forgotPasswordForm";
    }
    @PostMapping("/enterNewPassword/{token}/{uuid}")
    public String resetPassword(HttpServletRequest
                                        request,@PathVariable("token") String token,@PathVariable("uuid") String uuid){
        PasswordResetToken passwordResetRequest = passwordResetService.findByToken(token);
        Users user = usersService.getUserByUuid(uuid);
        System.out.println(user);
        String newPassword=request.getParameter("password");
        if (passwordResetRequest == null || passwordResetRequest.getExpiryDate().isBefore(LocalDateTime.now())) {
            return "redirect:/login?error=InvalidToken";
        }
        user.setPassword(passwordEncoder.encode(newPassword));
//        user.setPassword(newPassword);
        usersService.saveUser(user);
        passwordResetService.delete(passwordResetRequest);
        //return "redirect:/login?success=PasswordReset";
        return "user-login";
    }
    @PostMapping("/updateProfile")
    public void updateProfile(UsersDto user, @RequestParam("resumeFile")MultipartFile resumeFile, @RequestParam("imgFile")MultipartFile imgFile) throws IOException {
        usersService.updateUser(user,imgFile,resumeFile);
    }
}