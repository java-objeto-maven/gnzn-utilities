package org.guanzon.gnzn.utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.gnzn.utilities.lib.css.CollectionCallCenterLeads;
import org.json.simple.JSONObject;

public class Create3CLeads {
    public static void main(String[] args) throws SQLException {
        LogWrapper logwrapr = new LogWrapper("Create3CLeads", "3C.log");
        
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
            
            CollectionCallCenterLeads trans = new CollectionCallCenterLeads(instance);
            
            JSONObject json;
            
            json = trans.Create();
            
            if (!((String) json.get("result")).equals("success")){
                logwrapr.severe((String) json.get("message"));
                System.exit(1);
            } 
        } catch (IOException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
        
        System.exit(0);
    }
}
