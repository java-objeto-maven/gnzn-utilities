package org.guanzon.gnzn.utilities.lp;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.json.simple.JSONObject;

/**
 *
 * @author Michael Cuison
 */
public class AnolidChargeInvoiceToAdvances {
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
                    System.exit(1);
                }
            } else {
                System.err.println("Unable to log user.");
                System.exit(1);
            }
            
            JSONObject dates = getDateRange(SQLUtil.dateFormat(CommonUtils.dateAdd(instance.getServerDate(), -1), SQLUtil.FORMAT_SHORT_DATE));
            String dateFrom = (String) dates.get("dateFrom");
            String dateThru = (String) dates.get("dateThru");            
            
            //check open and unbilled charge invoice
            String lsSQL = "SELECT" +
                                "  a.sTransNox" +
                                ", a.sClientID" +
                                ", a.sChargeNo" +
                                ", a.nAmountxx" +
                                ", a.nVATSales" +
                                ", a.nVATAmtxx" +
                                ", a.nDiscount" +
                                ", a.nVatDiscx" +
                                ", a.nPWDDiscx" +
                                ", a.sClientNm" +
                                ", d.sCompnyNm" +
                                ", c.cSalTypex" +
                                ", a.nAmountxx - (a.nDiscount + a.nVatDiscx + a.nPWDDiscx) nTranTotl" +
                                ", b.dTransact" +
                            " FROM CASys_DBF_LP.Charge_Invoice a" +
                                    " LEFT JOIN GGC_ISysDBF.Employee_Master001 c ON a.sClientID = c.sEmployID" +
                                    " LEFT JOIN GGC_ISysDBF.Client_Master d ON c.sEmployID = d.sClientID" +
                                ", CASys_DBF_LP.SO_Master b" +
                            " WHERE a.sSourceNo = b.sTransNox" +
                                " AND a.cBilledxx = '0'" +
                                " AND b.cTranStat <> '3'" +
                                " AND b.dTransact BETWEEN " + SQLUtil.toSQL(dateFrom) + " AND " + SQLUtil.toSQL(dateThru) +
                            " ORDER BY sChargeNo";

            ResultSet loRS = instance.executeQuery(lsSQL);

            if (MiscUtil.RecordCount(loRS) <= 0){
                System.out.println("No record found.");
                System.exit(0);
            }

            instance.beginTrans();

            try {
                while (loRS.next()){
                    lsSQL = "INSERT INTO Employee_Advances SET" +
                            "  sTransNox = " + SQLUtil.toSQL(MiscUtil.getNextCode("Employee_Advances", "sTransNox", true, instance.getConnection(), instance.getBranchCode())) +
                            ", dTransact = " + SQLUtil.toSQL(loRS.getString("dTransact")) +
                            ", sEmployID = " + SQLUtil.toSQL(loRS.getString("sClientID")) +
                            ", nAmountxx = " + loRS.getDouble("nTranTotl") +
                            ", sRemarksx = " + SQLUtil.toSQL("LP Cafeteria Charge Invoice: " + loRS.getString("dTransact")) +
                            ", sSourceCD = 'LPCI'" +
                            ", sSourceNo = " + SQLUtil.toSQL(loRS.getString("sTransNox")) +
                            ", sEntryByx = " + SQLUtil.toSQL(instance.getUserID()) +
                            ", dEntryDte = " + SQLUtil.toSQL(instance.getServerDate()) +
                            ", sApproved = " + SQLUtil.toSQL(instance.getUserID()) +
                            ", dApproved = " + SQLUtil.toSQL(dateThru) +
                            ", cTranStat = '2'" + 
                            ", sModified = " + SQLUtil.toSQL(instance.getUserID()) +
                            ", dModified = " + SQLUtil.toSQL(instance.getServerDate());

                    if (instance.executeQuery(lsSQL, "Employee_Advances", instance.getBranchCode(), "") <= 0){
                        instance.rollbackTrans();
                        System.err.println(instance.getErrMsg());
                        System.exit(1);
                    }

                    lsSQL = "UPDATE CASys_DBF_LP.Charge_Invoice SET" +
                                "  cBilledxx = '1'" +
                                ", dBilledxx = " + SQLUtil.toSQL(instance.getServerDate()) +
                            " WHERE sTransNox = " + SQLUtil.toSQL(loRS.getString("sTransNox"));

                    if (instance.executeQuery(lsSQL, "CASys_DBF_LP.Charge_Invoice", instance.getBranchCode(), "") <= 0){
                        instance.rollbackTrans();
                        System.err.println(instance.getErrMsg());
                        System.exit(1);
                    }
                }
            } catch (SQLException e) {
                instance.rollbackTrans();
                System.err.println(e.getMessage());
                System.exit(1);
            }

            instance.commitTrans();

            System.out.println("Charge invoices successfully captured.");
            System.exit(0);
        } catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }    
    }
    
    public static JSONObject getDateRange(String currentDate) {
        // Parse the input string to LocalDate
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE);
        LocalDate localDate = LocalDate.parse(currentDate, formatter);

        int day = localDate.getDayOfMonth();
        YearMonth yearMonth = YearMonth.from(localDate);

        LocalDate start;
        LocalDate end;

        if (day <= 15) {
            start = localDate.withDayOfMonth(1);
            end = localDate.withDayOfMonth(15);
        } else {
            start = localDate.withDayOfMonth(16);
            end = localDate.withDayOfMonth(yearMonth.lengthOfMonth());
        }

        JSONObject result = new JSONObject();
        result.put("dateFrom", start.toString());
        result.put("dateThru", end.toString());

        return result;
    }

}
