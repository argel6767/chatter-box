package com.chat_room_app.auth;

import com.chat_room_app.auth.dtos.RegisterUserDto;
import com.chat_room_app.exceptions.custom_exceptions.BadRequest400Exception;
import com.chat_room_app.exceptions.custom_exceptions.Conflict409Exception;
import com.chat_room_app.users.UserRepository;
import lombok.extern.java.Log;
import org.apache.commons.validator.routines.EmailValidator;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log
public class AuthUtil {

    public static void verifyRegistrationDetails(RegisterUserDto request, UserRepository userRepository) {
        if (!isValidEmail(request.email())) {
            log.warning("Invalid email " + request.email());
            throw new BadRequest400Exception(request.email() + " is an invalid email");
        }
        if (!isValidUsername(request.username())) {
            log.warning("Invalid username " + request.username());
            throw new BadRequest400Exception(request.username() + " is an invalid username.");
        }
        if (!isValidPassword(request.password())) {
            log.warning("Invalid password " + request.password());
            throw new BadRequest400Exception(request.password() + " is an invalid password.");
        }
        if (userRepository.findByEmail(request.email()).isPresent()) {
            log.warning("User with email " + request.email() + " already exists");
            throw new Conflict409Exception(request.email() + " is already in use");
        }
        if (userRepository.findByUsername(request.username()).isPresent()) {
            log.warning("User with username " + request.username() + " already exists");
            throw new Conflict409Exception(request.username() + " is already in use");
        }
    }

    /**
     * Checks whether an email is a valid one utilizing
     * org.apache.commons.validator.routines EmailValidator class
     * @param email
     * @return
     */
    public static boolean isValidEmail(String email) {
        EmailValidator validator = EmailValidator.getInstance();
        return validator.isValid(email);
    }

    public static boolean isValidUsername(String username) {
        if(username == null || username.isEmpty()) {
            return false;
        }
        String regExpn = "^[a-zA-Z0-9_]+$";
        Pattern pattern = Pattern.compile(regExpn);
        Matcher matcher = pattern.matcher(username);
        return matcher.matches();
    }

    /**
     * This makes sure a password has the following characteristics:
     * (?=.*[a-z]): makes sure that there is at least one small letter
     * (?=.*[A-Z]): needs at least one capital letter
     * (?=.*\\d): requires at least one digit
     * (?=.*[@#$%^&+=!*?]): provides a guarantee of at least one special symbol
     * .{8,}: imposes the minimum length of 8 characters
     * @param password
     * @return
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        String regExpn = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!*?])(?=\\S+$).{8,}$";
        Pattern pattern = Pattern.compile(regExpn);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    /**
     * generates a random 6-digit number to be used as a verification code
     */
    public static String generateVerificationCode() {
        Random random = new Random();
        return String.valueOf(random.nextInt(900000) + 100000); //guaranteed 6-digit number
    }

    /**
     * generates a new verification code for user and sets it
     * @param authDetails
     * @return
     */
    public static String setVerificationCode(AuthDetails authDetails) {
        String verificationCode = generateVerificationCode();
        authDetails.setCodeExpiryTime(LocalDateTime.now().plusMinutes(30));
        authDetails.setVerificationCode(verificationCode);
        return verificationCode;
    }
}
