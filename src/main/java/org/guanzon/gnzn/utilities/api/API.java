package org.guanzon.gnzn.utilities.api;

import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.token.RequestAccess;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class API {
    public static String GET_GANADO_ONLINE = "https://restgk.guanzongroup.com.ph/gcircle/ganado/download_ganado.php";
    public static String GET_GANADO_ONLINEX = "https://restgk.guanzongroup.com.ph/x-api/v1.0/ganado/download_ganado.php";
    public static String GET_APP_USER = "https://restgk.guanzongroup.com.ph/x-api/v1.0/ganado/download_app_user.php";
    
    public static HashMap getWSHeader(String fsProdctID){
        String clientid = "GGC_BM001";
        String productid = fsProdctID;
        String imei = "GMC_SEG09";
        String user = "M001111122";
        String log = "";
        
        Calendar calendar = Calendar.getInstance();
        Map<String, String> headers = 
                        new HashMap<String, String>();
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json");
        headers.put("g-api-id", productid);
        headers.put("g-api-imei", imei);
        
        headers.put("g-api-key", SQLUtil.dateFormat(calendar.getTime(), "yyyyMMddHHmmss"));        
        headers.put("g-api-hash", org.apache.commons.codec.digest.DigestUtils.md5Hex((String)headers.get("g-api-imei") + (String)headers.get("g-api-key")));
        headers.put("g-api-client", clientid);    
        headers.put("g-api-user", user);    
        headers.put("g-api-log", log);    
        headers.put("g-char-request", "UTF-8");
        headers.put("g-api-token", "");    
        
        return (HashMap) headers;
    }
    
    public static String getAccessToken(String access){
        try {            
            JSONParser oParser = new JSONParser();
            JSONObject token = (JSONObject)oParser.parse(new FileReader(access));

            Calendar current_date = Calendar.getInstance();
            current_date.add(Calendar.MINUTE, -25);
            Calendar date_created = Calendar.getInstance();
            date_created.setTime(SQLUtil.toDate((String) token.get("created") , SQLUtil.FORMAT_TIMESTAMP));
            
            //Check if token is still valid within the time frame
            //Request new access token if not in the current period range
            if(current_date.after(date_created)){
                String[] xargs = new String[] {(String) token.get("parent"), access};
                RequestAccess.main(xargs);
                token = (JSONObject)oParser.parse(new FileReader(access));
            }
            
            return (String)token.get("access_key");
        } catch (IOException | ParseException ex) {
            System.err.println(ex.getMessage());
            return null;
        }
    }
}
