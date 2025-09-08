package org.guanzon.gnzn.utilities.test;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class testSendOutlookMail {
    public static void main(String[] args) {
        // Replace with your Gmail account and App Password
        final String username = "guanzon.events@gmail.com";
        final String password = "hcewfmkwvvzbfwrt"; // Not your normal Gmail password

        // Gmail SMTP server settings
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Create a session with authentication
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // Create a new email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username, "Guanzon Convention"));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse("michael_cuison07@yahoo.com")
            );
            message.setSubject("Test Email from Java via Gmail SMTP");
            message.setText("Hello, this is a test email sent using Java and Gmail SMTP!");

            // Send the email
            Transport.send(message);

            System.out.println("Email sent successfully!");
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(testSendOutlookMail.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
