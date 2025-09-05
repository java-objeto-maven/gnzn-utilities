package org.guanzon.gnzn.utilities.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.guanzon.appdriver.base.GRider;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class testLogin {
    public static void main(String [] args){
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Maven_Systems";
        }
        else{
            path = "/srv/GGC_Maven_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        
        GRider instance = new GRider("gRider");
        
        if (!instance.logUser("gRider", "M001000001")){
            System.err.println(instance.getErrMsg());
            System.exit(1);
        }
        
        // Replace with your Gmail account and App Password
        final String username = "guanzon.events@gmail.com";
        final String password = "hcewfmkwvvzbfwrt"; // Use App Password, not normal Gmail password

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
        
        String lsSQL = "SELECT a.sAreaDesc, a.sEmailAd2" + 
                        " FROM GGC_Events.Event_Attendee_List a" + 
                            ", GGC_Events.Event_Detail b" +
                        " WHERE a.sAttndIDx = b.sAttndIDx" +
                                " AND a.sEmployID IS NOT NULL" + 
                                " AND a.sAttndIDx BETWEEN 'M001250281' AND 'M001250678'" + 
                                " AND (a.sCompnyID LIKE 'M%' OR a.sCompnyID LIKE 'C%')" +
                                " AND b.cMailSent = '0'" +
                                " AND a.sAreaDesc IS NOT NULL" +
                                " AND a.`sEmailAd2` IS NOT NULL" +
                        " GROUP BY a.sAreaDesc, a.sEmailAd2" +
                        " ORDER BY a.sAreaDesc";
        
        ResultSet loRS = instance.executeQuery(lsSQL);
        
        try {
            while (loRS.next()){
                String dir = "D:/GGC_Java_Systems/temp/invitation/internal/";
                String lsEmail = loRS.getString("sEmailAd2");
                String lsArea = loRS.getString("sAreaDesc") + " - ";
                
                System.out.println(lsEmail);
                System.out.println(lsArea);
                
                List<String> results = findFilesWithWord(dir, lsArea);

                if (results.isEmpty()) {
                    System.out.println("No files found containing '" + lsArea + "'");
                } else {
                    // Create a new email message
                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(username, "Guanzon Convention"));
                    message.setRecipients(
                            Message.RecipientType.TO,
                            InternetAddress.parse(lsEmail)
                    );
                    
                    message.setRecipients(
                        Message.RecipientType.CC,
                        InternetAddress.parse("mtcuison@outlook.com, cmespanol@outlook.com")
                    );
                    
                    message.setSubject("Guanzon Convention Registration QR Code - " + lsArea);

                    // --- Email Body ---
                    MimeBodyPart textPart = new MimeBodyPart();
                    textPart.setText("KAY gandang araw! Please save attached QR images for the registration on Sept 6 Convention Night.\n\n" +
                                        "Kindly disseminate individually to Officers in your AOR for their reference.\n\n");

                    // --- Multipart container ---
                    Multipart multipart = new MimeMultipart();
                    multipart.addBodyPart(textPart);
                    
                    System.out.println("Files found containing '" + lsArea + "':");
                    for (String file : results) {
                        System.out.println(file);
                        MimeBodyPart attachmentPart = new MimeBodyPart();
                        attachmentPart.attachFile(new File(file));
                        multipart.addBodyPart(attachmentPart);
                    }
                    
                     // --- Set content ---
                    message.setContent(multipart);

                    // Send the email
                    Transport.send(message);

                    System.out.println("✅ Email sent successfully with multiple attachments!");
                }
            }
        } catch (MessagingException | SQLException | IOException e) {
            e.printStackTrace();
        }
    }
    
    public static List<String> findFilesWithWord(String directoryPath, String keyword) {
        List<String> matchedFiles = new ArrayList<>();
        File dir = new File(directoryPath);

        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("Invalid directory: " + directoryPath);
            return matchedFiles;
        }

        searchDirectory(dir, keyword.toLowerCase(), matchedFiles);
        return matchedFiles;
    }

    private static void searchDirectory(File dir, String keyword, List<String> matchedFiles) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                // Recursively search subdirectories
                searchDirectory(file, keyword, matchedFiles);
            } else {
                // Check if filename contains the keyword
                if (file.getName().toLowerCase().contains(keyword)) {
                    matchedFiles.add(file.getAbsolutePath());
                }
            }
        }
    }
}
