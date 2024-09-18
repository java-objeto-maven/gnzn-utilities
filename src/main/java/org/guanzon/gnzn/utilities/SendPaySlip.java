package org.guanzon.gnzn.utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import javax.mail.MessagingException;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.mailer.MessageInfo;
import org.guanzon.appdriver.mailer.SendMail;

public class SendPaySlip {
    private static GRider instance = null;
    private static SendMail pomail;
    private static LogWrapper logwrapr;
    
    public static void main(String [] args){
        String path;
        
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Maven_Systems";
        }
        else{
            path = "/srv/GGC_Maven_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        System.setProperty("sys.default.path.temp", path + "/temp");
        
        logwrapr = new LogWrapper("gnzn-utilities.SendPaySlip", System.getProperty("sys.default.path.temp") + "/SendPaySlip.log");
        
        try {
            Properties po_props = new Properties();
            po_props.load(new FileInputStream(path + "/config/cas.properties"));
                    
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
            
            String sender = "";
            
            //set guanzon SMTP configuration as default
            switch (args.length) {
                case 0:
                    sender = "guanzon";
                    System.setProperty("sEmployID", "");
                    break;
                case 1:
                    sender = args[0];
                    System.setProperty("sEmployID", "");
                    break;
                case 2:
                    sender = args[0];
                    System.setProperty("sEmployID", args[1]);
                    break;
                default:
                    System.err.println("Invalid parameters detected.");
                    System.exit(1);
            }
            
            ResultSet rsToSend = extract2send(sender);
            
            if (MiscUtil.RecordCount(rsToSend) <= 0){
                System.out.println("No record to send.");
                System.exit(0);
            } else {
            
            }
            
            //System.out.println("Records to send: " + MiscUtil.RecordCount(rsToSend));
            
            pomail = new SendMail(path, sender);

            if(pomail.connect(true)){
                System.out.println("Successfully connected to mail server.");
                try {                    
                    while(rsToSend.next()){
                        String lsEmail;
                        
                        //mac 2024.09.17 01:55pm
                        //send mail on personal address since guanzon mail can now send to gmail
                        lsEmail = rsToSend.getString("sEmailAdd");
                        
//                        //reroute email to branch and department email...
//                        if(rsToSend.getString("sEmailAdd").isEmpty()){
//                            if(rsToSend.getString("sBranchCD").equalsIgnoreCase("M029") && 
//                                rsToSend.getString("sDeptIDxx").equalsIgnoreCase("015")){
//                                lsEmail = rsToSend.getString("sBranchMl");
//                            } else if(rsToSend.getString("sBranchCD").equalsIgnoreCase("M001") && 
//                                rsToSend.getString("sDeptIDxx").equalsIgnoreCase("015")){
//                                lsEmail = rsToSend.getString("sBranchMl");
//                            } else if(rsToSend.getString("sBranchCD").equalsIgnoreCase("PHO1") && 
//                                (rsToSend.getString("sDeptIDxx").equalsIgnoreCase("015") ||
//                                    rsToSend.getString("sDeptIDxx").equalsIgnoreCase("040"))){
//                                lsEmail = rsToSend.getString("sBranchMl");
//                            } else{
//                                lsEmail = rsToSend.getString("sDeptMail");
//                            }
//                        } else if(!rsToSend.getString("sEmailAdd").contains("gmail")){
//                            lsEmail = rsToSend.getString("sEmailAdd");
//                        } else if(!rsToSend.getString("sEmpLevID").equalsIgnoreCase("0")  ){
//                            lsEmail = rsToSend.getString("sEmailAdd");
//                        } else if(rsToSend.getString("cMainOffc").contains("1")){
//                            if(rsToSend.getString("sBranchCD").equalsIgnoreCase("M029") && 
//                                rsToSend.getString("sDeptIDxx").equalsIgnoreCase("015")){
//                                lsEmail = rsToSend.getString("sBranchMl");
//                            } else if(rsToSend.getString("sBranchCD").equalsIgnoreCase("M001") && 
//                                rsToSend.getString("sDeptIDxx").equalsIgnoreCase("015")){
//                                lsEmail = rsToSend.getString("sBranchMl");
//                            } else if(rsToSend.getString("sBranchCD").equalsIgnoreCase("PHO1") && 
//                                (rsToSend.getString("sDeptIDxx").equalsIgnoreCase("015") ||
//                                    rsToSend.getString("sDeptIDxx").equalsIgnoreCase("040"))){
//                                lsEmail = rsToSend.getString("sBranchMl");
//                            } else{
//                                lsEmail = rsToSend.getString("sDeptMail");
//                            }
//                        } else{
//                            lsEmail = rsToSend.getString("sBranchMl");
//                        }
                        
                        String lsSQL;
                        
                        if(!(lsEmail.isEmpty())){
                            MessageInfo msginfo = new MessageInfo();
                            msginfo.addTo(lsEmail);
                            msginfo.setSubject("PAYSLIP (" + rsToSend.getString("dPeriodFr") + " - " + rsToSend.getString("dPeriodTo") + ")");
                            msginfo.setBody(rsToSend.getString("sEmployNm") + ".\nPlease download and delete your payslip.");
                            msginfo.addAttachment("/srv/GGC_Java_Systems/temp/payslip/" + rsToSend.getString("sEmployID") + rsToSend.getString("sPayPerID") + ".pdf");
                            msginfo.setFrom("No Reply <no-reply@guanzongroup.com.ph>");
                            
                            try {
                                pomail.sendMessage(msginfo);
                                
                                lsSQL = "UPDATE Payroll_Summary_New" +
                                        " SET cMailSent = 1 | 4" +
                                        " WHERE sPayPerID = " + SQLUtil.toSQL(rsToSend.getString("sPayPerID")) +
                                            " AND sEmployID = " + SQLUtil.toSQL(rsToSend.getString("sEmployID"));
                                instance.getConnection().createStatement().executeUpdate(lsSQL);
                            } catch (MessagingException e){
                                lsSQL = "UPDATE Payroll_Summary_New" +
                                        " SET cMailSent = 1 | 6" +
                                        " WHERE sPayPerID = " + SQLUtil.toSQL(rsToSend.getString("sPayPerID")) +
                                            " AND sEmployID = " + SQLUtil.toSQL(rsToSend.getString("sEmployID"));
                                instance.getConnection().createStatement().executeUpdate(lsSQL);
                                
                                logwrapr.severe("extract2send: MessagingException error detected.", e);
                                System.exit(1);
                            }
                        } else {
                            lsSQL = "UPDATE Payroll_Summary_New" +
                                        " SET cMailSent = '6'" +
                                        " WHERE sPayPerID = " + SQLUtil.toSQL(rsToSend.getString("sPayPerID")) +
                                            " AND sEmployID = " + SQLUtil.toSQL(rsToSend.getString("sEmployID"));
                            instance.getConnection().createStatement().executeUpdate(lsSQL);
                        }
                    }
                } catch (SQLException e) {
                    logwrapr.severe("extract2send: SQLException error detected.", e);
                    System.exit(1);
                }
            } else {
                logwrapr.severe("extract2send: SQLException error detected.", "Unable to connect to mail server.");
                System.exit(1);
            }
        } catch (IOException e) {
            logwrapr.severe("extract2send: SQLException error detected.", e);
            System.exit(1);
        }
        
        System.exit(0);
    }
    
