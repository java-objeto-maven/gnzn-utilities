package org.guanzon.gnzn.utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.gnzn.utilities.lib.cp.Bolttech;
import org.json.simple.JSONObject;

public class CreateBolttechReport {
    public static void main(String[] args) {
        LogWrapper logwrapr = new LogWrapper("CreateBolttechReport", "bolttech.log");
        
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Maven_Systems";
        }
        else{
            path = "/srv/GGC_Maven_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(path + "/config/cas.properties"));
            
            GRider instance = null;
                    
            if (props.getProperty("developer.mode").equals("1")){
                instance = new GRider("gRider");
        
                if (!instance.logUser("gRider", "M001000001")){
                    System.err.println(instance.getErrMsg());
                    System.exit(1);
                }
            } else {
                System.err.println("Unable to log user.");
                System.exit(1);
            }
            
            Bolttech trans = new Bolttech(instance);
            
            JSONObject json;
            
            json = trans.CreateReport("2024-09-01", "2024-09-30");
            
            if (!((String) json.get("result")).equals("success")){
                logwrapr.severe((String) json.get("message"));
            }
        } catch (IOException e) {
            System.exit(1);
        }
        
        System.exit(0);
    }
}
