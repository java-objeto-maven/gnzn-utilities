package org.guanzon.gnzn.utilities.lib.cp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.opencsv.CSVWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Properties;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.base.StringHelper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Bolttech {
    private final String UPLOAD = System.getProperty("sys.default.path.config") + "/temp/Bolttech/upload/";
    private final String SUCCESS = System.getProperty("sys.default.path.config") + "/temp/Bolttech/success/";
    private final String FAILED = System.getProperty("sys.default.path.config") + "/temp/Bolttech/failed/";
    private final String REPORT = System.getProperty("sys.default.path.config") + "/temp/Bolttech/report/";
    
    GRider _instance;
    
    public Bolttech(GRider foValue){
        _instance = foValue;
    }
    
    public JSONObject NewTransaction(){
        JSONObject loJSON = new JSONObject();
        
        String lsSQL;
        
        //get transactions that is not yet extracted
        lsSQL = getSQ_Master() + " AND xTransNox IS NULL" +
                " ORDER BY a.dTransact";
        
        ResultSet loRS = _instance.executeQuery(lsSQL);
        
        try {
            ResultSet loDetail;
            JSONObject loJSONDet;
            
            while (loRS.next()){
                switch(loRS.getString("sTransNox")){
                    //multiple SI on single transaction
                    case "C03925001240": //SELECT * FROM `CP_SO_Master` WHERE sTransNox LIKE 'C035%' AND sSalesInv IN ('75376', '75375');
                        lsSQL = MiscUtil.addCondition(getSQ_Detail(), "a.sTransNox = 'C03925001239'");
                        break;
                    default:
                        lsSQL = MiscUtil.addCondition(getSQ_Detail(), "a.sTransNox = " + SQLUtil.toSQL(loRS.getString("sTransNox")));
                }
                
                loDetail = _instance.executeQuery(lsSQL);
                
                if (loDetail.next()){
                    loJSONDet = new JSONObject();
                    
                    for (int lnCtr = 1; lnCtr <= loDetail.getMetaData().getColumnCount(); lnCtr++){
                        switch (loDetail.getMetaData().getColumnLabel(lnCtr)){
                        case "IMEI":
//                            if (loDetail.getString("BRAND_ID").equals("C001058")){ //iphone
//                                loJSONDet.put(loDetail.getMetaData().getColumnLabel(lnCtr), "");
//                            } else {
//                                loJSONDet.put(loDetail.getMetaData().getColumnLabel(lnCtr), loDetail.getObject(lnCtr));
//                            }
                            
                            if (loDetail.getObject(lnCtr).toString().length() <= 15 &&
                                StringHelper.isNumeric(loDetail.getObject(lnCtr).toString())){
                                
                                loJSONDet.put(loDetail.getMetaData().getColumnLabel(lnCtr), loDetail.getObject(lnCtr));
                            } else {
                                loJSONDet.put(loDetail.getMetaData().getColumnLabel(lnCtr), "");
                            }
                            break;
                        case "SERIALNO":
//                            if (loDetail.getString("BRAND_ID").equals("C001058")){ //iphone
//                                loJSONDet.put(loDetail.getMetaData().getColumnLabel(lnCtr), loDetail.getObject(lnCtr));
//                            } else {
//                                loJSONDet.put(loDetail.getMetaData().getColumnLabel(lnCtr), "");
//                            }
                            
                            if (loDetail.getObject(lnCtr).toString().length() <= 15 &&
                                StringHelper.isNumeric(loDetail.getObject(lnCtr).toString())){
                                
                                loJSONDet.put(loDetail.getMetaData().getColumnLabel(lnCtr), "");
                            } else {
                                loJSONDet.put(loDetail.getMetaData().getColumnLabel(lnCtr), loDetail.getObject(lnCtr));
                            }
                            break;
                        case "DTIME_START":
                            lsSQL = loDetail.getString("CONTRACT_SOLD_DATE"); //DD/MM/YYYY
                            //ADLD - start date is same with purchase date
                            
                            if (loRS.getString("sCategID2").equals("C001054")){
                                //EW - start date is 12 months after the purchase date
                                //      we are assumming that the standard warranty period is always 12 months for new units
                                lsSQL = SQLUtil.dateFormat(CommonUtils.dateAdd(SQLUtil.toDate(lsSQL, "dd/MM/yyyy"), Calendar.MONTH, 12), "dd/MM/yyyy");
                            }
                            
                            loJSONDet.put(loDetail.getMetaData().getColumnLabel(lnCtr), lsSQL);                            
                            break;
                        case "DTIME_END":
                            lsSQL = (String) loJSONDet.get("DTIME_START");
                            lsSQL = SQLUtil.dateFormat(CommonUtils.dateAdd(SQLUtil.toDate(lsSQL, "dd/MM/yyyy"), Calendar.MONTH, 12), "dd/MM/yyyy");
                            
                            loJSONDet.put(loDetail.getMetaData().getColumnLabel(lnCtr), lsSQL);   
                            break;
                        case "NAME_GOODS_TYPE":
                        case "DEVICE_TYPE":
                            if (loDetail.getString("CATEG_ID4").equals("C001055") ||
                                loDetail.getString("CATEG_ID4").equals("C0A9008")){
                                loJSONDet.put(loDetail.getMetaData().getColumnLabel(lnCtr), "TABLET");  
                            } else {
                                loJSONDet.put(loDetail.getMetaData().getColumnLabel(lnCtr), "SMARTPHONE");  
                            }
                            break;
                        case "BRAND_ID":
                        case "CATEG_ID":
                        case "CATEG_ID4":
                            break;
                        default:
                            if (loDetail.getMetaData().getColumnLabel(lnCtr).equals("PRODUCT_NAME")){
                                loJSONDet.put(loDetail.getMetaData().getColumnLabel(lnCtr), loRS.getString("sCategrNm"));
                            } else {
                                loJSONDet.put(loDetail.getMetaData().getColumnLabel(lnCtr), loDetail.getObject(lnCtr));
                            }
                        }                    
                    }
                    
                    lsSQL = "INSERT INTO CP_SO_Insurance SET" +
                            "  sTransNox = " + SQLUtil.toSQL(MiscUtil.getNextCode("CP_SO_Insurance", "sTransNox", true, _instance.getConnection(), _instance.getBranchCode())) +
                            ", dTransact = " + SQLUtil.toSQL(_instance.getServerDate()) +
                            ", sSourceNo = " + SQLUtil.toSQL(loRS.getString("sTransNox")) +
                            ", sStockIDx = " + SQLUtil.toSQL(loRS.getString("sStockIDx")) +
                            ", sPayloadx = " + SQLUtil.toSQL(loJSONDet.toJSONString()) +
                            ", sModified = " + SQLUtil.toSQL(_instance.getUserID());
                    
                    if (_instance.executeUpdate(lsSQL) <= 0){
                        loJSON.put("result", "error");
                        loJSON.put("message", _instance.getMessage() + "; " + _instance.getErrMsg());
                        return loJSON;
                    }
                }
            }
        } catch (SQLException e) {
            loJSON.put("result", "error");
            loJSON.put("message", e.getMessage());
            return loJSON;
        }
        
        loJSON.put("result", "success");
        loJSON.put("message", "Transactions exported successfully.");
        
        return loJSON;
    }
    
    public JSONObject CreateCSV(){
        JSONObject loJSON = new JSONObject();
        JSONParser loParser = new JSONParser();
        
        String lsSQL = getSQ_Batch();
        
        ResultSet loRS = _instance.executeQuery(lsSQL);
        
        if (MiscUtil.RecordCount(loRS) <= 0){
            loJSON.put("result", "success");
            loJSON.put("message", "No data to extract.");
            return loJSON;
        }
        
        String lsTransNox = MiscUtil.getNextCode("Bolttech", "sBatchNox", true, _instance.getConnection(), _instance.getBranchCode());
        String lsFilename = "GUAPHTCV_PH_" + SQLUtil.dateFormat(_instance.getServerDate(), SQLUtil.FORMAT_SHORT_DATEX) + ".csv";
        
        try (CSVWriter writer = new CSVWriter(new FileWriter(UPLOAD + lsFilename))) {
            lsSQL = "INSERT INTO Bolttech SET" + 
                        "  sBatchNox = " + SQLUtil.toSQL(lsTransNox) +
                        ", sFileName = " + SQLUtil.toSQL(lsFilename) +
                        ", cTranStat = '0'" +
                        ", dCreatedx = " + SQLUtil.toSQL(SQLUtil.dateFormat(_instance.getServerDate(), SQLUtil.FORMAT_TIMESTAMP));
                
            if (_instance.executeUpdate(lsSQL) <= 0) {
                loJSON.put("result", "error");
                loJSON.put("message", _instance.getMessage() + "; " + _instance.getErrMsg());
                return loJSON;
            }
            
            String [] header = new String[30];
            header[0] = "CLIENT_TRANS_NO";
            header[1] = "CONTRACT_SOLD_DATE";
            header[2] = "PRODUCT_NAME";
            header[3] = "CUST_NAME";
            header[4] = "CUST_ID";
            header[5] = "CUST_MOBILE_NO";
            header[6] = "CUST_EMAIL";
            header[7] = "CUST_ADDRESS";
            header[8] = "CUST_CITY";
            header[9] = "STORE_CODE";
            header[10] = "STORE_NAME";
            header[11] = "STORE_ADDRESS";
            header[12] = "STORE_CITY";
            header[13] = "SALES_REP_ID";
            header[14] = "SALES_REP_NAME";
            header[15] = "DEVICE_RRP";
            header[16] = "DEVICE_TYPE";
            header[17] = "DEVICE_MAKE";
            header[18] = "DEVICE_MODEL";
            header[19] = "COLOR";
            header[20] = "IMEI";
            header[21] = "NAME_GOODS_TYPE";
            header[22] = "DTIME_START";
            header[23] = "DTIME_END";
            header[24] = "DEVICE_VALUE_SUM_COVERED";
            header[25] = "CREATION_DATE";
            header[26] = "SERIALNO";
            header[27] = "PARTNER_ID";
            header[28] = "VALUE_ADDED_SERVICES";
            header[29] = "DWH_UNIQUE_KEY";
            writer.writeNext(header);
            
            while(loRS.next()){
                loJSON = (JSONObject) loParser.parse(loRS.getString("sPayloadx"));
                
                // Write details
                String [] row = new String[header.length];
                
                for (int i = 0; i <= header.length-1; i++){
                    row[i] = loJSON.get(header[i]).toString();                    
                }
                writer.writeNext(row);
                
                lsSQL = "UPDATE CP_SO_Insurance SET" +
                            "  cTranStat = '1'" +
                            ", sBatchNox = " + SQLUtil.toSQL(lsTransNox) +
                        " WHERE sTransNox = " + SQLUtil.toSQL(loRS.getString("sTransNox"));
                
                if (_instance.executeUpdate(lsSQL) <= 0) {
                    loJSON.put("result", "error");
                    loJSON.put("message", _instance.getMessage() + "; " + _instance.getErrMsg());
                    return loJSON;
                }
            }
        } catch (IOException | SQLException | ParseException e) {
            loJSON = new JSONObject();
            loJSON.put("result", "error");
            loJSON.put("message", e.getMessage());
            return loJSON;
        }
        
        loJSON.put("result", "success");
        loJSON.put("message", "Transactions exported successfully.");
        return loJSON;
    }
    
    public JSONObject UploadFile(){
        JSONObject loJSON = new JSONObject();
        
        try {
            loadConfig();
            
            Session session = null;
            ChannelSftp channelSftp = null;
            
            //connect to bolttech server
            JSch jsch = new JSch();
            jsch.addIdentity(System.getProperty("bolttech.pkey"));
            session = jsch.getSession(System.getProperty("bolttech.user"), 
                                            System.getProperty("bolttech.host"), 
                                            Integer.parseInt(System.getProperty("bolttech.port")));
            
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            //end - connect to bolttech server
            
            //get the unsent transactions
            String lsSQL = "SELECT sBatchNox, sFileName, cTranStat" +
                            " FROM Bolttech" +
                            " WHERE cTranStat IN ('0', '3')";
            
            ResultSet loRS = _instance.executeQuery(lsSQL);
            
            while (loRS.next()){
                String lsSRC;
                
                if (loRS.getString("cTranStat").equals("0")){
                    lsSRC = UPLOAD;
                } else{
                    lsSRC = FAILED;
                }
                
                try (FileInputStream fis = new FileInputStream(lsSRC + loRS.getString("sFileName"))) {
                    System.out.println(System.getProperty("bolttech.rdir") + loRS.getString("sFileName"));
                    channelSftp.put(fis, System.getProperty("bolttech.rdir") + loRS.getString("sFileName"));
                
                    //update the transaction status to sent
                    lsSQL = "UPDATE Bolttech SET" +
                                "  cTranStat = '1' " +
                                ", dDateSent = " + SQLUtil.toSQL(_instance.getServerDate()) +
                            " WHERE sBatchNox = " + SQLUtil.toSQL(loRS.getString("sBatchNox"));
                    if (_instance.executeUpdate(lsSQL) <= 0) {
                        loJSON.put("result", "error");
                        loJSON.put("message", _instance.getMessage() + "; " + _instance.getErrMsg());
                        return loJSON;
                    }
                    
                    fis.close();
                    
                    //move the file to success folder
                    Path sourcePath = Paths.get(lsSRC + loRS.getString("sFileName"));
                    Path destinationPath = Paths.get(SUCCESS + loRS.getString("sFileName"));
                    Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (SftpException ex) {
                    //update the transaction status to failed
                    lsSQL = "UPDATE Bolttech SET cTranStat = '3' WHERE sBatchNox = " + SQLUtil.toSQL(loRS.getString("sBatchNox"));
                    if (_instance.executeUpdate(lsSQL) <= 0) {
                        loJSON.put("result", "error");
                        loJSON.put("message", _instance.getMessage() + "; " + _instance.getErrMsg());
                        return loJSON;
                    }
                    
                    //move the file to unsent folder
                    Path sourcePath = Paths.get(lsSRC + loRS.getString("sFileName"));
                    Path destinationPath = Paths.get(FAILED + loRS.getString("sFileName"));
                    Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                    
                    loJSON = new JSONObject();
                    loJSON.put("result", "error");
                    loJSON.put("message", ex.getMessage());
                    return loJSON;
                }
            }
        } catch (JSchException | IOException | SQLException e) {
            loJSON = new JSONObject();
            loJSON.put("result", "error");
            loJSON.put("message", e.getMessage());
            return loJSON;
        }
        
        loJSON.put("result", "success");
        loJSON.put("message", "Files uploaded successfully.");
        return loJSON;
    }
    
    public JSONObject CreateReport(String fdDateFrom, String fdDateThru){
        String lsSQL = MiscUtil.addCondition(getSQ_Sent(), 
                            "b.dTransact BETWEEN " + SQLUtil.toSQL(fdDateFrom) + " AND " + SQLUtil.toSQL(fdDateThru));
              
        ResultSet loRS = _instance.executeQuery(lsSQL);
        
        JSONObject loJSON = new JSONObject();
        
        if (MiscUtil.RecordCount(loRS) <= 0){
            loJSON.put("result", "success");
            loJSON.put("message", "No record found.");
            return loJSON;
        }
        
        String lsFilename = "Bolttech " + SQLUtil.dateFormat(_instance.getServerDate(), SQLUtil.FORMAT_TIMESTAMPX) + ".csv";
        
        try (CSVWriter writer = new CSVWriter(new FileWriter(REPORT + lsFilename))){
            String [] header = new String[22];
            header[0] = "SI_NUMBER";
            header[1] = "CONTRACT_SOLD_DATE";
            header[2] = "PRODUCT_NAME";
            header[3] = "PRODUCT_AMOUNT";
            header[4] = "CUST_NAME";
            header[5] = "CUST_MOBILE_NO";
            header[6] = "CUST_EMAIL";
            header[7] = "CUST_ADDRESS";
            header[8] = "CUST_CITY";
            header[9] = "STORE_CODE";
            header[10] = "STORE_NAME";
            header[11] = "STORE_ADDRESS";
            header[12] = "STORE_CITY";
            header[13] = "SALES_REP_ID";
            header[14] = "SALES_REP_NAME";
            header[15] = "DEVICE_TYPE";
            header[16] = "DEVICE_MAKE";
            header[17] = "DEVICE_MODEL";
            header[18] = "COLOR";
            header[19] = "DEVICE_RRP";
            header[20] = "IMEI";
            header[21] = "SERIALNO";
            writer.writeNext(header);
            
            JSONParser loParser = new JSONParser();
            
            while (loRS.next()){
                loJSON = (JSONObject) loParser.parse(loRS.getString("sPayloadx"));
                
                lsSQL = MiscUtil.addCondition(getSQ_Master(), "a.sTransNox = " + SQLUtil.toSQL(loRS.getString("sSourceNo")));
                ResultSet loRx = _instance.executeQuery(lsSQL);
                
                String [] row = new String[header.length];
                
                for (int i = 0; i <= header.length-1; i++){
                    switch(header[i]){
                        case "SI_NUMBER":
                            row[i] = loJSON.get("CLIENT_TRANS_NO").toString();
                            break;
                        case "PRODUCT_AMOUNT":
                            if (loRx.next()){
                                row[i] = String.valueOf(loRx.getDouble("nUnitPrce"));
                            } else {
                                row[i] = "0.00";
                            }
                            break;
                        default:
                            row[i] = loJSON.get(header[i]).toString();
                    }
                                        
                }
                writer.writeNext(row);
            }
            loJSON.put("result", "success");
            loJSON.put("message", "Report exported successfully.");
        } catch (SQLException | IOException | ParseException e) {
            loJSON.put("result", "error");
            loJSON.put("message", e.getMessage());
        }
        
        return loJSON;
    }
    
    private void loadConfig() throws IOException{
        Properties props = new Properties();
        props.load(new FileInputStream(System.getProperty("sys.default.path.config") + "/config/maven.properties"));
        
        System.setProperty("bolttech.port", props.getProperty("bolttech.port"));
        System.setProperty("bolttech.host", props.getProperty("bolttech.host"));
        System.setProperty("bolttech.user", props.getProperty("bolttech.user"));
        System.setProperty("bolttech.rdir", props.getProperty("bolttech.rdir"));
        System.setProperty("bolttech.pkey", System.getProperty("sys.default.path.config") + "/config/" + props.getProperty("bolttech.pkey"));
    }
    
    private String getSQ_Master(){
        return "SELECT" +
                    "  a.sTransNox" +
                    ", b.sStockIDx" +
                    ", c.sCategID1" +
                    ", c.sCategID2" +
                    ", f.sCategrNm" +
                    ", e.sTransNox xTransNox" +
                    ", b.nUnitPrce" +
                " FROM CP_SO_Master a" +
                    " LEFT JOIN CP_SO_Insurance e ON a.sTransNox = e.sSourceNo AND e.cTranStat <> '3'" +
                    ", CP_SO_Detail b" +
                        " LEFT JOIN CP_Inventory c ON b.sStockIDx = c.sStockIDx" +
                        " LEFT JOIN Category d ON c.sCategID1 = d.sCategrID" +
                        " LEFT JOIN Category f ON c.sCategID2 = f.sCategrID" +
                " WHERE a.sTransNox = b.sTransNox" +
                    " AND a.cTranStat NOT IN ('3', '7')" +
                    " AND a.dTransact >= '2024-06-27'" +
                " HAVING c.sCategID1 = 'C001052'";
    }
    
    private String getSQ_Detail(){
        return "SELECT" + 
                    "  a.sSalesInv CLIENT_TRANS_NO" + 
                    ", DATE_FORMAT(a.dTransact,'%d/%m/%Y') CONTRACT_SOLD_DATE" + 
                    ", f.sCategrNm PRODUCT_NAME" + 
                    ", TRIM(CONCAT(g.sFrstName, ' ', g.sLastName )) CUST_NAME" + 
                    ", '' CUST_ID" + 
                    ", CONCAT('63', RIGHT(g.sMobileNo, 10)) CUST_MOBILE_NO" + 
                    ", g.sEmailAdd CUST_EMAIL" + 
                    ", TRIM(CONCAT(g.sAddressx, ' ', j.sTownName, ', ', h.sProvName)) CUST_ADDRESS" + 
                    ", '' CUST_CITY" + 
                    ", d.sBranchCd STORE_CODE" + 
                    ", d.sBranchNm STORE_NAME" + 
                    ", '' STORE_ADDRESS" + 
                    ", i.sTownName STORE_CITY" + 
                    ", a.sSalesman SALES_REP_ID" + 
                    ", IFNULL(TRIM(CONCAT(k.sFrstName, ' ', k.sLastName)), '') SALES_REP_NAME" + 
                    ", c.nPurchase DEVICE_RRP" + 
                    ", e.sCategrNm DEVICE_TYPE" + 
                    ", l.sBrandNme DEVICE_MAKE" + 
                    ", m.sModelNme DEVICE_MODEL" + 
                    ", n.sColorNme COLOR" + 
                    ", o.sSerialNo IMEI" + 
                    ", e.sCategrNm NAME_GOODS_TYPE" + 
                    ", '' DTIME_START" + 
                    ", '' DTIME_END" + 
                    ", c.nPurchase DEVICE_VALUE_SUM_COVERED" + 
                    ", '' CREATION_DATE" + 
                    ", o.sSerialNo SERIALNO" + 
                    ", 'PHGUANZRETNA01' PARTNER_ID" + 
                    ", 'TY_MBK_GR' VALUE_ADDED_SERVICES" + 
                    ", '' DWH_UNIQUE_KEY" + 
                    ", c.sBrandIDx BRAND_ID" +
                    ", c.sCategID2 CATEG_ID" +
                    ", c.sCategID4 CATEG_ID4" +
                    ", b.nUnitPrce PRODUCT_AMOUNT" +
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
                    " AND a.cTranStat NOT IN ('3', '7')" +
                " HAVING PRODUCT_NAME = 'Units'";
    }
    
    private String getSQ_Batch(){
        return "SELECT *" +
                " FROM CP_SO_Insurance" +
                " WHERE cTranStat = '0'" +
                    " AND sBatchNox IS NULL";
    }
    
    private String getSQ_Sent(){
        return "SELECT" +
                    "  a.sTransNox" +
                    ", a.sSourceNo" +
                    ", b.dTransact" +
                    ", a.sPayloadx" +
                    ", c.sFileName" +
                    ", c.dCreatedx" +
                    ", c.dDateSent" +
                " FROM CP_SO_Insurance a" +
                    ", CP_SO_Master b" +
                    ", Bolttech c" +
                " WHERE a.sSourceNo = b.sTransNox" +
                    " AND a.sBatchNox = c.sBatchNox" +
                    " AND a.cTranStat = '1'" +
                    " AND c.cTranStat = '1'" +
                " ORDER BY b.dTransact";
    }
}
