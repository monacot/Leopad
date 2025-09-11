package com.leopad.notepad.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Value("${sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from-email}")
    private String fromEmail;

    public void sendNoteByEmail(String toEmail, String noteTitle, String noteContent) {
        Email from = new Email(fromEmail);
        String subject = "Your Note: " + noteTitle;
        Email to = new Email(toEmail);
        
        String emailBody = buildEmailBody(noteTitle, noteContent);
        Content content = new Content("text/html", emailBody);
        
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sg.api(request);
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                logger.info("Email sent successfully to: {} with status code: {}", toEmail, response.getStatusCode());
            } else {
                logger.error("Failed to send email. Status code: {}, Response: {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("Failed to send email. Status code: " + response.getStatusCode());
            }
            
        } catch (IOException ex) {
            logger.error("Error sending email to {}: {}", toEmail, ex.getMessage());
            throw new RuntimeException("Failed to send email: " + ex.getMessage());
        }
    }

    private String buildEmailBody(String noteTitle, String noteContent) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Your Note</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #f8f9fa; padding: 20px; text-align: center; border-radius: 5px; }
                    .content { background-color: #ffffff; padding: 20px; border: 1px solid #dee2e6; border-radius: 5px; margin-top: 20px; }
                    .note-title { color: #007bff; font-size: 24px; font-weight: bold; margin-bottom: 15px; }
                    .note-content { font-size: 16px; white-space: pre-wrap; }
                    .footer { text-align: center; margin-top: 20px; color: #6c757d; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>📝 Your Note from Notepad App</h1>
                    </div>
                    <div class="content">
                        <div class="note-title">%s</div>
                        <div class="note-content">%s</div>
                    </div>
                    <div class="footer">
                        <p>This email was sent from your Notepad Application.</p>
                    </div>
                </div>
            </body>
            </html>
            """, noteTitle, noteContent);
    }
}