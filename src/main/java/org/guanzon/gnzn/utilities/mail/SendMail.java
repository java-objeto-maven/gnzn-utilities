/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.guanzon.gnzn.utilities.mail;

import java.util.*;
import java.io.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.Message.RecipientType;
import org.guanzon.appdriver.base.MySQLAESCrypt;

/**
 *
 * @author kalyptus
 */
public class SendMail {
   String SIGNATURE = "08220326";
   Properties po_props = new Properties();
   String ps_path;
   Boolean pb_init;
   Session po_session;
   Transport po_trans;
   MimeMessage po_msg;
   Multipart po_mprt;

   public SendMail(String app_path, String propfile){
      ps_path = app_path;

      try {
         po_props.load(new FileInputStream(ps_path + "/config/" + propfile + ".properties"));
         pb_init = true;
      } 
      catch (IOException ex) {
         ex.printStackTrace();
         pb_init = false;
      }
   }   
   
   //Connect to the server
   public boolean connect(boolean isdebug){
      //Don't allow connection if not properly initiaze.
      if(!pb_init) return false;
      
      final String user = po_props.getProperty("mail.user.id");
      final String pass = MySQLAESCrypt.Decrypt(po_props.getProperty("mail.user.auth"), SIGNATURE);
      
      po_session = Session.getDefaultInstance(po_props,
          new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
          }
        });
      
      po_session.setDebug(isdebug);

      boolean isOk = false;
      
      //Initialize connection here....
      try {
         po_trans = po_session.getTransport("smtp");
         po_trans.connect();
         isOk = true;
      } catch (NoSuchProviderException ex) {
         ex.printStackTrace();
      } catch (MessagingException ex) {
         ex.printStackTrace();
      }
     
      return isOk;
   }   
   
   // initialize the message
   public boolean initmsg(){
      boolean bOk = true;
      
      //Don't allow initialization of message if not properly initiaze.
      if(!pb_init) return bOk;
      
      po_msg = new MimeMessage(po_session);
      po_mprt = new MimeMultipart();
      
      try {
         po_msg.setFrom(new InternetAddress(po_props.getProperty("mail.user.id")));
      } catch (MessagingException ex) {
         bOk = false;
      }
      
      return bOk;
   }
   
   // set sender
   public void setFrom(String from) throws MessagingException{
      po_msg.setFrom(new InternetAddress(from));
   }
   
   // set recepients
   public void setRecipients(RecipientType type, String to) throws AddressException, MessagingException{
         InternetAddress[] address = {new InternetAddress(to)};
         po_msg.setRecipients(type, address);
   }   
   
   // add additional recipient
   public void addRecipients(RecipientType type, String to) throws AddressException, MessagingException{
      InternetAddress[] address = {new InternetAddress(to)};
      po_msg.addRecipients(type, address);
   }   
   
   //set subject
   public void setSubject(String subject) throws MessagingException{
      po_msg.setSubject(subject);
   }
   
   //set email message
   //kalyptus - 2019.09.03 04:13pm
   //set the message as HTML instead of pure text...
   public void setBody(String message) throws MessagingException{
      MimeBodyPart mbp1 = new MimeBodyPart();
      //kalyptus - 2019.09.03 02:42pm
      //use setContent to enable sending of html instead of pure text
      mbp1.setContent(message, "text/html");
      //mbp1.setText(message);      
      po_mprt.addBodyPart(mbp1);
   }
   
   //https://www.codejava.net/java-ee/javamail/embedding-images-into-e-mail-with-javamail
   //kalyptus - 2019.09.03 04:12pm 
   //add capability to add online images to the mail
   public void addOnlineImage(String cid, String image){
        MimeBodyPart mbp1 = new MimeBodyPart();
        //DataSource fds = new FileDataSource(image);

        try {
            //mbp1.setDataHandler(new DataHandler(fds));
            mbp1.setHeader("Content-ID", "<" + cid + ">");
            //tell mail clients that the image is to be displayed inline (not as an attachment) 
            mbp1.setDisposition(MimeBodyPart.INLINE);
            
            mbp1.attachFile(image);
            po_mprt.addBodyPart(mbp1);
        } catch (MessagingException ex) {
           ex.printStackTrace();
        } catch (IOException ex) {
           ex.printStackTrace();
        }
   }
   
   //set attachement
   public void addAttachment(String filename) throws IOException, MessagingException{
      MimeBodyPart mbp1 = new MimeBodyPart();
      mbp1.attachFile(filename);      
      po_mprt.addBodyPart(mbp1);
   }

   // send message
   public void sendMessage() throws MessagingException{
      // add the Multipart to the message
      po_msg.setContent(po_mprt);
      // set the Date: header
      po_msg.setSentDate(new Date());
      // don't forget to save
      po_msg.saveChanges();  
      // send the message
      po_trans.sendMessage(po_msg, po_msg.getAllRecipients());
   }
   
   // send message
   public void sendMessage(MessageInfo msginfo) throws MessagingException, IOException{
      //initialize message
      initmsg();

      //set from
      if(!msginfo.getFrom().isEmpty()){
         setFrom(msginfo.getFrom());
      }

      //set to
      if(msginfo.Size_To() > 0 ){
         StringBuffer lsrcpt = new StringBuffer();
         for(int n=0;n<=msginfo.Size_To()-1;n++){
            addRecipients(Message.RecipientType.TO, msginfo.getTo(n));               
            //lsrcpt.append(";").append(msginfo.getTo(n));
         }
         //setRecipients(Message.RecipientType.TO, lsrcpt.substring(1));               
      }

      //set cc
      if(msginfo.Size_CC() > 0 ){
         StringBuffer lsrcpt = new StringBuffer();
         for(int n=0;n<=msginfo.Size_CC()-1;n++){
            addRecipients(Message.RecipientType.CC, msginfo.getCC(n));               
            //lsrcpt.append(";").append(msginfo.getCC(n));
         }
         //setRecipients(Message.RecipientType.CC, lsrcpt.substring(1));               
      }
      
      //set bcc
      if(msginfo.Size_BCC() > 0 ){
         StringBuffer lsrcpt = new StringBuffer();
         for(int n=0;n<=msginfo.Size_BCC()-1;n++){
            addRecipients(Message.RecipientType.BCC, msginfo.getBCC(n));
            //lsrcpt.append(";").append(msginfo.getBCC(n));
         }
         //setRecipients(Message.RecipientType.BCC, lsrcpt.substring(1));               
      }

      setSubject(msginfo.getSubject());
      setBody(msginfo.getBody());

      //kalyptus - 2019.09.03 04:11pm
      //set online images
      if(msginfo.Size_OnlineImage()> 0 ){
          msginfo.getOnlineImages().keySet().forEach((key) -> {
              addOnlineImage(key, msginfo.getOnlineImages().get(key));
          });
      }
      
      //set attachment
      if(msginfo.Size_Attachment()> 0 ){
         for(int n=0;n<=msginfo.Size_Attachment()-1;n++){
            addAttachment(msginfo.getAttachment(n));
         }
      }
      
      // add the Multipart to the message
      po_msg.setContent(po_mprt);
      // set the Date: header
      po_msg.setSentDate(new Date());
      // don't forget to save
      po_msg.saveChanges();  
      // send the message
      po_trans.sendMessage(po_msg, po_msg.getAllRecipients());
   }
   
   // disconnect from server...
   public void disconnect() throws MessagingException{
      po_trans.close();
   }
}   
