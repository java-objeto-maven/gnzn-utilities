package org.guanzon.gnzn.utilities.mis;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import javax.mail.MessagingException;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MySQLAESCrypt;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.mailer.MessageInfo;
import org.guanzon.appdriver.mailer.SendMail;

public class NotifyAppUser {
    public static void main (String [] args){
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Maven_Systems";
        }
        else{
            path = "/srv/GGC_Maven_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        
        try {
            Properties po_props = new Properties();
            po_props.load(new FileInputStream(path + "/config/cas.properties"));
            
            GRider instance = null;
                    
            if (po_props.getProperty("developer.mode").equals("1")){
                instance = new GRider("gRider");
        
                if (!instance.logUser("gRider", "M001000001")){
                    System.err.println(instance.getErrMsg());
                    System.exit(1);
                }
            } else {
                System.err.println("Unable to log user.");
                System.exit(1);
            }
            
            SendMail mail = new SendMail(System.getProperty("sys.default.path.config"), "gmail");
        
            if (mail.connect(true)){
                String to;
                String message;
                
                //request forgot password
                String lsSQL = "SELECT * FROM App_User_Credential_Request" +
                                " WHERE cTranStat = '0'" +
                                    " AND cNotifyxx = '0'";

                ResultSet loRS = instance.executeQuery(lsSQL);

                while(loRS.next()){
                    to = loRS.getString("sEmailAdd");
                    message = "Thank you for keeping up with us!" +
                                "<br><br>Listed below is your account credential." +
                                "<br><br>Email     : " + loRS.getString("sEmailAdd")  +
                                "<br>Password  : " + MySQLAESCrypt.Decrypt(loRS.getString("sPassword"), "20190625");

                    MessageInfo msginfo = new MessageInfo();           
                    msginfo.addTo(to);
                    msginfo.setSubject("Guanzon App: Credentials");
                    msginfo.setFrom("Guanzon <no-reply@guanzongroup.com.ph>");
                    msginfo.setBody(message);
                    mail.sendMessage(msginfo);
                    
                    lsSQL = "UPDATE App_User_Credential_Request SET" +
                                "  cNotifyxx = '1'" +
                                ", dNotifyxx = " + SQLUtil.toSQL(instance.getServerDate()) +
                                ", cTranStat = '1'" +
                            " WHERE sTransNox = " + SQLUtil.toSQL(loRS.getString("sTransNox"));
                    instance.executeUpdate(lsSQL);
                }
                
                //new accounts
                lsSQL = "SELECT *" + 
                        " FROM App_User_Master" + 
                        " WHERE sProdctID IN ('gRider', 'IntegSys', 'GuanzonApp')" +
                            " AND cEmailSnt <> '1'" + 
                            " AND cActivatd <> '1'" +
                            " AND dCreatedx >= '2023-08-01'" +
                        " ORDER BY dCreatedx DESC" +
                        " LIMIT 50";

                loRS = instance.executeQuery(lsSQL);

                while (loRS.next()){
                    to = loRS.getString("sEmailAdd");
                    message = "Thank you for signing up!" +
                                "<br><br>Your account has been created. Please click the link below to activate your account." +
                                "<br><br>https://restgk.guanzongroup.com.ph/security/account_verify.php?email=" + loRS.getString("sEmailAdd") + "&hash=" + loRS.getString("sItIsASIN");

                    MessageInfo msginfo = new MessageInfo();           
                    msginfo.addTo(to);
                    msginfo.setSubject("Guanzon App: Verify");
                    msginfo.setFrom("Guanzon <no-reply@guanzongroup.com.ph>");
                    msginfo.setBody(message);
                    mail.sendMessage(msginfo);
                    
                    lsSQL = "UPDATE App_User_Master SET" +
                                "  cEmailSnt = '1'" +
                                ", nEmailSnt = 1" +
                            " WHERE sUserIDxx = " + SQLUtil.toSQL(loRS.getString("sUserIDxx"));
                    instance.executeUpdate(lsSQL);
                }
                
                //update mobile request
                lsSQL = "SELECT" +
                            "  a.sTransNox" +
                            ", a.cReqstCDe" +
                            ", a.sMobileNo" +
                            ", a.sSourceCD" +
                            ", a.sSourceNo" +
                            ", a.sMobileNo" +
                            ", IFNULL(c.sFrstName, '') xFrstName" +
                            ", b.sEmailAdd" +
                        " FROM Mobile_Update_Request a" +
                            " LEFT JOIN App_User_Master b ON a.sSourceNo = b.sUserIDxx" +
                            " LEFT JOIN Client_Master c ON b.sMPlaceID = c.sClientID" +
                        " WHERE a.sSourceCD = 'SKit'" + 
                            " AND a.cTranStat = '0'";

                loRS = instance.executeQuery(lsSQL);

                while (loRS.next()){
                    to = loRS.getString("sEmailAdd");

                    if (loRS.getString("xFrstName").isEmpty()){
                        message = "Hi!";
                    } else {
                        message = "Hi " + loRS.getString("xFrstName") + "!\n\n";
                    }

                    message += "<br>You have requested to change your mobile number to " + loRS.getString("sMobileNo") + ".\n";
                    message += "<br><br>If you did not initiate this transaction please inform your UPLINE or contact us on 09171545477 or 09989545477.";

                    MessageInfo msginfo = new MessageInfo();           
                    msginfo.addTo(to);
                    msginfo.setSubject("Guanzon App: Update Mobile");
                    msginfo.setFrom("Guanzon <no-reply@guanzongroup.com.ph>");
                    msginfo.setBody(message);
                    mail.sendMessage(msginfo);
                    
                    lsSQL = "UPDATE Mobile_Update_Request SET" +
                                "  cTranStat = '1'" +
                            " WHERE sTransNox = " + SQLUtil.toSQL(loRS.getString("sTransNox"));
                    instance.executeUpdate(lsSQL);

                    lsSQL = "UPDATE App_User_Master SET" +
                                "  sMobileNo = " + SQLUtil.toSQL(loRS.getString("sMobileNo")) +
                            " WHERE sUserIDxx = " + SQLUtil.toSQL(loRS.getString("sSourceNo"));
                    instance.executeUpdate(lsSQL);
                }
            } else {
                System.err.println("Unable to to connect to email host...");
                System.exit(1);
            }
        } catch (IOException | SQLException | MessagingException e) {
            e.printStackTrace();
            System.exit(1);
        }
        
        System.exit(0);
    }
}
