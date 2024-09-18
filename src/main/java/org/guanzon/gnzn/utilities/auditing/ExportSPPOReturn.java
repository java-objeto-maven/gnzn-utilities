package org.guanzon.gnzn.utilities.auditing;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;

public class ExportSPPOReturn {
    public static void main(String[] args) {
        LogWrapper logwrapr = new LogWrapper("ExportSPPOReturn", "gnzn-utilities.log");
        logwrapr.info("Start of Process!");
        
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Maven_Systems";
        }
        else{
            path = "/srv/GGC_Maven_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        System.setProperty("sys.default.path.temp", path + "/temp");
        
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
                        
            String lsSQL = "SELECT *" +
                            " FROM SP_PO_Return_Master" +
                            " WHERE sTransNox LIKE 'M095%'" +
                                " AND dTransact >= '2021-01-01'" +
                            " ORDER BY sTransNox";
            
            ResultSet loRS = instance.executeQuery(lsSQL);
            
            if (MiscUtil.RecordCount(loRS) <= 0){
                System.out.println("No Transactions Found...");
                System.exit(0);
            }
            
            try (FileWriter writer = new FileWriter(System.getProperty("sys.default.path.temp") + "/SPPOReturn - " + SQLUtil.dateFormat(instance.getServerDate(), SQLUtil.FORMAT_TIMESTAMPX)  + ".csv", true)){
                writer.append("Transaction No.");
                writer.append(',');
                writer.append("Date");
                writer.append(',');
                writer.append("Remarks");
                writer.append(',');
                writer.append("Tran Total");
                writer.append(',');
                writer.append("Status");
                writer.append(',');
                writer.append("Date Created");
                writer.append(',');
                writer.append("Created By");
                writer.append(',');
                writer.append("Created ID");
                writer.append(',');
                writer.append("Date Printed");
                writer.append(',');
                writer.append("Printed By");
                writer.append(',');
                writer.append("Printed ID");
                writer.append(',');
                writer.append("Date Cancelled");
                writer.append(',');
                writer.append("Cancelled By");
                writer.append(',');
                writer.append("Cancelled ID");
                writer.append('\n');
                
                while (loRS.next()){
                    writer.append(loRS.getString("sTransNox"));
                    writer.append(',');
                    writer.append(loRS.getString("dTransact"));
                    writer.append(',');
                    writer.append(loRS.getString("sRemarksx"));
                    writer.append(',');
                    writer.append(String.valueOf(loRS.getDouble("nTranTotl")));
                    writer.append(',');
                    writer.append(loRS.getString("cTranStat"));
                    writer.append(',');

                    lsSQL = "SELECT * FROM xxxReplicationLog" +
                            " WHERE sTransNox LIKE " + SQLUtil.toSQL(loRS.getString("sTransNox").substring(0, 6) + "%") +
                                " AND sStatemnt LIKE 'INSERT INTO%" + loRS.getString("sTransNox") + "%'" +
                                " AND sTableNme = 'SP_PO_Return_Master'";

                    ResultSet loRx = instance.executeQuery(lsSQL);

                    //transaction creation
                    if (loRx.next()){
                        writer.append(loRx.getString("dEntryDte"));
                        writer.append(',');
                        writer.append(getUserName(instance, loRx.getString("sModified")));
                        writer.append(',');
                        writer.append(loRx.getString("sModified"));
                        writer.append(',');
                    } else {
                        writer.append("");
                        writer.append(',');
                        writer.append("");
                        writer.append(',');
                        writer.append("");
                        writer.append(',');
                    }

                    //transaction printing
                    lsSQL = "SELECT * FROM xxxReplicationLog" +
                            " WHERE sTransNox LIKE " + SQLUtil.toSQL(loRS.getString("sTransNox").substring(0, 6) + "%") +
                                " AND sStatemnt LIKE 'UPDATE%SET cTranStat = 1%" + loRS.getString("sTransNox") + "%'" +
                                " AND sTableNme = 'SP_PO_Return_Master'";

                    loRx = instance.executeQuery(lsSQL);

                    if (loRx.next()){
                        writer.append(loRx.getString("dEntryDte"));
                        writer.append(',');
                        writer.append(getUserName(instance, loRx.getString("sModified")));
                        writer.append(',');
                        writer.append(loRx.getString("sModified"));
                        writer.append(',');
                    } else {
                        writer.append("");
                        writer.append(',');
                        writer.append("");
                        writer.append(',');
                        writer.append("");
                        writer.append(',');
                    }

                    //transaction cancellation
                    lsSQL = "SELECT * FROM xxxReplicationLog" +
                            " WHERE sTransNox LIKE " + SQLUtil.toSQL(loRS.getString("sTransNox").substring(0, 6) + "%") +
                                " AND sStatemnt LIKE 'UPDATE%SET cTranStat = 3%" + loRS.getString("sTransNox") + "%'" +
                                " AND sTableNme = 'SP_PO_Return_Master'";

                    loRx = instance.executeQuery(lsSQL);

                    if (loRx.next()){
                        writer.append(loRx.getString("dEntryDte"));
                        writer.append(',');
                        writer.append(getUserName(instance, loRx.getString("sModified")));
                        writer.append(',');
                        writer.append(loRx.getString("sModified"));
                        writer.append('\n');
                    } else {
                        writer.append("");
                        writer.append(',');
                        writer.append("");
                        writer.append(',');
                        writer.append("");
                        writer.append('\n');
                    }
                }
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
        
        System.exit(0);
    }
    
    private static String getUserName(GRider foGRider, String fsUserIDxx) throws SQLException{
        String lsSQL = "SELECT" +
                            "  CONCAT(c.sFrstName, ' ', c.sLastName) sCompnyNm" +
                        " FROM xxxSysUser a" +
                            " LEFT JOIN Employee_Master001 b ON a.sEmployNo = b.sEmployID" +
                            " LEFT JOIN Client_Master c ON b.sEmployID = c.sClientID" +
                        " WHERE a.sUserIDxx = " + SQLUtil.toSQL(fsUserIDxx);
                        
        ResultSet loRS = foGRider.executeQuery(lsSQL);
        
        if (loRS.next())
            return loRS.getString("sCompnyNm");
        else
            return "";
    }
}
