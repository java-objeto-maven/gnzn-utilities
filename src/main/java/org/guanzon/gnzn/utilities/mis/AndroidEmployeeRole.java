package org.guanzon.gnzn.utilities.mis;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.SQLUtil;

public class AndroidEmployeeRole {
    public static void main (String [] args){
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
            
            ArrayList<String> laDefaultRoles = new ArrayList<>();
            laDefaultRoles.add("PET Manager");
            laDefaultRoles.add("Business Trip");
            laDefaultRoles.add("Leave Application");
            laDefaultRoles.add("Selfie Log");
            laDefaultRoles.add("Application Approval");
            laDefaultRoles.add("Application History");
            laDefaultRoles.add("Company Rule Book");
            laDefaultRoles.add("Rules and Policies");
            
            String lsSQL = "SELECT" +
                                " a.sUserIDxx" +
                            " FROM App_User_Master a" +
                                ", Employee_Master001 b" +
                                ", Branch_Others c" +
                            " WHERE a.sEmployNo = b.sEmployID" +
                                " AND b.sBranchCd = c.sBranchCD" +
                                " AND c.cPayDivCd IN ('2','5')" +
                                " AND a.sProdctID = 'gRider'" +
                                " AND b.cRecdStat = '1'" +
                            " GROUP BY b.sDeptIDxx";
            
            ResultSet loRS = instance.executeQuery(lsSQL);
            ResultSet loRole;
            
            while (loRS.next()){
                for (int lnCtr = 0; lnCtr <= laDefaultRoles.size() - 1; lnCtr++){
                    lsSQL = "SELECT * FROM xxxAOEmpRole" +
                            " WHERE sUserIDxx = " + SQLUtil.toSQL(loRS.getString("sUserIDxx")) +
                                " AND sObjectNm = " + SQLUtil.toSQL(laDefaultRoles.get(lnCtr));
                    
                    loRole = instance.executeQuery(lsSQL);
                    
                    if (!loRole.next()){
                        lsSQL = "INSERT INTO xxxAOEmpRole SET" +
                                "  sProdctID = 'gRider'" + 
                                ", sUserIDxx = " + SQLUtil.toSQL(loRS.getString("sUserIDxx")) +
                                ", sObjectNm = " + SQLUtil.toSQL(laDefaultRoles.get(lnCtr)) +
                                ", cRecdStat = '1'";
                        
                        System.out.println(lsSQL);
                        instance.executeUpdate(lsSQL);
                    }
                }
            }
            
            System.out.println("Done. Thank you...");
        } catch (IOException | SQLException e){
            e.printStackTrace();
            System.exit(1);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
