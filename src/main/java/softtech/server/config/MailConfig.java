package softtech.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

@Configuration
public class MailConfig {

    private static final Logger logger = LoggerFactory.getLogger(MailConfig.class);

    @Value("${spring.mail.host:smtp.gmail.com}")
    private String mailHost;

    @Value("${spring.mail.port:587}")
    private int mailPort;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Bean
    public JavaMailSender javaMailSender() {
        // Check if mail credentials are provided
        if (mailUsername == null || mailUsername.isEmpty() || 
            mailPassword == null || mailPassword.isEmpty()) {
            
            logger.warn("Mail credentials not configured. Using mock mail sender for development.");
            return new MockJavaMailSender();
        }

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        mailSender.setHost(mailHost);
        mailSender.setPort(mailPort);
        mailSender.setUsername(mailUsername);
        mailSender.setPassword(mailPassword);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "false");
        props.put("mail.smtp.ssl.trust", mailHost);

        return mailSender;
    }

    // Mock JavaMailSender for development/testing
    private static class MockJavaMailSender implements JavaMailSender {
        private static final Logger logger = LoggerFactory.getLogger(MockJavaMailSender.class);

        @Override
        public MimeMessage createMimeMessage() {
            return new MockMimeMessage();
        }

        @Override
        public MimeMessage createMimeMessage(java.io.InputStream contentStream) throws org.springframework.mail.MailException {
            return new MockMimeMessage();
        }

        @Override
        public void send(MimeMessage mimeMessage) throws org.springframework.mail.MailException {
            logger.info("Mock email sent: {}", mimeMessage.toString());
        }

        @Override
        public void send(MimeMessage... mimeMessages) throws org.springframework.mail.MailException {
            for (MimeMessage message : mimeMessages) {
                send(message);
            }
        }

        @Override
        public void send(org.springframework.mail.SimpleMailMessage simpleMessage) throws org.springframework.mail.MailException {
            logger.info("Mock simple email sent to: {}", simpleMessage.getTo());
        }

        @Override
        public void send(org.springframework.mail.SimpleMailMessage... simpleMessages) throws org.springframework.mail.MailException {
            for (org.springframework.mail.SimpleMailMessage message : simpleMessages) {
                send(message);
            }
        }
    }

    // Mock MimeMessage
    private static class MockMimeMessage extends MimeMessage {
        public MockMimeMessage() {
            super((jakarta.mail.Session) null);
        }

        @Override
        public String toString() {
            return "Mock MimeMessage";
        }
    }
}