package org.guanzon.gnzn.utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.gnzn.utilities.lib.hcm.ILoveMyJob;
import org.guanzon.gnzn.utilities.lib.hcm.ILoveMyJobValidator;

public class CreateILMJ {
    public static void main(String[] args) {
        LogWrapper logwrapr = new LogWrapper("CreateILMJ", "gnzn-utilities.log");
        logwrapr.info("Start of Process!");
        
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
                instance = new GRider("gRider");
        
                if (!instance.logUser("gRider", "M001000001")){
                    System.err.println(instance.getErrMsg());
                    System.exit(1);
                }
            } else {
                System.err.println("Unable to log user.");
                System.exit(1);
            }
            
            ILoveMyJobValidator utility;
            
            utility = ILoveMyJob.make(ILoveMyJob.Type.EMPLOYEE);
            utility.setGRider(instance);
            utility.setWithParent(true);

            if (!utility.Run("MX01", SQLUtil.dateFormat(instance.getServerDate(), SQLUtil.FORMAT_SHORT_DATE), "")){
                System.err.println(utility.getMessage());
                logwrapr.info("Error!!!");
                System.exit(1);
            }
            
            System.out.print("Raffle entries created successfully.");
            
        } catch (IOException e) {
            System.exit(1);
        }
        
        System.exit(0);
    }
}
