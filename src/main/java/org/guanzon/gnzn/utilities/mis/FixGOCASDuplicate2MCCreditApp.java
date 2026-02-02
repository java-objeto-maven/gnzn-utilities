package org.guanzon.gnzn.utilities.mis;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;

/**
 *
 * @author Michael Cuison
 */
public class FixGOCASDuplicate2MCCreditApp {
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
            
            String lsSQL = "SELECT sReferNox, COUNT(sTransNox) xCountxxx" +
                            " FROM MC_Credit_Application" +
                            " WHERE IFNULL(sReferNox, '') <> ''" +
                                    " AND IFNULL(sGOCASNox, '') <> ''" +
                            " GROUP BY sReferNox";
            
            //group applications by reference number
            ResultSet loGroup = instance.executeQuery(lsSQL);
            
            while (loGroup.next()){
                //have duplicate entries
                if (loGroup.getInt("xCountxxx") > 1){
                    lsSQL = "SELECT sTransNox, sReferNox, sGOCASNox" +
                            " FROM MC_Credit_Application" +
                            " WHERE sReferNox = " + SQLUtil.toSQL(loGroup.getString("sReferNox")) +
                                    " AND cTranStat = '4'";

                    //get if there is an selected application
                    ResultSet loRS = instance.executeQuery(lsSQL);

                    while (loRS.next()){
                        //yes it has!!!
                        System.out.println("REFERENCE: " + loRS.getString("sReferNox"));
                        lsSQL = "SELECT sTransNox" +
                                " FROM MC_Credit_Application" +
                                " WHERE sTransNox <> " + SQLUtil.toSQL(loRS.getString("sTransNox")) +
                                    " AND sReferNox = " + SQLUtil.toSQL(loRS.getString("sReferNox")) +
                                    " AND cTranStat <> '4'";

                        //get duplicate applications that are not approved
                        ResultSet loDuplicate = instance.executeQuery(lsSQL);

                        if (MiscUtil.RecordCount(loDuplicate) > 0){
                            instance.beginTrans();
                            while(loDuplicate.next()){
                                lsSQL = "DELETE FROM MC_Credit_Application" +
                                        " WHERE sTransNox = " + SQLUtil.toSQL(loDuplicate.getString("sTransNox")) +
                                            " AND cTranStat <> '4'";

                                //delete record
                                System.out.println("DELETE RECORD: " + loDuplicate.getString("sTransNox"));
                                if (instance.executeQuery(lsSQL, "xxxTableAll", instance.getBranchCode(), "") <= 0){
                                    instance.rollbackTrans();
                                    System.err.println("Unable to delete record." + lsSQL);
                                    System.exit(1);
                                }
                            }
                            instance.commitTrans();
                        }
                    }
                }
            }
            
            //now delete the duplicate cancelled
            lsSQL = "SELECT * FROM (" +
                        " SELECT sReferNox, cTranStat, COUNT(sTransNox) xCountxxx" +
                        " FROM MC_Credit_Application" +
                        " WHERE IFNULL(sReferNox, '') <> ''" +
                        " GROUP BY sReferNox, cTranStat) xx" +
                    " WHERE xx.xCountxxx > 1" +
                    " ORDER BY xx.cTranStat, xx.sReferNox";
            
            //group applications by reference number
            loGroup = instance.executeQuery(lsSQL);
            
            while (loGroup.next()){
                lsSQL = "SELECT" +
                            "   sTransNox" +
                            " , cTranStat" +
                        " FROM MC_Credit_Application" +
                        " WHERE sReferNox = " + SQLUtil.toSQL(loGroup.getString("sReferNox")) +
                            " AND cTranStat = " + SQLUtil.toSQL(loGroup.getString("cTranStat"));
                
                ResultSet loRS = instance.executeQuery(lsSQL);
                
                while (loRS.next()){
                    if (!loRS.getString("cTranStat").equals("4")){ //not selected duplicate
                        if (!loRS.isFirst()){
                            instance.beginTrans();
                            //selected credit app has no mc sales
                            lsSQL = "DELETE FROM MC_Credit_Application" +       
                                    " WHERE sTransNox = " + SQLUtil.toSQL(loRS.getString("sTransNox"));

                            //delete record
                            System.out.println("DELETE RECORD: " + loRS.getString("sTransNox"));
                            if (instance.executeQuery(lsSQL, "xxxTableAll", instance.getBranchCode(), "") <= 0){
                                instance.rollbackTrans();
                                System.err.println("Unable to delete record." + lsSQL);
                                System.exit(1);
                            }
                            instance.commitTrans();
                        }
                    } else { //selected but duplicate
                        //check if the credit app is used in MC Sales, if not delete
                        lsSQL = "SELECT * FROM MC_SO_Master WHERE sApplicNo = " + SQLUtil.toSQL(loRS.getString("sTransNox"));
                        
                        ResultSet loMCSales = instance.executeQuery(lsSQL);
                        
                        if (!loMCSales.next()){
                            instance.beginTrans();
                            //selected credit app has no mc sales
                            lsSQL = "DELETE FROM MC_Credit_Application" +       
                                    " WHERE sTransNox = " + SQLUtil.toSQL(loRS.getString("sTransNox"));

                            //delete record
                            System.out.println("DELETE RECORD: " + loRS.getString("sTransNox"));
                            if (instance.executeQuery(lsSQL, "xxxTableAll", instance.getBranchCode(), "") <= 0){
                                instance.rollbackTrans();
                                System.err.println("Unable to delete record." + lsSQL);
                                System.exit(1);
                            }
                            instance.commitTrans();
                        }
                    }
                }
            }
            
            System.out.println("Thank you...");
            System.exit(0);
        } catch (IOException | SQLException e){
            e.printStackTrace();
            System.exit(1);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }    
    }
}
