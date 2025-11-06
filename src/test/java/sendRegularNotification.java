
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.base.WebClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class sendRegularNotification {
    public static void main(String [] args) throws IOException{
        sendNotification("gRider", 
                            "GAP0190004", 
                            "Test notification", 
                            "This is a tetst.");
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
}
