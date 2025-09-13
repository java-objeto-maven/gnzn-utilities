package org.guanzon.gnzn.utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.gnzn.utilities.lib.cp.Bolttech;
import org.json.simple.JSONObject;

public class SendBolttech {
    public static void main(String[] args) {
        LogWrapper logwrapr = new LogWrapper("SendBolttech", "bolttech.log");
        
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
            
            JSONObject json = trans.UploadFile();
            
            if (!((String) json.get("result")).equals("success")){
                logwrapr.severe((String) json.get("message"));
            }
        } catch (IOException e) {
            System.exit(1);
        }
        
        System.exit(0);
    }
}