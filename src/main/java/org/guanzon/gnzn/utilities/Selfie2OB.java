package org.guanzon.gnzn.utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;

/**
 *
 * @author Michael Cuison
 */
public class Selfie2OB {
    public static void main (String [] args){
        LogWrapper logwrapr = new LogWrapper("Selfie2OB", "selfie2ob.log");
        final String BRANCHCD = "MX01";
        
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
            
            //get date start of cutoff, assuming the cutoff period is 1-15 and 16-last day of the month
            String dateThru = SQLUtil.dateFormat(CommonUtils.dateAdd(instance.getServerDate(), -1), SQLUtil.FORMAT_SHORT_DATE);
            String dateFrom = dateThru;
            
            if (Integer.parseInt(dateThru.substring(9)) <= 5){
                dateFrom = SQLUtil.dateFormat(CommonUtils.dateAdd(instance.getServerDate(), -15), SQLUtil.FORMAT_SHORT_DATE);
            } else {
                dateFrom = dateFrom.substring(0, 8) + "01";
            }
            
            String lsSQL ="SELECT" +
                                "  a.sEmployID" +
                                ", a.sBranchCd" +
                                ", a.dLogTimex" +
                                ", b.sBranchNm" +
                                ", DATE_FORMAT(a.dLogTimex, '%Y-%m-%d') dTransact" +
                                ", a.sTransNox" +
                                ", d.sCompnyNm" +
                            " FROM Employee_Log_Selfie a" +
                                " LEFT JOIN Branch b ON a.sBranchCd = b.sBranchCd" +
                                ", Employee_Master001 c" +
                                " LEFT JOIN Client_Master d" +
                                    " ON c.sEmployID = d.sClientID" +
                            " WHERE a.sEmployID = c.sEmployID" +
                                " AND c.cSalTypex = 'S'" +
                                " AND a.cCaptured = '0'" +
                                " AND a.sBranchCd <> ''" +
                                " AND DATE_FORMAT(a.dLogTimex, '%Y-%m-%d') BETWEEN " +
                                   SQLUtil.toSQL(dateFrom) + " AND " +
                                   SQLUtil.toSQL(dateThru) +
                            " GROUP BY a.sEmployID, dTransact" +
                            " ORDER BY a.sEmployID, a.dLogTimex, a.sBranchCd";
            
            ResultSet loRS = instance.executeQuery(lsSQL);
            
            //grouped ob
            while(loRS.next()){
                lsSQL = "SELECT" +
                                "  a.sEmployID" +
                                ", a.sBranchCd" +
                                ", a.dLogTimex" +
                                ", b.sBranchNm" +
                                ", DATE_FORMAT(a.dLogTimex, '%Y-%m-%d') dTransact" +
                                ", a.sTransNox" +
                                ", d.sCompnyNm" +
                             " FROM Employee_Log_Selfie a" +
                                   " LEFT JOIN Branch b ON a.sBranchCd = b.sBranchCd" +
                                ", Employee_Master001 c" +
                                    " LEFT JOIN Client_Master d" +
                                        " ON c.sEmployID = d.sClientID" +
                             " WHERE a.sEmployID = c.sEmployID" +
                                " AND c.cSalTypex = 'S'" +
                                " AND a.cCaptured = '0'" +
                                " AND a.sBranchCd <> ''" +
                                " AND DATE_FORMAT(a.dLogTimex, '%Y-%m-%d') BETWEEN " +
                                   SQLUtil.toSQL(dateFrom) + " AND " +
                                   SQLUtil.toSQL(dateThru) +
                             " ORDER BY a.sEmployID, a.dLogTimex, a.sBranchCd";
                
                lsSQL = MiscUtil.addCondition(lsSQL, "a.sEmployID = " + SQLUtil.toSQL(loRS.getString("sEmployID")) + 
                                                        " AND DATE_FORMAT(a.dLogTimex, '%Y-%m-%d') = " + SQLUtil.toSQL(loRS.getString("dTransact")));
                
                //individual ob per date
                ResultSet ind = instance.executeQuery(lsSQL);
                
                String transactionNo = "";
                String employeeId = "";
                String transactionDate = "";
                String remarks = "selfie log -";
                
                //prepare the entry for business trip
                while(ind.next()){
                    employeeId = ind.getString("sEmployID");
                    transactionDate = ind.getString("dTransact");
                    transactionNo += ", " + SQLUtil.toSQL(ind.getString("sTransNox"));
                    remarks += " " + ind.getString("sBranchCd");
                }
                
                //check if the employee OB application
                lsSQL = "SELECT * FROM Employee_Business_Trip" +
                        " WHERE sEmployID = " + SQLUtil.toSQL(loRS.getString("sEmployID")) +
                           " AND " + SQLUtil.toSQL(loRS.getString("dTransact")) + " BETWEEN dDateFrom AND dDateThru" +
                           " AND cTranStat IN ('1', '2')";
                
                ResultSet ob = instance.executeQuery(lsSQL);
                
                instance.beginTrans();
                if (ob.next()){
                    //update the status of selfie logs
                    lsSQL = "UPDATE Employee_Log_Selfie SET" +
                                " cCaptured = 'x'" +
                            " WHERE sTransNox IN (" + transactionNo.substring(2) + ")";
                    
                    System.out.println(lsSQL);
                    if (instance.executeQuery(lsSQL, "Employee_Log_Selfie", BRANCHCD, "") <= 0){
                        instance.rollbackTrans();
                        logwrapr.severe(lsSQL);
                        logwrapr.severe(instance.getErrMsg());
                        System.exit(1);
                    }
                } else {
                    //create business trip
                    lsSQL = MiscUtil.getNextCode("Employee_Business_Trip", "sTransNox", true, instance.getConnection(), BRANCHCD);
                    
                    lsSQL = "INSERT INTO Employee_Business_Trip SET" +
                            "  sTransNox = " + SQLUtil.toSQL(lsSQL) +
                            ", dTransact = " + SQLUtil.toSQL(instance.getServerDate()) +
                            ", sEmployID = " + SQLUtil.toSQL(employeeId) +
                            ", dDateFrom = " + SQLUtil.toSQL(transactionDate) +
                            ", dDateThru = " + SQLUtil.toSQL(transactionDate) +
                            ", sRemarksx = " + SQLUtil.toSQL(remarks) +
                            ", dAppldFrx = " + SQLUtil.toSQL(transactionDate) +
                            ", dAppldTox = " + SQLUtil.toSQL(transactionDate) +
                            ", sApproved = " + SQLUtil.toSQL(instance.getUserID()) +
                            ", dApproved = " + SQLUtil.toSQL(transactionDate) +
                            ", cTranStat = '1'" +
                            ", sModified = " + SQLUtil.toSQL(instance.getUserID()) +
                            ", dModified = " + SQLUtil.toSQL(instance.getServerDate());
                         
                    System.out.println(lsSQL);
                    if (instance.executeQuery(lsSQL, "Employee_Business_Trip", BRANCHCD, "") <= 0){
                        instance.rollbackTrans();
                        logwrapr.severe(lsSQL);
                        logwrapr.severe(instance.getErrMsg());
                        System.exit(1);
                    }
                    
                    //update the status of selfie logs
                    lsSQL = "UPDATE Employee_Log_Selfie SET" +
                                " cCaptured = '1'" +
                            " WHERE sTransNox IN (" + transactionNo.substring(2) + ")";
                    
                    System.out.println(lsSQL);
                    if (instance.executeQuery(lsSQL, "Employee_Log_Selfie", BRANCHCD, "") <= 0){
                        instance.rollbackTrans();
                        logwrapr.severe(lsSQL);
                        logwrapr.severe(instance.getErrMsg());
                        System.exit(1);
                    }
                    
                    //update timesheet
                    lsSQL = "DELETE FROM Employee_Timesheet" +
                            " WHERE sEmployID = " + SQLUtil.toSQL(employeeId) + 
                                " AND dTransact = " + SQLUtil.toSQL(transactionDate);
                    
                    System.out.println(lsSQL);
                    instance.executeQuery(lsSQL, "Employee_Timesheet", BRANCHCD, "");
                                       
                    lsSQL = "INSERT INTO Employee_Timesheet SET"  +
                            "  sEmployID = " + SQLUtil.toSQL(employeeId) +
                            ", dTransact = " + SQLUtil.toSQL(transactionDate) +
                            ", cAbsentxx = '0'" +
                            ", cLeavexxx = '0'" +
                            ", cHolidayx = '0'" +
                            ", cDeductxx = '0'" +
                            ", cRestDayx = '0'" +
                            ", nTardyxxx = 0" +
                            ", nOverTime = 0" +
                            ", nUndrTime = 0" +
                            ", nUnOffOTx = 0" +
                            ", nPayRatex = 1.00" +
                            ", nOTRatexx = 1.00" +
                            ", nNightDif = 0" +
                            ", nOTNghtDf = 0" +
                            ", nUnOTNght = 0" +
                            ", nOrigTard = 0" +
                            ", nOrigUndr = 0" +
                            ", nAttEqual = 1.00" +
                            ", cInvalidx = '0'";
                    
                    System.out.println(lsSQL);
                    if (instance.executeQuery(lsSQL, "Employee_Timesheet", BRANCHCD, "") <= 0){
                        instance.rollbackTrans();
                        logwrapr.severe(lsSQL);
                        logwrapr.severe(instance.getErrMsg());
                        System.exit(1);
                    }
                }
                instance.commitTrans();
            }
        } catch (IOException | SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
        
        System.exit(0);
    }
}
