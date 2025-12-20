package org.guanzon.gnzn.utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.codec.binary.Base64;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.base.WebClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class SendPaySlipApp {
    private static GRider instance = null;
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
        
        logwrapr = new LogWrapper("gnzn-utilities.SendPaySlipApp", System.getProperty("sys.default.path.temp") + "/SendPaySlip.log");
        
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
            
            ResultSet rsToSend = extract2send();
            
            if (MiscUtil.RecordCount(rsToSend) <= 0){
                System.out.println("No record to send.");
                SendPaySlip.main(args);
                System.exit(0);
            }
            
            try {
                while (rsToSend.next()){
                    if(!rsToSend.getString("sProdctID").isEmpty()){
                        String message;
                        message = "Good day! \n\n Attached is your payslip for the payroll period " + rsToSend.getString("dPeriodFr") + " - " + rsToSend.getString("dPeriodTo");
                        message += ".\n\n [http://repl.guanzongroup.com.ph:2007/repl/misc/download_ps.php?period="+ toBase64(rsToSend.getString("sPayPerID")) + "&client=" + toBase64(rsToSend.getString("sEmployID")) + "]";
                        if (sendNotification( 
                            rsToSend.getString("sProdctID"), 
                            rsToSend.getString("sUserIDxx"), 
                            "PAYSLIP (" + rsToSend.getString("dPeriodFr") + " - " + rsToSend.getString("dPeriodTo") + ")", 
                            message)){
                            
                            String lsSQL = "UPDATE Payroll_Summary_New" +
                                            " SET cMailSent = 1 | 2" +
                                            " WHERE sPayPerID = " + SQLUtil.toSQL(rsToSend.getString("sPayPerID")) +
                                                " AND sEmployID = " + SQLUtil.toSQL(rsToSend.getString("sEmployID"));
                            instance.getConnection().createStatement().executeUpdate(lsSQL);
                        }
                    }
                }
            } catch (SQLException e) {
                logwrapr.severe("extract2send: SQLException error detected.", e);
                System.exit(1);
            }
        } catch (IOException e) {
            logwrapr.severe("extract2send: IOException error detected.", e);
            System.exit(1);
        }
        
        SendPaySlip.main(args);
        
        System.exit(0);
    }
    
    private static ResultSet extract2send(){
        ResultSet rs = null;
        
        String lsSQL = "SELECT" +
                            "  a.sBranchCD" +
                            ", a.sPayPerID" +
                            ", a.sEmployID" +
                            ", IFNULL(b.sEmailAdd, '') sEmailAdd" +
                            ", c.dPeriodFr" +
                            ", c.dPeriodTo" +
                            ", CONCAT(b.sLastName, ', ', b.sFrstname) sEmployNm" +
                            ", IFNULL(g.sUserIDxx, '') sUserIDxx" +
                            ", IFNULL(j.sProdctID, '') sProdctID" +
                            ", IFNULL(e.sEmailAdd, '') sBranchMl" + 
                            ", IFNULL(d.sEmailAdd, '') sDeptMail" + 
                            ", IFNULL(i.sEmpLevID, '0') sEmpLevID" + 
                            ", f.cDivision" + 
                            ", a.sDeptIDxx" + 
                            ", i.sBranchCD" + 
                            ", e.cMainOffc" +
                            ", g.sAppVersn" + 
                        " FROM Payroll_Summary_New a" + 
                            " LEFT JOIN Client_Master b ON a.sEmployID = b.sClientID" + 
                            " LEFT JOIN Payroll_Period c ON a.sPayPerID = c.sPayPerID" + 
                            " LEFT JOIN App_User_Master j" + 
                                " LEFT JOIN App_User_Device g" + 
                                " ON j.sUserIDxx = g.sUserIDxx" +
                                    " AND g.sAppVersn >= 79" +
                            " ON a.sEmployID = j.sEmployNo" + 
                                " AND j.cActivatd = '1'" + 
                                " AND j.sProdctID = 'gRider'" + 
                            " LEFT JOIN Employee_Master001 i ON a.sEmployID = i.sEmployID" +  
                            " LEFT JOIN Department d ON i.sDeptIDxx = d.sDeptIDxx" +  
                            " LEFT JOIN Branch e ON i.sBranchCD = e.sBranchCD" +  
                            " LEFT JOIN Branch_Others f ON i.sBranchCD = f.sBranchCD" +  
                        " WHERE a.cMailSent = '1'" + 
                        " GROUP BY sPayPerID, sUserIDxx" +                  
                        " HAVING sUserIDxx <> ''" +
                        " ORDER BY a.sPayPerID ASC, e.sEmailAdd DESC";
        //" AND a.sPayPerID IN ('A0012425', 'A0012426', 'M0012437', 'M0012438', 'A0012427', 'A0012428', 'M0012440', 'M0012441')" +

        rs = instance.executeQuery(lsSQL);
        
        return rs;
    }  
    
    private static boolean sendNotification(String apps, String user, String title, String message) throws IOException{
        String sURL = "https://restgk.guanzongroup.com.ph/notification/send_request_system.php";        
        
        Calendar calendar = Calendar.getInstance();
        
        Map<String, String> headers = 
                        new HashMap<String, String>();
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json");
        headers.put("g-api-id", "gRider");
        headers.put("g-api-imei", "356060072281722");
        headers.put("g-api-key", SQLUtil.dateFormat(calendar.getTime(), "yyyyMMddHHmmss"));    
        headers.put("g-api-hash", org.apache.commons.codec.digest.DigestUtils.md5Hex((String)headers.get("g-api-imei") + (String)headers.get("g-api-key")));    
        headers.put("g-api-user", "GAP0190001");   
        headers.put("g-api-mobile", "09178048085");
        headers.put("g-api-token", "fFg2vKxLR-6VJmLA1f8ZbX:APA91bF-pCydHARkxMoj5JeyhHM9WyHo8WhES--609t5-vD9wEfR5PcgHCCRpPqsZHHDmD3CySSSKhvB7Lud_jOLYTcmDk--PDry4darnlQGdsB-9tgPDmfnAHXnf1k7NJpPh0Vu2xFA");    

        JSONArray rcpts = new JSONArray();
        JSONObject rcpt = new JSONObject();
        rcpt.put("app", apps);
        rcpt.put("user", user);
        rcpts.add(rcpt);
        
        //Create the parameters needed by the API
        JSONObject param = new JSONObject();
        param.put("type", "00000");
        param.put("parent", null);
        param.put("title", title);
        param.put("message", message);
        param.put("rcpt", rcpts);

        JSONParser oParser = new JSONParser();
        JSONObject json_obj = null;

        String response = WebClient.sendHTTP(sURL, param.toJSONString(), (HashMap<String, String>) headers);
        if(response == null){
            System.out.println("HTTP Error detected: " + System.getProperty("store.error.info"));
            return false;
        }
        
        System.out.println(response);
        return true;
    }
    
    private static String toBase64(String val){
        Base64 base64 = new Base64();
        String encodedString1 = new String(base64.encode(val.getBytes()));
        return (encodedString1);
    }
}
