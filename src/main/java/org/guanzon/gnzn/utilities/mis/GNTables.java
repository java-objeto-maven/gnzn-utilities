package org.guanzon.gnzn.utilities.mis;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class GNTables {
    public static void main(String[] args) {
        LogWrapper logwrapr = new LogWrapper("GNTables", "gnzn-utilities.log");
        
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
                    logwrapr.warning(instance.getErrMsg());
                    System.exit(1);
                }
            } else {
                System.err.println("Unable to log user.");
                System.exit(1);
            }
            
            //backupTables(instance);
            
            instance.beginTrans();
            instance.executeQuery("SET sql_notes = 0;", "xxxTableAll", instance.getBranchCode(), instance.getBranchCode());
            instance.executeQuery("DROP TABLE IF EXISTS gn_customer;", "xxxTableAll", instance.getBranchCode(), instance.getBranchCode());
            instance.executeQuery("DROP TABLE IF EXISTS gn_inv_type_transfer_master;", "xxxTableAll", instance.getBranchCode(), instance.getBranchCode());
            instance.executeQuery("DROP TABLE IF EXISTS gn_inv_type_transfer_detail;", "xxxTableAll", instance.getBranchCode(), instance.getBranchCode());
            instance.executeQuery("DROP TABLE IF EXISTS gn_so_accessories;", "xxxTableAll", instance.getBranchCode(), instance.getBranchCode());
            instance.executeQuery("DROP TABLE IF EXISTS gn_so_checks;", "xxxTableAll", instance.getBranchCode(), instance.getBranchCode());
            instance.executeQuery("DROP TABLE IF EXISTS gn_so_giveaways;", "xxxTableAll", instance.getBranchCode(), instance.getBranchCode());
            instance.executeQuery("DROP TABLE IF EXISTS gn_so_package;", "xxxTableAll", instance.getBranchCode(), instance.getBranchCode());
            instance.executeQuery("DROP TABLE IF EXISTS gn_so_return_master;", "xxxTableAll", instance.getBranchCode(), instance.getBranchCode());
            instance.executeQuery("DROP TABLE IF EXISTS gn_so_return_detail;", "xxxTableAll", instance.getBranchCode(), instance.getBranchCode());
            instance.executeQuery("DROP TABLE IF EXISTS gn_wso_detail;", "xxxTableAll", instance.getBranchCode(), instance.getBranchCode());
            instance.executeQuery("DROP TABLE IF EXISTS gn_wso_giveaways;", "xxxTableAll", instance.getBranchCode(), instance.getBranchCode());
            instance.executeQuery("DROP TABLE IF EXISTS gn_wso_master;", "xxxTableAll", instance.getBranchCode(), instance.getBranchCode());
            instance.executeQuery("DROP TABLE IF EXISTS gn_wso_return_detail;", "xxxTableAll", instance.getBranchCode(), instance.getBranchCode());
            instance.executeQuery("DROP TABLE IF EXISTS gn_wso_return_master;", "xxxTableAll", instance.getBranchCode(), instance.getBranchCode());
            instance.executeQuery("SET sql_notes = 1;", "xxxTableAll", instance.getBranchCode(), instance.getBranchCode());
            instance.commitTrans();
            
        } catch (IOException e) {
            logwrapr.warning(e.getMessage());
            System.exit(1);
        }
        
        System.exit(0);
        //backup data
        //backup create table
        //drop table
        //create table
        //insert data
    }
    
    private static void backupTables(GRider instance) throws SQLException{
        System.setProperty("newTable", "GN_CO_Detail");
        System.setProperty("oldTable", "gn_co_detail");

        String dropTable = "DROP TABLE " + System.getProperty("oldTable");
        String tableStruct = "SHOW CREATE TABLE " + System.getProperty("oldTable");

        ResultSet resultSet = instance.executeQuery(tableStruct);

        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;

        if (resultSet.next()){
            String sql = resultSet.getString(2).replace(System.getProperty("oldTable"), System.getProperty("newTable"));

            jsonObject = new JSONObject();
            jsonObject.put("TABLE_NAME", System.getProperty("newTable"));
            jsonObject.put("CREATE_TABLE", sql);

            sql = "SELECT * FROM " + System.getProperty("oldTable");

            resultSet = instance.executeQuery(sql);
            JSONArray payload = MiscUtil.RS2JSON(resultSet);
            jsonObject.put("PAYLOAD", payload);

            System.out.println(jsonObject.toJSONString());
        }
    }
    
    private static List<String> getTables(){
        List<String> laTables = new ArrayList<>();
        
        laTables.add("GN_Accessories");
        laTables.add("GN_Accessories_JobOrder_Detail");
        laTables.add("GN_Accessories_JobOrder_Master");
        laTables.add("GN_Adjustment");
        laTables.add("GN_Brand");
        laTables.add("GN_Category");
        laTables.add("GN_Classification_Detail");
        laTables.add("GN_Classification_Master");
        laTables.add("GN_Condition");
        laTables.add("GN_Dealer");
        laTables.add("GN_Defect");
        laTables.add("GN_Fabrication_Detail");
        laTables.add("GN_Fabrication_Master");
        laTables.add("GN_Inv_Type");
        laTables.add("GN_Inventory");
        laTables.add("GN_Inventory_Adjustment");
        laTables.add("GN_Inventory_Count_Detail");
        laTables.add("GN_Inventory_Count_Master");
        laTables.add("GN_Inventory_Ledger");
        laTables.add("GN_Inventory_Master");
        laTables.add("GN_Inventory_Serial");
        laTables.add("GN_Inventory_Serial_Ledger");
        laTables.add("GN_JobOrder_Detail");
        laTables.add("GN_JobOrder_Master");
        laTables.add("GN_JobOrder_Movement");
        laTables.add("GN_JobOrder_Transfer_Detail");
        laTables.add("GN_JobOrder_Transfer_Master");
        laTables.add("GN_Labor");
        laTables.add("GN_Model");
        laTables.add("GN_Model_Price");
        laTables.add("GN_PO_Master");
        laTables.add("GN_PO_Receiving_Detail");
        laTables.add("GN_PO_Receiving_Master");
        laTables.add("GN_PO_Receiving_Package");
        laTables.add("GN_PO_Receiving_Serial");
        laTables.add("GN_Package_Model");
        laTables.add("GN_SO_Detail");
        laTables.add("GN_SO_Master");
        laTables.add("GN_Serial_Cost");
        laTables.add("GN_Serial_History");
        laTables.add("GN_Service_Center");
        laTables.add("GN_Supplier");
        laTables.add("GN_StockInquiry_Detail");
        laTables.add("GN_StockInquiry_Master");
        laTables.add("GN_Stock_Request_Detail");
        laTables.add("GN_Stock_Request_Master");
        laTables.add("GN_Symptom");
        laTables.add("GN_Transfer_Detail");
        laTables.add("GN_Transfer_Master");

        return laTables;
    }
}
