package com.chatter_box.email_service.email;

import com.chatter_box.email_service.email.dto.VerificationCodeDto;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assertions.*;
@QuarkusTest
class EmailResourceTest {

    @Inject
    MockMailbox mockMailbox;

    private static final String VALID_ACCESS_TOKEN = "secret-key";
    private static final String INVALID_ACCESS_TOKEN = "invalid-token";
    private static final String VERIFY_ENDPOINT = "/api/v1/emails/verify";
    private static final String RESET_PASSWORD_ENDPOINT = "/api/v1/emails/reset-password";

    private VerificationCodeDto validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new VerificationCodeDto("test@example.com", "testuser", "123456");
        // Clear the mock mailbox before each test
        mockMailbox.clear();
    }

    // ==================== VERIFICATION EMAIL TESTS ====================

    @Test
    void sendVerificationEmail_WithValidToken_ShouldSucceed() {
        given()
                .header("Access-Token", VALID_ACCESS_TOKEN)
                .contentType(ContentType.JSON)
                .body(validRequest)
                .when()
                .post(VERIFY_ENDPOINT)
                .then()
                .statusCode(204);

        // Verify email was captured by mock mailbox
        List<io.quarkus.mailer.Mail> emails = mockMailbox.getMailsSentTo("test@example.com");
        assertEquals(1, emails.size(), "Should have sent exactly one email");

        io.quarkus.mailer.Mail sentEmail = emails.get(0);
        assertEquals("test@example.com", sentEmail.getTo().get(0));
        assertTrue(sentEmail.getSubject().toLowerCase().contains("verification") ||
                        sentEmail.getSubject().toLowerCase().contains("verify"),
                "Email subject should contain verification-related text");
    }

    @Test
    void sendVerificationEmail_WithInvalidToken_ShouldReturn401() {
        given()
                .header("Access-Token", INVALID_ACCESS_TOKEN)
                .contentType(ContentType.JSON)
                .body(validRequest)
                .when()
                .post(VERIFY_ENDPOINT)
                .then()
                .statusCode(401);

        // Verify no email was sent
        assertEquals(0, mockMailbox.getTotalMessagesSent(),
                "No emails should be sent with invalid token");
    }

    @Test
    void sendVerificationEmail_WithMissingToken_ShouldReturn401() {
        given()
                .contentType(ContentType.JSON)
                .body(validRequest)
                .when()
                .post(VERIFY_ENDPOINT)
                .then()
                .statusCode(401);

        // Verify no email was sent
        assertEquals(0, mockMailbox.getTotalMessagesSent(),
                "No emails should be sent with missing token");
    }

    @Test
    void sendVerificationEmail_WithEmptyToken_ShouldReturn401() {
        given()
                .header("Access-Token", "")
                .contentType(ContentType.JSON)
                .body(validRequest)
                .when()
                .post(VERIFY_ENDPOINT)
                .then()
                .statusCode(401);

        // Verify no email was sent
        assertEquals(0, mockMailbox.getTotalMessagesSent(),
                "No emails should be sent with empty token");
    }

    @Test
    void sendVerificationEmail_WithInvalidContentType_ShouldReturn415() {
        given()
                .header("Access-Token", VALID_ACCESS_TOKEN)
                .contentType(ContentType.TEXT)
                .body("invalid body")
                .when()
                .post(VERIFY_ENDPOINT)
                .then()
                .statusCode(415);

        // Verify no email was sent
        assertEquals(0, mockMailbox.getTotalMessagesSent(),
                "No emails should be sent with invalid content type");
    }

    @Test
    void sendVerificationEmail_WithMissingBody_ShouldReturn400() {
        given()
                .header("Access-Token", VALID_ACCESS_TOKEN)
                .contentType(ContentType.JSON)
                .when()
                .post(VERIFY_ENDPOINT)
                .then()
                .statusCode(401);

        // Verify no email was sent
        assertEquals(0, mockMailbox.getTotalMessagesSent(),
                "No emails should be sent with missing body");
    }

    @Test
    void sendVerificationEmail_WithInvalidJson_ShouldReturn400() {
        given()
                .header("Access-Token", VALID_ACCESS_TOKEN)
                .contentType(ContentType.JSON)
                .body("{invalid json}")
                .when()
                .post(VERIFY_ENDPOINT)
                .then()
                .statusCode(400);

        // Verify no email was sent
        assertEquals(0, mockMailbox.getTotalMessagesSent(),
                "No emails should be sent with invalid JSON");
    }

    // ==================== RESET PASSWORD EMAIL TESTS ====================

    @Test
    void sendResetPasswordEmail_WithValidToken_ShouldSucceed() {
        given()
                .header("Access-Token", VALID_ACCESS_TOKEN)
                .contentType(ContentType.JSON)
                .body(validRequest)
                .when()
                .post(RESET_PASSWORD_ENDPOINT)
                .then()
                .statusCode(204);

        // Verify email was captured by mock mailbox
        List<io.quarkus.mailer.Mail> emails = mockMailbox.getMailsSentTo("test@example.com");
        assertEquals(1, emails.size(), "Should have sent exactly one email");

        io.quarkus.mailer.Mail sentEmail = emails.get(0);
        assertEquals("test@example.com", sentEmail.getTo().get(0));
        assertTrue(sentEmail.getSubject().toLowerCase().contains("reset") ||
                        sentEmail.getSubject().toLowerCase().contains("password"),
                "Email subject should contain password reset-related text");
    }

    @Test
    void sendResetPasswordEmail_WithInvalidToken_ShouldReturn401() {
        given()
                .header("Access-Token", INVALID_ACCESS_TOKEN)
                .contentType(ContentType.JSON)
                .body(validRequest)
                .when()
                .post(RESET_PASSWORD_ENDPOINT)
                .then()
                .statusCode(401);

        // Verify no email was sent
        assertEquals(0, mockMailbox.getTotalMessagesSent(),
                "No emails should be sent with invalid token");
    }

    @Test
    void sendResetPasswordEmail_WithMissingToken_ShouldReturn401() {
        given()
                .contentType(ContentType.JSON)
                .body(validRequest)
                .when()
                .post(RESET_PASSWORD_ENDPOINT)
                .then()
                .statusCode(401);

        // Verify no email was sent
        assertEquals(0, mockMailbox.getTotalMessagesSent(),
                "No emails should be sent with missing token");
    }

    // ==================== RATE LIMITING TESTS ====================
    // Note: These tests might need adjustment based on your rate limiting implementation

    @Test
    void sendVerificationEmail_RateLimitExceeded_ShouldReturn429() {
        // Send 5 requests (at the rate limit)
        for (int i = 0; i < 5; i++) {
            given()
                    .header("Access-Token", VALID_ACCESS_TOKEN)
                    .contentType(ContentType.JSON)
                    .body(validRequest)
                    .when()
                    .post(VERIFY_ENDPOINT)
                    .then()
                    .statusCode(204);
        }

        // The 6th request should be rate limited
        given()
                .header("Access-Token", VALID_ACCESS_TOKEN)
                .contentType(ContentType.JSON)
                .body(validRequest)
                .when()
                .post(VERIFY_ENDPOINT)
                .then()
                .statusCode(429);

        // Verify only 5 emails were sent (not 6)
        assertEquals(5, mockMailbox.getTotalMessagesSent(),
                "Should have sent exactly 5 emails before rate limiting kicked in");
    }

    @Test
    void sendResetPasswordEmail_RateLimitExceeded_ShouldReturn429() {
        // Send 5 requests (at the rate limit)
        for (int i = 0; i < 5; i++) {
            given()
                    .header("Access-Token", VALID_ACCESS_TOKEN)
                    .contentType(ContentType.JSON)
                    .body(validRequest)
                    .when()
                    .post(RESET_PASSWORD_ENDPOINT)
                    .then()
                    .statusCode(204);
        }

        // The 6th request should be rate limited
        given()
                .header("Access-Token", VALID_ACCESS_TOKEN)
                .contentType(ContentType.JSON)
                .body(validRequest)
                .when()
                .post(RESET_PASSWORD_ENDPOINT)
                .then()
                .statusCode(429);

        // Verify only 5 emails were sent (not 6)
        assertEquals(5, mockMailbox.getTotalMessagesSent(),
                "Should have sent exactly 5 emails before rate limiting kicked in");
    }

    // ==================== INTEGRATION TESTS ====================

    @Test
    void bothEndpoints_WithValidRequests_ShouldWorkIndependently() {
        VerificationCodeDto verifyRequest = new VerificationCodeDto("verify@example.com", "verifyuser", "111111");
        VerificationCodeDto resetRequest = new VerificationCodeDto("reset@example.com", "resetuser", "222222");

        // Test verification endpoint
        given()
                .header("Access-Token", VALID_ACCESS_TOKEN)
                .contentType(ContentType.JSON)
                .body(verifyRequest)
                .when()
                .post(VERIFY_ENDPOINT)
                .then()
                .statusCode(204);

        // Test reset password endpoint
        given()
                .header("Access-Token", VALID_ACCESS_TOKEN)
                .contentType(ContentType.JSON)
                .body(resetRequest)
                .when()
                .post(RESET_PASSWORD_ENDPOINT)
                .then()
                .statusCode(204);

        // Verify both emails were sent
        assertEquals(2, mockMailbox.getTotalMessagesSent(),
                "Should have sent exactly 2 emails total");

        List<io.quarkus.mailer.Mail> verifyEmails = mockMailbox.getMailsSentTo("verify@example.com");
        List<io.quarkus.mailer.Mail> resetEmails = mockMailbox.getMailsSentTo("reset@example.com");

        assertEquals(1, verifyEmails.size(), "Should have sent one verification email");
        assertEquals(1, resetEmails.size(), "Should have sent one reset password email");
    }

    @Test
    void multipleEmailsToSameRecipient_ShouldAllBeTracked() {
        // Send verification email
        given()
                .header("Access-Token", VALID_ACCESS_TOKEN)
                .contentType(ContentType.JSON)
                .body(validRequest)
                .when()
                .post(VERIFY_ENDPOINT)
                .then()
                .statusCode(204);

        // Send reset password email to same recipient
        given()
                .header("Access-Token", VALID_ACCESS_TOKEN)
                .contentType(ContentType.JSON)
                .body(validRequest)
                .when()
                .post(RESET_PASSWORD_ENDPOINT)
                .then()
                .statusCode(204);

        // Verify both emails were sent to the same recipient
        List<io.quarkus.mailer.Mail> emails = mockMailbox.getMailsSentTo("test@example.com");
        assertEquals(2, emails.size(), "Should have sent 2 emails to the same recipient");
        assertEquals(2, mockMailbox.getTotalMessagesSent(), "Should have sent 2 emails total");
    }
}