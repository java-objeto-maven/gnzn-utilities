package org.guanzon.gnzn.utilities.api;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;

public class API {
    public static String GET_GANADO_ONLINE = "http://localhost/gcircle/ganado/download_ganado.php";
    
    public static String POST_GANADO_ONLINE = "http://localhost/system/execute.php";
    
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
}
