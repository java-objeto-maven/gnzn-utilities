package org.guanzon.gnzn.utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;

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
            
            String lsSQL;
            
            //get agents that is not yet tagged as customer
            lsSQL = "SELECT" +
                        "  a.sReferdBy" +
                        ", b.sClientID" +
                    " FROM Ganado_Online a" +
                        ", App_User_Master b" +
                    " WHERE a.sReferdBy = b.sUserIDxx" +
                        " AND a.cSourcexx = '1'" +
                        " AND a.cTranStat <> '3'" +
                        " AND IFNULL(b.cGnznCltx, '0') = '0'" +
                    " GROUP BY a.sReferdBy";
            
            ResultSet loRS = instance.executeQuery(lsSQL);
            
            instance.beginTrans();
            while (loRS.next()){
                lsSQL = "SELECT *" +
                        " FROM App_User_Profile" +
                        " WHERE sUserIDxx = " + SQLUtil.toSQL(loRS.getString("sReferdBy")) +
                            " AND IFNULL(sClientID, '') <> ''" +
                        " GROUP BY sClientID";  
                       
                ResultSet loResult = instance.executeQuery(lsSQL);
         
                if (MiscUtil.RecordCount(loResult) > 0){
                    String lsCondition = "";
                    while (loResult.next()){
                        lsCondition += ", " + SQLUtil.toSQL(loResult.getString("sClientID"));
                    }
                    lsCondition = "sClientID IN (" + lsCondition.substring(2) + ")";

                    //find transactions made
                    lsSQL = "SELECT" +
                                "  sTransNox" +
                                ", sClientID" +
                                ", nTranTotl" +
                            " FROM MC_SO_Master" +
                            " WHERE " + lsCondition +
                            " UNION" +
                            " SELECT" +
                                "  sTransNox" +
                                ", sClientID" +
                                ", nTranTotl" +
                            " FROM JobOrderBranch_Master" + 
                            " WHERE " + lsCondition +
                            " UNION" +
                            " SELECT" +
                                "  sTransNox" +
                                ", sClientID" +
                                ", nTranTotl" +
                            " FROM JobOrder_Master" + 
                            " WHERE " + lsCondition +
                            " UNION" +
                            " SELECT" +
                                "  sTransNox" +
                                ", sClientID" +
                                ", nTranTotl" +
                            " FROM SP_SO_Master" + 
                            " WHERE " + lsCondition;

                    lsSQL = "SELECT * FROM (" + lsSQL + ") a ORDER BY a.nTranTotl DESC LIMIT 1";

                    loResult = instance.executeQuery(lsSQL);

                    if (loResult.next()){
                        lsSQL = "UPDATE App_User_Master SET" +
                                    "  sClientID = " + SQLUtil.toSQL(loResult.getString("sClientID")) +
                                    ", cGnznCltx = '1'" + 
                                " WHERE sUserIDxx = " + SQLUtil.toSQL(loRS.getString("sReferdBy"));

                        if (instance.executeQuery(lsSQL, "App_User_Master", instance.getBranchCode(), "") <= 0){
                            logwrapr.severe("Unable to update App User Master." + "\n" +
                                            instance.getErrMsg() + "\n" +
                                            instance.getMessage());
                            instance.rollbackTrans();
                            System.exit(1);
                        }
                    }
                }
                
                
            }
            instance.commitTrans();
        } catch (IOException | SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
        
        System.exit(0);
    }
}
