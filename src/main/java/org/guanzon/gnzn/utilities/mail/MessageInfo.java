package org.guanzon.gnzn.utilities.mail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author kalyptus
 */
public class MessageInfo {
   String ps_from;
   ArrayList<String> ps_to;
   ArrayList<String> ps_cc;
   ArrayList<String> ps_bc;
   String ps_subject;
   String ps_date;
   String ps_body;
   ArrayList<String> ps_attach;
   Map<String, String> ps_photo;

   public MessageInfo(){
      ps_from = "";
      ps_subject = "";
      ps_date = "";
      ps_body = "";
      
      ps_to = new ArrayList<String>();
      ps_cc = new ArrayList<String>();
      ps_bc = new ArrayList<String>();
      ps_attach = new ArrayList<String>();
      ps_photo = new HashMap<String, String>();
   }

   //from
   public String getFrom(){
      return ps_from;
   }
   public void setFrom(String from){
      ps_from = from;
   }

   //to
   public String getTo(int item){
      return ps_to.get(item);
   }
   public void addTo(String to){
        //byte email[] = to.getBytes("UTF-8");
       
        ps_to.add(to);
   }
   public int Size_To(){
      return ps_to.size();
   }
   
   //cc
   public String getCC(int item){
      return ps_cc.get(item);
   }
   public void addCC(String cc){
      ps_cc.add(cc);
   }
   public int Size_CC(){
      return ps_cc.size();
   }
   
   //bcc
   public String getBCC(int item){
      return ps_bc.get(item);
   }
   public void addBCC(String bcc){
      ps_bc.add(bcc);
   }
   public int Size_BCC(){
      return ps_bc.size();
   }

   //subject
   public String getSubject(){
      return ps_subject;
   }
   public void setSubject(String subject){
      ps_subject = subject;
   }
   
   //date
   public String getDate(){
      return ps_date;
   }
   public void setDate(String date){
      ps_date = date;
   }
   
   public String getBody(){
      return ps_body;
   }
   public void setBody(String body){
      ps_body = body;
   }

   public Map<String, String>getOnlineImages(){
      return ps_photo;
   }
   //where photo should be CID:path
   //example: sign:/home/guanzon/mix.bmp
   //example: image:/home/guanzon/pix.jpg
   public void addOnlineImage(String cid, String photo){
      ps_photo.put(cid, photo);       
   }
   public int Size_OnlineImage(){
      return ps_photo.size();
   }
   
   public String getAttachment(int item){
      return ps_attach.get(item);
   }
   public void addAttachment(String attachment){
      ps_attach.add(attachment);
   }
   public int Size_Attachment(){
      return ps_attach.size();
   }
}
