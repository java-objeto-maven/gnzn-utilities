package org.guanzon.gnzn.utilities.lib.cp;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.json.simple.JSONObject;

public class Bolttech {
    GRider _instance;
    
    public Bolttech(GRider foValue){
        _instance = foValue;
    }
    
    public JSONObject NewTransaction(){
        String lsSQL;
        JSONObject loJSON;
        
        //get transactions that is not yet extracted
        lsSQL = getSQ_Master();
        
        ResultSet loRS = _instance.executeQuery(lsSQL);
        
        try {
            ResultSet loDetail;
            JSONObject loJSONDet;
            
            while (loRS.next()){
                lsSQL = MiscUtil.addCondition(getSQ_Detail(), "a.sTransNox = " + SQLUtil.toSQL(loRS.getString("sTransNox")));
                
                loDetail = _instance.executeQuery(lsSQL);
                
                if (loDetail.next()){
                    loJSONDet = new JSONObject();
                    
                    for (int lnCtr = 0; lnCtr <= loDetail.getMetaData().getColumnCount() -1; lnCtr++){
                        loJSONDet.put(loDetail.getMetaData().getColumnLabel(lnCtr), loDetail.getObject(lnCtr));
                    }
                    
                    lsSQL = "INSERT INTO CP_SO_Insurance SET" +
                            "  sTransNox = " + SQLUtil.toSQL(MiscUtil.getNextCode("CP_SO_Insurance", "sTransNox", true, _instance.getConnection(), _instance.getBranchCode())) +
                            ", dTransact = " + SQLUtil.toSQL(_instance.getServerDate()) +
                            ", sSourceNo = " + SQLUtil.toSQL(loRS.getString("sTransNox")) +
                            ", sStockIDx = " + SQLUtil.toSQL(loRS.getString("sStockIDx")) +
                            ", sPayloadx = " + SQLUtil.toSQL(loJSONDet);
                }
            }
        } catch (SQLException e) {
            loJSON = new JSONObject();
            loJSON.put("result", "error");
            loJSON.put("message", e.getMessage());
        }
        
        loJSON = new JSONObject();
        loJSON.put("result", "success");
        loJSON.put("message", "Transactions exported successfully.");
        
        return loJSON;
    }
    
    public JSONObject CreateCSV(){
        JSONObject loJSON = new JSONObject();
        
        return loJSON;
    }
    
    public JSONObject UploadFile(){
        JSONObject loJSON = new JSONObject();
        
        return loJSON;
    }
    
    private String getSQ_Master(){
        return "SELECT" +
                    "  a.sTransNox" +
                    ", b.sStockIDx" +
                    ", c.sCategID1" +
                    ", e.sTransNox xTransNox" +
                " FROM CP_SO_Master a" +
                    " LEFT JOIN CP_SO_Insurance e ON a.sTransNox = e.sSourceNo" +
                    ", CP_SO_Detail b" +
                        " LEFT JOIN CP_Inventory c ON b.sStockIDx = c.sStockIDx" +
                        " LEFT JOIN Category d ON c.sCategID1 = d.sCategrID" +
                " WHERE a.sTransNox = b.sTransNox" +
                    " AND a.cTranStat <> '3'" +
                    " AND a.dTransact > '2024-06-27'" +
                " HAVING c.sCategID1 = 'C001052'" +
                    " AND xTransNox IS NULL";
    }
    
    private String getSQ_Detail(){
        return "SELECT" + 
                    "  a.sSalesInv CLIENT_TRANS_NO" + 
                    ", DATE_FORMAT(a.dTransact,'%d/%m/%Y') CONTRACT_SOLD_DATE" + 
                    ", f.sCategrNm PRODUCT_NAME" + 
                    ", TRIM(CONCAT(g.sFrstName, ' ', g.sLastName )) CUST_NAME" + 
                    ", '' CUST_ID" + 
                    ", g.sMobileNo CUST_MOBILE_NO" + 
                    ", g.sEmailAdd CUST_EMAIL" + 
                    ", TRIM(CONCAT(g.sAddressx, ' ', j.sTownName, ', ', h.sProvName)) CUST_ADDRESS" + 
                    ", '' CUST_CITY" + 
                    ", d.sBranchCd STORE_CODE" + 
                    ", d.sBranchNm STORE_NAME" + 
                    ", '' STORE_ADDRESS" + 
                    ", i.sTownName STORE_CITY" + 
                    ", a.sSalesman SALES_REP_ID" + 
                    ", TRIM(CONCAT(k.sFrstName, ' ', k.sLastName)) SALES_REP_NAME" + 
                    ", c.nPurchase DEVICE_RRP" + 
                    ", e.sCategrNm DEVICE_TYPE" + 
                    ", l.sBrandNme DEVICE_MAKE" + 
                    ", m.sModelNme DEVICE_MODEL" + 
                    ", n.sColorNme COLOR" + 
                    ", o.sSerialNo IMEI" + 
                    ", e.sCategrNm NAME_GOODS_TYPE" + 
                    ", DATE_FORMAT(a.dTransact,'%d/%m/%Y') DTIME_START" + 
                    ", '' DTIME_END" + 
                    ", c.nPurchase DEVICE_VALUE_SUM_COVERED" + 
                    ", '' CREATION_DATE" + 
                    ", o.sSerialNo SERIALNO" + 
                    ", 'PHGUANZRETNA01' PARTNER_ID" + 
                    ", 'MBG' VALUE_ADDED_SERVICES" + 
                    ", '' DWH_UNIQUE_KEY" + 
                " FROM CP_SO_Master a" + 
                        " LEFT JOIN Client_Master g ON a.sClientID = g.sClientID" + 
                        " LEFT JOIN TownCity j ON g.sTownIDxx = j.sTownIDxx" + 
                        " LEFT JOIN Province h ON j.sProvIDxx = h.sProvIDxx" + 
                        " LEFT JOIN Salesman k ON a.sSalesman = k.sEmployID" + 
                    ", CP_SO_Detail b" + 
                        " LEFT JOIN CP_Inventory_Serial o ON b.sSerialID = o.sSerialID" + 
                    ", CP_Inventory c" + 
                        " LEFT JOIN Category e ON c.sCategID1 = e.sCategrID" + 
                        " LEFT JOIN Category f ON c.sCategID2 =  f.sCategrID" + 
                        " LEFT JOIN CP_Brand l ON c.sBrandIDx = l.sBrandIDx" + 
                        " LEFT JOIN CP_Model m ON c.sModelIDx = m.sModelIDx" + 
                        " LEFT JOIN Color n ON c.sColorIDx = n.sColorIDx" + 
                    ", Branch d" + 
                        " LEFT JOIN TownCity i ON d.sTownIDxx = i.sTownIDxx" + 
                " WHERE a.sTransNox = b.sTransNox" + 
                    " AND b.sStockIDx = c.sStockIDx" + 
                    " AND LEFT(a.sTransNox,4) = d.sBranchCd" + 
                    " AND a.cTranStat <> 3" +
                " HAVING PRODUCT_NAME = 'Units'";
    }
}
