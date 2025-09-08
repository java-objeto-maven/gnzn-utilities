package org.guanzon.gnzn.utilities.mis;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;

public class UpdateTyphoonHoliday {
    public static void main(String[] args) {
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
            
            String lsSQL = "SELECT" +
                                "  c.sCompnyNm" +
                                ", d.sDeptName" +
                                ", a.*" +
                                ", e.dTimeInAM" +
                                ", e.dTmeOutAM" +
                                ", e.dTimeInPM" +
                                ", e.dTmeOutPM" +
                                ", b.sEmpLevID" +
                                ", a.sEmployID" +
                            " FROM Employee_Log a" +
                                " LEFT JOIN Shift e" + 
                                    " ON a.sShiftIDx = e.sShiftIDx" +
                                ", Employee_Master001 b" +
                                    " LEFT JOIN Client_Master c" +
                                        " ON b.sEmployID = c.sClientID" +
                                    " LEFT JOIN Department d" +
                                    " ON b.sDeptIDxx = d.sDeptIDxx" +
                            " WHERE a.sEmployID = b.sEmployID" +
                                " AND a.sBranchCd IN ('M001', 'M0W1')" +
                                " AND a.dTransact = '2024-10-24'" +
                                " AND b.cSalTypex <> 'S'" +
                                " AND a.sEmployID NOT IN ('M00107002263', 'M00110017110', 'M00117003242', 'M00119001128'," +
                                    "'M00104002087', 'M00117002700', 'M00119002188', 'M00119002346', 'M00119002355'," +
                                    "'M00119002364', 'M00119002377', 'M00119002381', 'M00119002391', 'M00120000419'," +
                                    "'M00122000869', 'M00122000884', 'M00122000888', 'M00123000096', 'M00123000506'," +
                                    "'M00123000768', 'M00124000936', 'N00419000001', 'N00419000027', 'N00420000001'," +
                                    "'M00105000084', 'M00108004560', 'M00109003037', 'M00109019180', 'M02806000034')";
            
            ResultSet loRS = instance.executeQuery(lsSQL);
            if (MiscUtil.RecordCount(loRS) <= 0){
                System.out.println("No record to update.");
                System.exit(0);
            }
            
            instance.beginTrans();
            
            String lsField;
            while (loRS.next()){
                //check tranline
                if (loRS.getInt("nTranLine") == 2){
                    lsField = "dAMOutxxx";
                } else {
                    lsField = "dPMOutxxx";
                }
                
                //check out if less than 12nn; treat as half day
                // Define the time format
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                LocalTime time1 = LocalTime.parse("12:00:00", formatter);
                LocalTime time2 = LocalTime.parse(loRS.getString(lsField), formatter);
                
                Duration duration = Duration.between(time1, time2);
                
                if (duration.toHours() <= 0){ //time out is 1PM and below
                    if (Integer.parseInt(loRS.getString("sEmpLevID")) == 0){
                        //half day for RNF
                        //System.out.println(loRS.getString("sCompnyNm") + "\t" + loRS.getString("sEmpLevID") + "\t" + "HALFDAY");
                        lsSQL = "UPDATE Employee_Timesheet SET" +
                                    "  cAbsentxx = '0'" +
                                    ", cHolidayx = '0'" +
                                    ", cDeductxx = '0'" + 
                                    ", nTardyxxx = '0'" +
                                    ", nUndrTime = " + 240 +
                                    ", nUnOffOTx = 0" +
                                    ", nNightDif = 0" +
                                    ", nPayRatex = 1.00" +
                                " WHERE sEmployID = " + SQLUtil.toSQL(loRS.getString("sEmployID")) +
                                    " AND dTransact = '2024-10-24'";
                    } else {
                        //whole day for bisor and up
                        //System.out.println(loRS.getString("sCompnyNm") + "\t" + loRS.getString("sEmpLevID") + "\t" + "WHOLEDAY");
                        lsSQL = "UPDATE Employee_Timesheet SET" +
                                    "  cAbsentxx = '0'" +
                                    ", cHolidayx = '0'" +
                                    ", cDeductxx = '0'" + 
                                    ", nTardyxxx = '0'" +
                                    ", nUndrTime = '0'" +
                                    ", nUnOffOTx = 0" +
                                    ", nNightDif = 0" +
                                    ", nPayRatex = 1.00" +
                                " WHERE sEmployID = " + SQLUtil.toSQL(loRS.getString("sEmployID")) +
                                    " AND dTransact = '2024-10-24'";
                    }
                } else {
//                    if (Integer.parseInt(loRS.getString("sEmpLevID")) == 0){
//                        //half day for RNF
//                        time1 = LocalTime.parse(loRS.getString("dAMInxxxx"), formatter);                        
//                        
//                        duration = Duration.between(time1, time2);
//                        
//                        if (duration.toHours() >= 6){
//                            //System.out.println(loRS.getString("sCompnyNm") + "\t" + loRS.getString("sEmpLevID") + "\t" + "WHOLEDAY");
//                            lsSQL = "UPDATE Employee_Timesheet SET" +
//                                    "  cAbsentxx = '0'" +
//                                    ", cHolidayx = '0'" +
//                                    ", cDeductxx = '0'" + 
//                                    ", nTardyxxx = '0'" +
//                                    ", nUndrTime = '0'" +
//                                    ", nUnOffOTx = 0" +
//                                    ", nNightDif = 0" +
//                                    ", nPayRatex = 1.00" +
//                                " WHERE sEmployID = " + SQLUtil.toSQL(loRS.getString("sEmployID")) +
//                                    " AND dTransact = '2024-10-24'";
//                        } else {
//                            //System.out.println(loRS.getString("sCompnyNm") + "\t" + loRS.getString("sEmpLevID") + "\t" + "UNDERTIME " + (540 - duration.toMinutes()));
//                            lsSQL = "UPDATE Employee_Timesheet SET" +
//                                    "  cAbsentxx = '0'" +
//                                    ", cHolidayx = '0'" +
//                                    ", cDeductxx = '0'" + 
//                                    ", nTardyxxx = 0" +
//                                    ", nUndrTime = " + (540 - duration.toMinutes()) +
//                                    ", nUnOffOTx = 0" +
//                                    ", nNightDif = 0" +
//                                    ", nPayRatex = 1.00" +
//                                " WHERE sEmployID = " + SQLUtil.toSQL(loRS.getString("sEmployID")) +
//                                    " AND dTransact = '2024-10-24'";
//                        }
//                    } else {
//                        //whole day for bisor and up
//                        //System.out.println(loRS.getString("sCompnyNm") + "\t" + loRS.getString("sEmpLevID") + "\t" + "WHOLEDAY");
//                        lsSQL = "UPDATE Employee_Timesheet SET" +
//                                    "  cAbsentxx = '0'" +
//                                    ", cHolidayx = '0'" +
//                                    ", cDeductxx = '0'" + 
//                                    ", nTardyxxx = '0'" +
//                                    ", nUndrTime = '0'" +
//                                    ", nUnOffOTx = 0" +
//                                    ", nNightDif = 0" +
//                                    ", nPayRatex = 1.00" +
//                                " WHERE sEmployID = " + SQLUtil.toSQL(loRS.getString("sEmployID")) +
//                                    " AND dTransact = '2024-10-24'";
//                    }
                    
                    lsSQL = "UPDATE Employee_Timesheet SET" +
                                    "  cAbsentxx = '0'" +
                                    ", cHolidayx = '0'" +
                                    ", cDeductxx = '0'" + 
                                    ", nTardyxxx = '0'" +
                                    ", nUndrTime = '0'" +
                                    ", nUnOffOTx = 0" +
                                    ", nNightDif = 0" +
                                    ", nPayRatex = 1.00" +
                                " WHERE sEmployID = " + SQLUtil.toSQL(loRS.getString("sEmployID")) +
                                    " AND dTransact = '2024-10-24'";
                }
                
                instance.executeQuery(lsSQL, "Employee_Timesheet", instance.getBranchCode(), "");
            }      
            
            instance.commitTrans();
        } catch (IOException | SQLException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        
        System.exit(0);
    }
}
