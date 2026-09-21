package com.parfum.ecommerce.mail;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.enabled}")
    private boolean enabled;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.from-name}")
    private String fromName;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    /**
     * Envoi asynchrone : un échec d'email ne doit jamais bloquer
     * une inscription ou une commande.
     */
    @Async
    public void send(String to, String subject, String templateName, Map<String, Object> variables) {
        if (!enabled) {
            log.info("\n=== EMAIL (non envoye, mode developpement) ===\nA : {}\nSujet : {}\nDonnees : {}\n==============================================",
                    to, subject, variables);
            return;
        }

        try {
            Context context = new Context();
            context.setVariables(variables);
            String html = templateEngine.process("mail/" + templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("Email '{}' envoye a {}", subject, to);

        } catch (Exception e) {
            log.error("Echec de l'envoi de l'email '{}' a {} : {}", subject, to, e.getMessage());
        }
    }
}