package org.guanzon.gnzn.utilities.api;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.base.WebClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class POST_ganado_online {
    public static void main (String args[]){
        if (args.length == 0){
            System.out.println("Invalid parameters detected.");
            System.exit(1);
        }
                
        try {
            JSONObject json;
            JSONParser parser = new JSONParser();
            
            json = (JSONObject) parser.parse(args[0]);
            
            if (!json.containsKey("sUserIDxx") ||
                !json.containsKey("sPayloadx") ||
                !json.containsKey("bBatchExe")){
                System.out.println("Invalid API parameters detected.");
                System.exit(1);
            }
            
            String path;
            if(System.getProperty("os.name").toLowerCase().contains("win")){
                path = "D:/GGC_Maven_Systems";
            }
            else{
                path = "/srv/GGC_Maven_Systems";
            }
            System.setProperty("sys.default.path.config", path);
            
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
            
            String sql = "SELECT * FROM xxxSysUser WHERE sUserIDxx = " + SQLUtil.toSQL((String) json.get("sUserIDxx"));
            ResultSet ors = instance.executeQuery(sql);
            
            if (!ors.next()){
                System.err.println("Invalid User ID.");
                System.exit(1);
            }
            
            Map<String, String> headers = API.getWSHeader("TeleMktg");

            String response;
            
            response = WebClient.sendHTTP(API.GET_GANADO_ONLINE, json.toJSONString(), (HashMap<String, String>) headers);
            if(response == null){
                System.out.println("No Response");
                System.exit(1);
            } 
            
            json = (JSONObject) parser.parse(response);
            
            if (!"success".equals((String) json.get("result"))){
                System.err.println("Unable to POST queries.");
                System.exit(1);
            }
        } catch (ParseException | IOException | SQLException ex) {
            System.out.println(ex.getMessage());
            System.exit(1);
        }
        
        System.exit(0);
    }
}
