package org.guanzon.gnzn.utilities.mis;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.guanzon.appdriver.base.GRider;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.base.StringHelper;
import org.w3c.dom.ls.LSSerializer;

public class CreatePayeeClient {
    public static void main (String [] args){
        try {
            String path;
            String lsTemp;
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                path = "D:/GGC_Maven_Systems";
                lsTemp = "D:/temp";
            } else {
                path = "/srv/GGC_Maven_Systems";
                lsTemp = "/srv/temp";
            }
            System.setProperty("sys.default.path.config", path);
            

            GRiderCAS instance = new GRiderCAS("gRider");

            if (!instance.logUser("gRider", "M001000001")) {
                System.err.println(instance.getMessage());
                System.exit(1);
            }
            
            File file = new File("D:\\GGC_Maven_Systems\\temp\\Payee.xlsx");
            if (!file.exists()) {
                System.out.println("File not found: " + file.getAbsolutePath());
                System.exit(1);
            }
            System.out.println("File opened.");
            
            try (FileInputStream fis = new FileInputStream(file);
                Workbook workbook = new XSSFWorkbook(fis)) {

                Sheet sheet = workbook.getSheetAt(0);

                Row headerRow = sheet.getRow(0);

                int company = -1;
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    if ("Company".equalsIgnoreCase(headerRow.getCell(i).getStringCellValue())) {
                        company = i;
                        break;
                    }
                }
                
                int tin = -1;
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    if ("TIN".equalsIgnoreCase(headerRow.getCell(i).getStringCellValue())) {
                        tin = i;
                        break;
                    }
                }
                
