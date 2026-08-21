package org.guanzon.gnzn.utilities.goals;

import java.io.FileInputStream;
import java.sql.ResultSet;
import java.util.Properties;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.SQLUtil;

public class MobilePhoneArea {
    private static final String[] AREAS = {
        "0010", "0011", "0012", "0013", "0016", "0020", "0024", "0028"
    };
    
    private static final String[] MONTH_PERIODS = {
        "01", "02", "03", "04", "05", "06",
        "07", "08", "09", "10", "11", "12"
    };
    
    public static void main(String[] args) {
        String path = System.getProperty("os.name").toLowerCase().contains("win")
                ? "D:/GGC_Maven_Systems"
                : "/srv/GGC_Maven_Systems";
        System.setProperty("sys.default.path.config", path);
 
        try {
            Properties poProps = new Properties();
            try (FileInputStream propsIn = new FileInputStream(path + "/config/cas.properties")) {
                poProps.load(propsIn);
            }
 
            if (!"1".equals(poProps.getProperty("developer.mode"))) {
                System.err.println("Unable to log user.");
                System.exit(1);
                return;
            }
 
            GRider instance = new GRider("gRider");
            if (!instance.logUser("gRider", "M001000001")) {
                System.exit(1);
                return;
            }           
            
            String lsSQL;
            ResultSet loRS;
            
            
            for (int lnCtr = 0; lnCtr <= AREAS.length - 1; lnCtr++){
                instance.beginTrans();
                for (int month = 0; month <= MONTH_PERIODS.length - 1; month++){
                    lsSQL = "SELECT SUM(nCPGoalxx) nCPGoalxx" +
                            " FROM MP_Branch_Performance" +
                            " WHERE sPeriodxx = " + SQLUtil.toSQL("2026" + MONTH_PERIODS[month]) +
                                " AND sBranchCd IN (SELECT a.sBranchCd" + 
                                                    " FROM Branch a" +
                                                        ", Branch_Others b" +
                                                    " WHERE a.sBranchCd = b.sBranchCd" +
                                                        " AND b.sAreaCode = " + SQLUtil.toSQL(AREAS[lnCtr]) + ")";
                    
                    loRS = instance.executeQuery(lsSQL);
                    
                    if (loRS.next()){
                        lsSQL = "INSERT INTO MP_Area_Performance SET" +
                                    "  sAreaCode = " + SQLUtil.toSQL(AREAS[lnCtr]) +
                                    ", sPeriodxx = " + SQLUtil.toSQL("2026" + MONTH_PERIODS[month]) +
                                    ", nCPGoalxx = " + loRS.getDouble("nCPGoalxx") + 
                                    ", dModified = " + SQLUtil.toSQL(instance.getServerDate()) +
                                " ON DUPLICATE KEY UPDATE" +
                                    "  nCPGoalxx = " + loRS.getDouble("nCPGoalxx") + 
                                    ", dModified = " + SQLUtil.toSQL(instance.getServerDate());
                        
                        if (instance.executeQuery(lsSQL, "MP_Area_Performance", instance.getBranchCode(), "") <= 0){
                            System.err.println("Unable to execute command: " + lsSQL);
                            instance.rollbackTrans();
                            System.exit(1);
                        }
                    }
                }
                instance.commitTrans();
            }
            
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
