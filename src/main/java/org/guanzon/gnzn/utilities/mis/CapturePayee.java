package org.guanzon.gnzn.utilities.mis;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.MiscUtil;

public class CapturePayee {
    public static void main (String [] args) throws SQLException, GuanzonException{
        LogWrapper logwrapr = new LogWrapper("ConvertSysUser", "mis.log");
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
            
            GRiderCAS instance = null;
                    
            if (po_props.getProperty("developer.mode").equals("1")){
                instance = new GRiderCAS("gRider");
        
                if (!instance.logUser("gRider", "M001000001")){
                    logwrapr.warning(instance.getMessage());
                    System.exit(1);
                }
            } else {
                System.err.println("Unable to log user.");
                System.exit(1);
            }
            
            String lsSQL = "SELECT" +
                                "  sPayeeIDx" +
                                ", sPayeeNme" +
                                ", IFNULL(sPrtclrID, '') sPrtclrID" +
                                ", IFNULL(sAPClntID, '') sAPClntID" +
                                ", IFNULL(sClientID, '') sClientID" +
                            " FROM GGC_ISysDBF.Payee" +
                            " WHERE sPayeeIDx IN (" + 
                                    "SELECT DISTINCT(sPayeeIDx) sPayeeIDx" +
                                    " FROM GGC_ISysDBF.Check_Disbursement" +
                                    " WHERE dTransact BETWEEN '2021-01-01' AND '2026-03-30')" +
                                " AND cRecdStat = '1'";
            
            ResultSet loRS = instance.executeQuery(lsSQL);
            
            System.out.println(MiscUtil.RecordCount(loRS));
        } catch (IOException e) {
            System.exit(1);
        }
    }
}