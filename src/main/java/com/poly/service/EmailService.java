package com.poly.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sibModel.CreateSmtpEmail;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailSender;
import sibModel.SendSmtpEmailTo;
import sibApi.TransactionalEmailsApi;
import sendinblue.ApiClient; // Import ApiClient
import sendinblue.ApiException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j(topic = "EMAIL-SERVICE")
public class EmailService {

    private final ApiClient apiClient;

    @Value("${spring.brevo.fromEmail}")
    private String fromEmail;

    @Value("${spring.brevo.fromName}")
    private String fromName;

    @Value("${spring.brevo.templateId}")
    private Long templateId;

    @Value("${spring.brevo.verificationLink}")
    private String verificationLink;

    public EmailService(@Value("${spring.brevo.apiKey}") String apiKey) {
        this.apiClient = new ApiClient();
        this.apiClient.setApiKey(apiKey);
    }

    /**
     * Send simple email with Brevo
     *
     * @param to
     * @param subject
     * @param text
     */
    public void send(String to, String subject, String text) {
        SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
        sendSmtpEmail.setSender(new SendSmtpEmailSender().email(fromEmail).name(fromName));
        sendSmtpEmail.setTo(List.of(new SendSmtpEmailTo().email(to)));
        sendSmtpEmail.setSubject(subject);
        sendSmtpEmail.setHtmlContent(text);

        TransactionalEmailsApi apiInstance = new TransactionalEmailsApi(apiClient);
        try {
            CreateSmtpEmail result = apiInstance.sendTransacEmail(sendSmtpEmail);
            log.info("Email sent successfully. Message ID: {}", result.getMessageId());
        } catch (ApiException e) {
            log.error("Email sent failed: {}", e.getResponseBody(), e);
        }
    }

    /**
     * Send email verification with Brevo template
     *
     * @param to
     * @param name
     */
    public void sendVerificationEmail(String to, String name, String secretCode) throws IOException {
        log.info("Sending verification email for name={}", name);

        // Tạo dynamic template data
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("verification_link", verificationLink + "?secretCode=" + secretCode); // Include secretCode in verification link

        SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
        sendSmtpEmail.setSender(new SendSmtpEmailSender().email(fromEmail).name(fromName));
        sendSmtpEmail.setTo(List.of(new SendSmtpEmailTo().email(to)));
        sendSmtpEmail.setTemplateId(templateId);
        sendSmtpEmail.setParams(params);

        TransactionalEmailsApi apiInstance = new TransactionalEmailsApi(apiClient);
        try {
            CreateSmtpEmail result = apiInstance.sendTransacEmail(sendSmtpEmail);
            log.info("Verification email sent successfully. Message ID: {}", result.getMessageId());
        } catch (ApiException e) {
            log.error("Verification email sent failed: {}", e.getResponseBody(), e);
        }
    }
}