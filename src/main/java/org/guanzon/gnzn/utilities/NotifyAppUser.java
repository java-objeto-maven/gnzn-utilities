package org.guanzon.gnzn.utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import javax.mail.MessagingException;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MySQLAESCrypt;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.gnzn.utilities.mail.MessageInfo;
import org.guanzon.gnzn.utilities.mail.SendMail;

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
        
                if (!instance.logUser("gRider", "M001111122")){
                    System.err.println(instance.getErrMsg());
                    System.exit(1);
                }
            } else {
                System.err.println("Unable to log user.");
                System.exit(1);
            }
            
            SendMail mail = new SendMail(System.getProperty("sys.default.path.config"), "gmail");
        
            if (mail.connect(true)){
                String lsSQL = "SELECT * FROM App_User_Credential_Request" +
                                " WHERE cTranStat = '0'" +
                                    " AND cNotifyxx = '0'";

                ResultSet loRS = instance.executeQuery(lsSQL);

                String to;
                String message;

                while(loRS.next()){
                    to = loRS.getString("sEmailAdd");
                    message = "Thank you for keeping up with us!" +
                                "<br><br>Listed below is your account credential." +
                                "<br><br>Email     : " + loRS.getString("sEmailAdd")  +
                                "<br>Password  : " + MySQLAESCrypt.Decrypt(loRS.getString("sPassword"), "20190625");

                    MessageInfo msginfo = new MessageInfo();           
                    msginfo.addTo(to);
                    msginfo.setSubject("Your Guanzon App Credentials");
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
