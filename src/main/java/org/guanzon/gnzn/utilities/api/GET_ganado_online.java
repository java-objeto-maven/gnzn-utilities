
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

public class GET_ganado_online {
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
                    System.out.println("Timestamp as parameter.");
                } else {
                    json.put("sTransNox", param);
                    json.put("dTimeStmp", "");
                    System.out.println("Transaction number as parameter.");
                }
            } else {
                sql = "SELECT dTransact FROM Ganado_Online ORDER BY dTransact DESC LIMIT 1";
                ors = instance.executeQuery(sql);
                
                json.put("dTransact", "1900-01-01 00:00:01");
                
                if (ors.next()){
                    json.put("dTransact", ors.getString("dTransact"));
                }
                
                System.out.println("Transaction date as parameter. " + json.get("dTransact"));
            }

            JSONObject headers = new JSONObject();
            System.out.println(System.getProperty("sys.default.path.config") + "/access.token");
            headers.put("g-access-token", API.getAccessToken(System.getProperty("sys.default.path.config") + "/access.token"));

            String response;
            
            response = WebClient.sendHTTP(API.GET_GANADO_ONLINEX, json.toJSONString(), (HashMap<String, String>) headers);
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
                    
                    sql = "SELECT sTransNox FROM Ganado_Online WHERE sTransNox = " + SQLUtil.toSQL(json.get("sTransNox"));
                    ors = instance.executeQuery(sql);
                    
                    if (ors.next()){
                        sql = "UPDATE Ganado_Online SET" +
                                    "  dTransact = " + SQLUtil.toSQL(json.get("dTransact")) +
                                    ", sClientNm = " + SQLUtil.toSQL(json.get("sClientNm")) +
                                    ", cSourcexx = " + SQLUtil.toSQL(json.get("cSourcexx")) +
                                    ", cGanadoTp = " + SQLUtil.toSQL(json.get("cGanadoTp")) +
                                    ", cPaymForm = " + SQLUtil.toSQL(json.get("cPaymForm")) +
                                    ", sCltInfox = " + SQLUtil.toSQL(json.get("sCltInfox")) +
                                    ", sFinancex = " + SQLUtil.toSQL(json.get("sFinancex")) +
                                    ", sPrdctInf = " + SQLUtil.toSQL(json.get("sPrdctInf")) +
                                    ", sPaymInfo = " + SQLUtil.toSQL(json.get("sPaymInfo")) +
                                    ", sCltInfoF = " + SQLUtil.toSQL(json.get("sCltInfoF")) +
                                    ", sFinanceF = " + SQLUtil.toSQL(json.get("sFinanceF")) +
                                    ", sPrdctxxF = " + SQLUtil.toSQL(json.get("sPrdctxxF")) +
                                    ", sPaymInfF = " + SQLUtil.toSQL(json.get("sPaymInfF"));
                        
                        if ("".equals((String) json.get("dTargetxx"))){
                            sql += ", dTargetxx = NULL";
                        } else {
                            sql += ", dTargetxx = " + SQLUtil.toSQL(json.get("dTargetxx"));
                        }
                        
                        if ("".equals((String) json.get("dFollowUp"))){
                            sql += ", dFollowUp = NULL";
                        } else {
                            sql += ", dFollowUp = " + SQLUtil.toSQL(json.get("dFollowUp"));
                        }
                        
                        sql = sql +
                                    ", sRemarksx = " + SQLUtil.toSQL(json.get("sRemarksx")) +
                                    ", sReferdBy = " + SQLUtil.toSQL(json.get("sReferdBy")) +
                                    ", sRelatnID = " + SQLUtil.toSQL(json.get("sRelatnID")) +
                                    ", dCreatedx = " + SQLUtil.toSQL(json.get("dCreatedx")) +
                                    ", nLatitude = " + SQLUtil.toSQL(json.get("nLatitude")) +
                                    ", nLongitud = " + SQLUtil.toSQL(json.get("nLongitud")) +
                                    ", sClientID = " + SQLUtil.toSQL(json.get("sClientID")) +
                                    ", sTLMAgent = " + SQLUtil.toSQL(json.get("sTLMAgent")) +
                                    ", cTranStat = " + SQLUtil.toSQL(json.get("cTranStat")) +
                                    ", sModified = " + SQLUtil.toSQL(json.get("sModified")) +
                                    ", dModified = " + SQLUtil.toSQL(json.get("dModified")) +
                                    ", dTimeStmp = " + SQLUtil.toSQL(json.get("dTimeStmp")) +
                                " WHERE sTransNox = " + SQLUtil.toSQL(json.get("sTransNox"));
                    } else {
                        sql = "INSERT INTO Ganado_Online SET" +
                                    "  sTransNox = " + SQLUtil.toSQL(json.get("sTransNox")) +
                                    ", dTransact = " + SQLUtil.toSQL(json.get("dTransact")) +
                                    ", sClientNm = " + SQLUtil.toSQL(json.get("sClientNm")) +
                                    ", cSourcexx = " + SQLUtil.toSQL(json.get("cSourcexx")) +
                                    ", cGanadoTp = " + SQLUtil.toSQL(json.get("cGanadoTp")) +
                                    ", cPaymForm = " + SQLUtil.toSQL(json.get("cPaymForm")) +
                                    ", sCltInfox = " + SQLUtil.toSQL(json.get("sCltInfox")) +
                                    ", sFinancex = " + SQLUtil.toSQL(json.get("sFinancex")) +
                                    ", sPrdctInf = " + SQLUtil.toSQL(json.get("sPrdctInf")) +
                                    ", sPaymInfo = " + SQLUtil.toSQL(json.get("sPaymInfo")) +
                                    ", sCltInfoF = " + SQLUtil.toSQL(json.get("sCltInfoF")) +
                                    ", sFinanceF = " + SQLUtil.toSQL(json.get("sFinanceF")) +
                                    ", sPrdctxxF = " + SQLUtil.toSQL(json.get("sPrdctxxF")) +
                                    ", sPaymInfF = " + SQLUtil.toSQL(json.get("sPaymInfF"));
                        
                        if ("".equals((String) json.get("dTargetxx"))){
                            sql += ", dTargetxx = NULL";
                        } else {
                            sql += ", dTargetxx = " + SQLUtil.toSQL(json.get("dTargetxx"));
                        }
                        
                        if ("".equals((String) json.get("dFollowUp"))){
                            sql += ", dFollowUp = NULL";
                        } else {
                            sql += ", dFollowUp = " + SQLUtil.toSQL(json.get("dFollowUp"));
                        }
                        
                        sql = sql +
                                    ", sRemarksx = " + SQLUtil.toSQL(json.get("sRemarksx")) +
                                    ", sReferdBy = " + SQLUtil.toSQL(json.get("sReferdBy")) +
                                    ", sRelatnID = " + SQLUtil.toSQL(json.get("sRelatnID")) +
                                    ", dCreatedx = " + SQLUtil.toSQL(json.get("dCreatedx")) +
                                    ", nLatitude = " + SQLUtil.toSQL(json.get("nLatitude")) +
                                    ", nLongitud = " + SQLUtil.toSQL(json.get("nLongitud")) +
                                    ", sClientID = " + SQLUtil.toSQL(json.get("sClientID")) +
                                    ", sTLMAgent = " + SQLUtil.toSQL(json.get("sTLMAgent")) +
                                    ", cTranStat = " + SQLUtil.toSQL(json.get("cTranStat")) +
                                    ", sModified = " + SQLUtil.toSQL(json.get("sModified")) +
                                    ", dModified = " + SQLUtil.toSQL(json.get("dModified")) +
                                    ", dTimeStmp = " + SQLUtil.toSQL(json.get("dTimeStmp"));
                    }
                    
                    System.out.println(sql);
                    if (instance.executeUpdate(sql) <= 0){
                        System.err.println(instance.getMessage() + instance.getErrMsg());
                        System.exit(1);
                    }
                }
            }else {
                System.out.println(json.toJSONString());
                System.exit(1);
            }
            
            System.out.println("Done. Thank you.");
            System.exit(0);
        } catch (IOException | ParseException | SQLException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }    
}
