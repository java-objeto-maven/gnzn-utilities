package org.guanzon.gnzn.utilities.api;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.base.WebClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GET_app_user_master {
    public static void main(String[] args) {
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
                instance = new GRider("TeleMktg");

                if (!instance.logUser("TeleMktg", "M001000001")){
                    System.err.println(instance.getErrMsg());
                    System.exit(1);
                }
            } else {
                System.err.println("Unable to log user.");
                System.exit(1);
            }
            
            String sql;
            String param;
            ResultSet ors;
            
            JSONObject json = new JSONObject();
            
            if (args.length > 0){
                param = args[0];
                
                if (CommonUtils.isDate(param, SQLUtil.FORMAT_TIMESTAMP)){
                    json.put("sTransNox", "");
                    json.put("dTimeStmp", param);
                } else {
                    json.put("sTransNox", param);
                    json.put("dTimeStmp", "");
                }
            } else {
                sql = "SELECT dTimeStmp FROM App_User_Master ORDER BY dTimeStmp DESC LIMIT 1";
                ors = instance.executeQuery(sql);

                json.put("sTransNox", "");
                json.put("dTimeStmp", "1900-01-01 00:00:01");
                
                if (ors.next()){
                    json.put("dTimeStmp", ors.getString("dTimeStmp"));
                }
            }

            JSONObject headers = new JSONObject();
            headers.put("g-access-token", API.getAccessToken(System.getProperty("sys.default.path.config") + "/access.token"));

            String response;
            
            response = WebClient.sendHTTP(API.GET_APP_USER, json.toJSONString(), (HashMap<String, String>) headers);
            if(response == null){
                System.out.println("No Response");
                System.exit(1);
            } 
            
            JSONParser parser = new JSONParser();
            json = (JSONObject) parser.parse(response);
            
            if ("success".equals((String) json.get("result"))){
                JSONArray arr = (JSONArray) json.get("payload");
                
                
                for (int lnCtr = 0; lnCtr <= arr.size() - 1; lnCtr++){
                    json = (JSONObject) arr.get(lnCtr);
                    
                    sql = "SELECT sUserIDxx FROM App_User_Master WHERE sUserIDxx = " + SQLUtil.toSQL(json.get("sUserIDxx"));
                    ors = instance.executeQuery(sql);
                    
                    if (ors.next()){
                        sql = "UPDATE App_User_Master SET" +
                                    "  sUserIDxx = " + SQLUtil.toSQL(json.get("sUserIDxx")) +
                                    ", sProdctID = " + SQLUtil.toSQL(json.get("sProdctID")) +
                                    ", sUserName = " + SQLUtil.toSQL(json.get("sUserName")) +
                                    ", sEmailAdd = " + SQLUtil.toSQL(json.get("sEmailAdd")) +
                                    ", sPassword = " + SQLUtil.toSQL(json.get("sPassword")) +
                                    ", sItIsASIN = " + SQLUtil.toSQL(json.get("sItIsASIN")) +
                                    ", sOTPasswd = " + SQLUtil.toSQL(json.get("sOTPasswd")) +
                                    ", sMobileNo = " + SQLUtil.toSQL(json.get("sMobileNo")) +
                                    ", sEmployNo = " + SQLUtil.toSQL(json.get("sEmployNo")) +
                                    ", sMPlaceID = " + SQLUtil.toSQL(json.get("sMPlaceID")) +
                                    ", nUserLevl = " + SQLUtil.toSQL(json.get("nUserLevl")) +
                                    ", cGloblAct = " + SQLUtil.toSQL(json.get("cGloblAct")) +
                                    ", cMPClient = " + SQLUtil.toSQL(json.get("cMPClient")) +
                                    ", cActivatd = " + SQLUtil.toSQL(json.get("cActivatd")) +
                                    ", cEmailSnt = " + SQLUtil.toSQL(json.get("cEmailSnt")) +
                                    ", nEmailSnt = " + SQLUtil.toSQL(json.get("nEmailSnt")) +
                                    ", cInactive = " + SQLUtil.toSQL(json.get("cInactive"));

                                if ("".equals((String) json.get("dActivatd"))){
                                    sql += ", dActivatd = NULL";
                                } else {
                                    sql += ", dActivatd = " + SQLUtil.toSQL(json.get("dActivatd"));
                                }
                                
                                if ("".equals((String) json.get("dCreatedx"))){
                                    sql += ", dCreatedx = NULL";
                                } else {
                                    sql += ", dCreatedx = " + SQLUtil.toSQL(json.get("dCreatedx"));
                                }
                        
                                if ("".equals((String) json.get("dInactive"))){
                                    sql += ", dInactive = NULL";
                                } else {
                                    sql += ", dInactive = " + SQLUtil.toSQL(json.get("dInactive"));
                                }

                                if ("".equals((String) json.get("dFirstLog"))){
                                    sql += ", dFirstLog = NULL";
                                } else {
                                    sql += ", dFirstLog = " + SQLUtil.toSQL(json.get("dFirstLog"));
                                }
                                
                                if ("".equals((String) json.get("dLastLogx"))){
                                    sql += ", dLastLogx = NULL";
                                } else {
                                    sql += ", dLastLogx = " + SQLUtil.toSQL(json.get("dLastLogx"));
                                }
                                
                            sql += ", dTimeStmp = " + SQLUtil.toSQL(json.get("dTimeStmp")) +
                                " WHERE sUserIDxx = " + SQLUtil.toSQL(json.get("sUserIDxx"));
                    } else {
                        sql = "INSERT INTO App_User_Master SET" +
                                "  sUserIDxx = " + SQLUtil.toSQL(json.get("sUserIDxx")) +
                                ", sProdctID = " + SQLUtil.toSQL(json.get("sProdctID")) +
                                ", sUserName = " + SQLUtil.toSQL(json.get("sUserName")) +
                                ", sEmailAdd = " + SQLUtil.toSQL(json.get("sEmailAdd")) +
                                ", sPassword = " + SQLUtil.toSQL(json.get("sPassword")) +
                                ", sItIsASIN = " + SQLUtil.toSQL(json.get("sItIsASIN")) +
                                ", sOTPasswd = " + SQLUtil.toSQL(json.get("sOTPasswd")) +
                                ", sMobileNo = " + SQLUtil.toSQL(json.get("sMobileNo")) +
                                ", sEmployNo = " + SQLUtil.toSQL(json.get("sEmployNo")) +
                                ", sMPlaceID = " + SQLUtil.toSQL(json.get("sMPlaceID")) +
                                ", nUserLevl = " + SQLUtil.toSQL(json.get("nUserLevl")) +
                                ", cGloblAct = " + SQLUtil.toSQL(json.get("cGloblAct")) +
                                ", cMPClient = " + SQLUtil.toSQL(json.get("cMPClient")) +
                                ", cActivatd = " + SQLUtil.toSQL(json.get("cActivatd")) +
                                ", cEmailSnt = " + SQLUtil.toSQL(json.get("cEmailSnt")) +
                                ", nEmailSnt = " + SQLUtil.toSQL(json.get("nEmailSnt")) +
                                ", cInactive = " + SQLUtil.toSQL(json.get("cInactive"));
                                
                                if ("".equals((String) json.get("dActivatd"))){
                                    sql += ", dActivatd = NULL";
                                } else {
                                    sql += ", dActivatd = " + SQLUtil.toSQL(json.get("dActivatd"));
                                }
                                
                                if ("".equals((String) json.get("dCreatedx"))){
                                    sql += ", dCreatedx = NULL";
                                } else {
                                    sql += ", dCreatedx = " + SQLUtil.toSQL(json.get("dCreatedx"));
                                }
                        
                                if ("".equals((String) json.get("dInactive"))){
                                    sql += ", dInactive = NULL";
                                } else {
                                    sql += ", dInactive = " + SQLUtil.toSQL(json.get("dInactive"));
                                }

                                if ("".equals((String) json.get("dFirstLog"))){
                                    sql += ", dFirstLog = NULL";
                                } else {
                                    sql += ", dFirstLog = " + SQLUtil.toSQL(json.get("dFirstLog"));
                                }
                                
                                if ("".equals((String) json.get("dLastLogx"))){
                                    sql += ", dLastLogx = NULL";
                                } else {
                                    sql += ", dLastLogx = " + SQLUtil.toSQL(json.get("dLastLogx"));
                                }
                                
                        sql += ", dTimeStmp = " + SQLUtil.toSQL(json.get("dTimeStmp"));
                    }
                    
                    System.out.println(sql);
                    if (instance.executeUpdate(sql) <= 0){
                        System.err.println(instance.getMessage() + instance.getErrMsg());
                        System.exit(1);
                    }
                }
            }
            
            System.out.println("Done. Thank you.");
            System.exit(0);
        } catch (IOException | ParseException | SQLException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
    
    
}
