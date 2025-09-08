package org.guanzon.gnzn.utilities.mis;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.guanzon.appdriver.base.GRider;

public class CreateConventionQRCode {
    public static void main(String[] args) {
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Maven_Systems";
        }
        else{
            path = "/srv/GGC_Maven_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        
        GRider instance = null;

        instance = new GRider("gRider");

        if (!instance.logUser("gRider", "M001111122")){
            System.err.println(instance.getErrMsg());
            System.exit(1);
        }
        
        System.exit(0);
    }
}