                int address = -1;
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    if ("ADDRESS".equalsIgnoreCase(headerRow.getCell(i).getStringCellValue())) {
                        address = i;
                        break;
                    }
                }
                
                int payeeId = -1;
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    if ("PAYEE ID".equalsIgnoreCase(headerRow.getCell(i).getStringCellValue())) {
                        payeeId = i;
                        break;
                    }
                }
                
                int clientId = -1;
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    if ("CLIENT ID".equalsIgnoreCase(headerRow.getCell(i).getStringCellValue())) {
                        clientId = i;
                        break;
                    }
                }
                
                int addressId = -1;
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    if ("ADDRESS ID".equalsIgnoreCase(headerRow.getCell(i).getStringCellValue())) {
                        addressId = i;
                        break;
                    }
                }
                
                int cpersonId = -1;
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    if ("CPERSON ID".equalsIgnoreCase(headerRow.getCell(i).getStringCellValue())) {
                        cpersonId = i;
                        break;
                    }
                }

                if (company == -1) {
                    System.out.println("⚠ 'COMPANY' column not found!");
                } else {
                    String lsSQL = "";
                    
                    instance.beginTrans("Create payee client id", "", "mac", "");
                    
                    lsSQL = "DELETE FROM Client_Master WHERE sClientID BETWEEN 'GCO126000005' AND 'GCO126000254'";
                    if (instance.executeQuery(lsSQL, "Client_Master", instance.getBranchCode(), "", "") <= 0){
                        instance.rollbackTrans();
                        System.out.println("Unable to remove client master record...");
                        System.exit(1);
                    }
                    
                    lsSQL = "DELETE FROM Client_Address WHERE sClientID BETWEEN 'GCO126000005' AND 'GCO126000254'";
                    if (instance.executeQuery(lsSQL, "Client_Master", instance.getBranchCode(), "", "") <= 0){
                        instance.rollbackTrans();
                        System.out.println("Unable to remove client address record...");
                        System.exit(1);
                    }
                    
                    lsSQL = "DELETE FROM Client_Institution_Contact_Person WHERE sClientID BETWEEN 'GCO126000005' AND 'GCO126000254'";
                    if (instance.executeQuery(lsSQL, "Client_Master", instance.getBranchCode(), "", "") <= 0){
                        instance.rollbackTrans();
                        System.out.println("Unable to remove client contact record...");
                        System.exit(1);
                    }
                                        
                    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                        Row row = sheet.getRow(i);
                        if (row == null) continue;
                        
                        Cell lsCompnyNm = row.getCell(company);
                        Cell lsTINumber = row.getCell(tin);
                        Cell lsAddressx = row.getCell(address);
                        Cell lsPayeeIdx = row.getCell(payeeId);

                        String lsClientID = MiscUtil.getNextCode("Client_Master", "sClientID", true, instance.getGConnection().getConnection(), instance.getBranchCode());
                        String lsAddrssID = MiscUtil.getNextCode("Client_Address", "sAddrssID", true, instance.getGConnection().getConnection(), instance.getBranchCode());
                        String lsPersonID = MiscUtil.getNextCode("Client_Institution_Contact_Person", "sContctID", true, instance.getGConnection().getConnection(), instance.getBranchCode());
                        
                        lsSQL = "INSERT INTO Client_Master SET" +                                
                                "  sClientID = " + SQLUtil.toSQL(lsClientID) +
                                ", cClientTp = " + SQLUtil.toSQL("1") +
                                ", sLastName = " + SQLUtil.toSQL("") +
                                ", sFrstName = " + SQLUtil.toSQL("") +
                                ", sMiddName = " + SQLUtil.toSQL("") +
                                ", sSuffixNm = " + SQLUtil.toSQL("") +
                                ", sMaidenNm = " + SQLUtil.toSQL("") +
                                ", sCompnyNm = " + SQLUtil.toSQL(lsCompnyNm.getStringCellValue().trim().toUpperCase()) +
                                ", cGenderCd = " + SQLUtil.toSQL("0") +
                                ", cCvilStat = " + SQLUtil.toSQL("0") +
                                ", sCitizenx = " + SQLUtil.toSQL("") +
                                ", dBirthDte = " + SQLUtil.toSQL("1900-01-01") +
                                ", sBirthPlc = " + SQLUtil.toSQL("") +
                                ", sAddlInfo = " + SQLUtil.toSQL("") +
                                ", sSpouseID = " + SQLUtil.toSQL("") +
                                ", sTaxIDNox = " + SQLUtil.toSQL(lsTINumber.getStringCellValue().trim().toUpperCase()) +
                                ", sLTOIDxxx = " + SQLUtil.toSQL("") +
                                ", sPHBNIDxx = " + SQLUtil.toSQL("") +
                                ", cLRClient = " + SQLUtil.toSQL("0") +
                                ", cMCClient = " + SQLUtil.toSQL("0") +
                                ", cSCClient = " + SQLUtil.toSQL("0") +
                                ", cSPClient = " + SQLUtil.toSQL("0") +
                                ", cCPClient = " + SQLUtil.toSQL("0") +
                                ", cEducLevl = " + SQLUtil.toSQL("") +
                                ", sRelgnIDx = " + SQLUtil.toSQL("") +
                                ", sSSSNoxxx = " + SQLUtil.toSQL("") +
                                ", sOccptnID = " + SQLUtil.toSQL("") +
                                ", sOccptnOT = " + SQLUtil.toSQL("") +
                                ", sClientNo = " + SQLUtil.toSQL("") +
                                ", sFatherID = " + SQLUtil.toSQL("") +
                                ", sMotherID = " + SQLUtil.toSQL("") +
                                ", sSiblngID = " + SQLUtil.toSQL("") +
                                ", cRecdStat = " + SQLUtil.toSQL("1") +
                                ", sModified = " + SQLUtil.toSQL(instance.Encrypt(instance.getUserID())) +
                                ", dModified = " + SQLUtil.toSQL(instance.getServerDate());
                                
                        if (instance.executeQuery(lsSQL, "Client_Master", instance.getBranchCode(), "", "") <= 0){
                            instance.rollbackTrans();
                            System.out.println("Unable to add client master record...");
                            System.exit(1);
                        }
                        
                        lsSQL = "INSERT INTO  Client_Address SET" +
                                "  sAddrssID = " + SQLUtil.toSQL(lsAddrssID) +
                                ", sClientID = " + SQLUtil.toSQL(lsClientID) +
                                ", sHouseNox = " + SQLUtil.toSQL("") +
                                ", sAddressx = " + SQLUtil.toSQL(lsAddressx.getStringCellValue().trim().toUpperCase()) +
                                ", sBrgyIDxx = " + SQLUtil.toSQL("1201283") +
                                ", sTownIDxx = " + SQLUtil.toSQL("0314") +
                                ", nLatitude = " + SQLUtil.toSQL(0.00000000000) +
                                ", nLongitud = " + SQLUtil.toSQL(0.00000000000) +
                                ", cPrimaryx = " + SQLUtil.toSQL("1") +
                                ", cOfficexx = " + SQLUtil.toSQL("0") +
                                ", cProvince = " + SQLUtil.toSQL("0") +
                                ", cBillingx = " + SQLUtil.toSQL("0") +
                                ", cShipping = " + SQLUtil.toSQL("0") +
                                ", cCurrentx = " + SQLUtil.toSQL("0") +
                                ", cLTMSAddx = " + SQLUtil.toSQL("0") +
                                ", sSourceCd = " + SQLUtil.toSQL("") +
                                ", sReferNox = " + SQLUtil.toSQL("") +
                                ", cRecdStat = " + SQLUtil.toSQL("1") +
                                ", dModified = " + SQLUtil.toSQL(instance.getServerDate());
                        
                        if (instance.executeQuery(lsSQL, "Client_Address", instance.getBranchCode(), "", "") <= 0){
                            instance.rollbackTrans();
                            System.out.println("Unable to add client address record...");
                            System.exit(1);
                        }
                        
                        lsSQL = "INSERT INTO  Client_Institution_Contact_Person SET" +
                                "  sContctID = " + SQLUtil.toSQL(lsPersonID) +
                                ", sClientID = " + SQLUtil.toSQL(lsClientID) +
                                ", sCategrCd = " + SQLUtil.toSQL("0000007") +
                                ", cCPrsonID = " + SQLUtil.toSQL("") +
                                ", sCPerson1 = " + SQLUtil.toSQL("") +
                                ", sCPPosit1 = " + SQLUtil.toSQL("") +
                                ", sJobTitle = " + SQLUtil.toSQL("") +
                                ", sDeprtmnt = " + SQLUtil.toSQL("") +
                                ", sRoleIDxx = " + SQLUtil.toSQL("") +
                                ", sMobileNo = " + SQLUtil.toSQL("") +
                                ", sTelNoxxx = " + SQLUtil.toSQL("") +
                                ", sFaxNoxxx = " + SQLUtil.toSQL("") +
                                ", sEMailAdd = " + SQLUtil.toSQL("") +
                                ", sAccount1 = " + SQLUtil.toSQL("") +
                                ", sAccount2 = " + SQLUtil.toSQL("") +
                                ", sAccount3 = " + SQLUtil.toSQL("") +
                                ", sRemarksx = " + SQLUtil.toSQL("") +
                                ", cPayeexxx = " + SQLUtil.toSQL("1") +
                                ", cPrimaryx = " + SQLUtil.toSQL("1") +
                                ", cRecdStat = " + SQLUtil.toSQL("1") +
                                ", sModified = " + SQLUtil.toSQL(instance.Encrypt(instance.getUserID())) +
                                ", dModified = " + SQLUtil.toSQL(instance.getServerDate());
                        
                        if (instance.executeQuery(lsSQL, "Client_Institution_Contact_Person", instance.getBranchCode(), "", "") <= 0){
                            instance.rollbackTrans();
                            System.out.println("Unable to add client contact record...");
                            System.exit(1);
                        }
                        
                        lsSQL = "INSERT INTO  AP_Client_Master SET" +
                                "  sClientID = " + SQLUtil.toSQL(lsClientID) + 
                                ", sAddrssID = " + SQLUtil.toSQL(lsAddrssID) + 
                                ", sContctID = " + SQLUtil.toSQL(lsPersonID) + 
                                ", sCategrCd = " + SQLUtil.toSQL("0000007") + 
                                ", dCltSince = " + SQLUtil.toSQL(instance.getServerDate()) + 
                                ", dBegDatex = " + SQLUtil.toSQL(instance.getServerDate()) + 
                                ", nBegBalxx = " + SQLUtil.toSQL(0.00) + 
                                ", sTermIDxx = " + SQLUtil.toSQL("") + 
                                ", nDiscount = " + SQLUtil.toSQL(0.00) + 
                                ", nCredLimt = " + SQLUtil.toSQL(0.00) + 
                                ", nABalance = " + SQLUtil.toSQL(0.00) + 
                                ", nOBalance = " + SQLUtil.toSQL(0.00) + 
                                ", nLedgerNo = " + SQLUtil.toSQL(0) + 
                                ", cVatablex = " + SQLUtil.toSQL("0") + 
                                ", cHoldAcct = " + SQLUtil.toSQL("0") + 
                                ", cAutoHold = " + SQLUtil.toSQL("0") + 
                                ", sRemarksx = " + SQLUtil.toSQL("") + 
                                ", sEmailAdd = " + SQLUtil.toSQL("") + 
                                ", cAutoSend = " + SQLUtil.toSQL("") + 
                                ", cPaymOptx = " + SQLUtil.toSQL("") + 
                                ", cHoldOrdr = " + SQLUtil.toSQL("") + 
                                ", cVATRegis = " + SQLUtil.toSQL("") + 
                                ", cPermitxx = " + SQLUtil.toSQL("") + 
                                ", cBackOrdr = " + SQLUtil.toSQL("") + 
                                ", cRecdStat = " + SQLUtil.toSQL("1") + 
                                ", sModified = " + SQLUtil.toSQL(instance.Encrypt(instance.getUserID())) +
                                ", dModified = " + SQLUtil.toSQL(instance.getServerDate());
                        
                        if (instance.executeQuery(lsSQL, "AP_Client_Master", instance.getBranchCode(), "", "") <= 0){
                            instance.rollbackTrans();
                            System.out.println("Unable to add ap client record...");
                            System.exit(1);
                        }
                        
                        lsSQL = "SELECT * FROM Payee WHERE sPayeeIDx = " + SQLUtil.toSQL(lsPayeeIdx.getStringCellValue().trim());
                        
                        ResultSet loRS = instance.executeQuery(lsSQL);
                        
                        if (loRS.next()){
                            if (loRS.getString("sClientID").isEmpty()){
                                lsSQL = "UPDATE Payee SET" + 
                                            "  sClientID = " + SQLUtil.toSQL(lsClientID) + 
                                            ", sAPClntID = " + SQLUtil.toSQL(lsClientID) + 
                                        " WHERE sPayeeIDx = " + SQLUtil.toSQL(lsPayeeIdx.getStringCellValue().trim());
                                
                                if (instance.executeQuery(lsSQL, "Payee", instance.getBranchCode(), "", "") <= 0){
                                    instance.rollbackTrans();
                                    System.out.println("Unable to update payee record...");
                                    System.exit(1);
                                }
                            }
                        }
                        
                        Cell client = row.getCell(clientId);
                        client.setCellValue(lsClientID);
                        
                        Cell addresx = row.getCell(addressId);
                        addresx.setCellValue(lsAddrssID);
                        
                        Cell person = row.getCell(cpersonId);
                        person.setCellValue(lsPersonID);
                   }
                    
                    instance.commitTrans();
               }

               // ✅ Save as a new file instead of overwriting
               String outputPath = file.getParent() + File.separator +
                                   file.getName().replace(".xlsx", "_updated.xlsx");
               try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                   workbook.write(fos);
               }

               System.out.println("Excel file saved as: " + outputPath);
               
               System.exit(0);
           } catch (Exception e) {
               e.printStackTrace();
           }
        } catch (IOException | SQLException | GuanzonException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
