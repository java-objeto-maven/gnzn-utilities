package org.guanzon.gnzn.utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.LogWrapper;

public class KitaMoToAgent {
    public static void main(String[] args) {
        LogWrapper logwrapr = new LogWrapper("KitaMoToAgent", "Ganado.log");
        
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
            
            //get agents that is not yet tagged as customer
//            
//            Select
//	  a.sReferdBy
//	, b.sMPlaceID
//from Ganado_Online a
//	, App_User_Master b
//WHERE a.`sReferdBy` = b.`sUserIDxx`
//	AND a.cSourcexx = '1'
//	AND a.cTranStat <> '3'
//	AND IFNULL(b.cGnznCltx, '0') = '0'
//GROUP BY a.sReferdBy;
//
//Select *
//from App_User_Profile
//WHERE sUserIDxx = 'GAP024000057'
//GROUP BY sClientID;
//
//SELECT
//	  sTransNox
//	, nTranTotl
//FROM MC_SO_Master 
//WHERE sClientID = 'M02816000604'
//UNION
//SELECT
//	  sTransNox
//	, nTranTotl
//FROM JobOrderBranch_Master 
//WHERE sClientID = 'M02816000604'
//UNION
//SELECT
//	  sTransNox
//	, nTranTotl
//FROM JobOrder_Master 
//WHERE sClientID = 'M02816000604'
//UNION
//SELECT
//	  sTransNox
//	, nTranTotl
//FROM SP_SO_Master 
//WHERE sClientID = 'M02816000604';
        } catch (IOException e) {
            System.exit(1);
        }
        
        System.exit(0);
    }
}
