package org.guanzon.gnzn.utilities.tlm;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import org.guanzon.appdriver.base.GRider;

public class CreateLeads {
    public static void main(String[] args) {
        if (args.length != 2){
            System.err.println("Invalid parameter detected.");
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
        
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(path + "/config/cas.properties"));
            
            GRider instance = null;
                    
            if (props.getProperty("developer.mode").equals("1")){
                instance = new GRider("TeleMktg");
        
                if (!instance.logUser("TeleMktg", args[0])){
                    System.err.println(instance.getErrMsg());
                    System.exit(1);
                }
            } else {
                System.err.println("Unable to log user.");
                System.exit(1);
            }
            
            System.setProperty("leads.follow.up.backdate", props.getProperty("leads.follow.up.backdate"));
            System.setProperty(("leads.ca.grace.period"), props.getProperty("leads.ca.grace.period"));
            System.setProperty("leads.inquiry.first", props.getProperty("leads.inquiry.first"));
            System.setProperty("leads.inquiry.grace.period", props.getProperty("leads.inquiry.grace.period"));
            
            CallLeads trans = new CallLeads(instance);
            trans.setNetworkProvider(args[1]);
            
            if (trans.GetNextCustomer()){
                System.out.println("New customer added to leads.");
            }else {
                System.err.println("No customer was retreived.");
                System.exit(1);
            }
            
        } catch (IOException |SQLException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        
        System.exit(0);
    }
}