    private static ResultSet extract2send(String sender){
        ResultSet rs = null;
        try {  
//            String lsSQL = "SELECT a.sBranchCD, a.sPayPerID, a.sEmployID, IFNULL(b.sEmailAdd, '') sEmailAdd, c.dPeriodFr, c.dPeriodTo, CONCAT(b.sLastName, ', ', b.sFrstname) sEmployNm" +
//                                ", IFNULL(e.sEmailAdd, '') sBranchMl" +
//                                ", IFNULL(d.sEmailAdd, '') sDeptMail" +
//                                ", IFNULL(i.sEmpLevID, '0') sEmpLevID" +
//                                ", f.cDivision" +
//                                ", a.sDeptIDxx" +
//                                ", i.sBranchCD" +
//                                ", e.cMainOffc" +
//                            " FROM Payroll_Summary_New a" +
//                                " LEFT JOIN Client_Master b ON a.sEmployID = b.sClientID" +
//                                " LEFT JOIN Payroll_Period c ON a.sPayPerID = c.sPayPerID" +
//                                " LEFT JOIN Employee_Master001 i ON a.sEmployID = i.sEmployID" + 
//                                " LEFT JOIN Department d ON i.sDeptIDxx = d.sDeptIDxx" + 
//                                " LEFT JOIN Branch e ON i.sBranchCD = e.sBranchCD" + 
//                                " LEFT JOIN Branch_Others f ON i.sBranchCD = f.sBranchCD" + 
//                            " WHERE a.cMailSent = '1'" +       
//                                " AND a.sBranchCd NOT IN (SELECT sBranchCd FROM Branch WHERE IFNULL(sEmailAdd, '') = '')" +
//                            " ORDER BY e.sEmailAdd DESC" + 
//                            (sender.compareToIgnoreCase("ymail") == 0 ? " LIMIT 150" : " LIMIT 350");
            String lsSQL = "SELECT a.sBranchCD, a.sPayPerID, a.sEmployID, IFNULL(b.sEmailAdd, '') sEmailAdd, c.dPeriodFr, c.dPeriodTo, CONCAT(b.sLastName, ', ', b.sFrstname) sEmployNm" +
                                ", IFNULL(e.sEmailAdd, '') sBranchMl" +
                                ", IFNULL(d.sEmailAdd, '') sDeptMail" +
                                ", IFNULL(i.sEmpLevID, '0') sEmpLevID" +
                                ", f.cDivision" +
                                ", a.sDeptIDxx" +
                                ", i.sBranchCD" +
                                ", e.cMainOffc" +
                                ", i.cRecdStat" +
                                ", i.cSalTypex" +
                            " FROM Payroll_Summary_New a" +
                                " LEFT JOIN Client_Master b ON a.sEmployID = b.sClientID" +
                                " LEFT JOIN Payroll_Period c ON a.sPayPerID = c.sPayPerID" +
                                " LEFT JOIN Employee_Master001 i ON a.sEmployID = i.sEmployID" + 
                                " LEFT JOIN Department d ON i.sDeptIDxx = d.sDeptIDxx" + 
                                " LEFT JOIN Branch e ON i.sBranchCD = e.sBranchCD" + 
                                " LEFT JOIN Branch_Others f ON i.sBranchCD = f.sBranchCD" + 
                            " WHERE a.cMailSent = '1'" +       
                                " AND IFNULL(b.sEmailAdd, '') <> ''" +
                            " HAVING i.cRecdStat = '1' AND i.cSalTypex <> 'S'" +
                            " ORDER BY e.sEmailAdd DESC" + 
                            (sender.compareToIgnoreCase("guanzon") == 0 ? " LIMIT 150" : " LIMIT 350");
            
            if (!System.getProperty("sEmployID").isEmpty()){
                lsSQL = MiscUtil.addCondition(lsSQL, "a.sEmployID = " + SQLUtil.toSQL(System.getProperty("sEmployID")));
            }

            System.out.println(lsSQL);
            rs = instance.getConnection().createStatement().executeQuery(lsSQL);
        } catch (SQLException ex) {
            logwrapr.severe("extract2send: SQLException error detected.", ex);
            System.exit(1);
        }
        
        return rs;
   }  
}
