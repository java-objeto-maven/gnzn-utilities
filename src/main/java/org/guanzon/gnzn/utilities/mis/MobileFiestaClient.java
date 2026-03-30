package org.guanzon.gnzn.utilities.mis;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.guanzon.appdriver.base.GRider;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.sql.ResultSet;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;

public class MobileFiestaClient {
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
            
            File file = new File("D:\\GGC_Maven_Systems\\temp\\client name.xlsx");
            if (!file.exists()) {
                System.out.println("File not found: " + file.getAbsolutePath());
                System.exit(1);
            }
            System.out.println("File opened.");
            
            try (FileInputStream fis = new FileInputStream(file);
                Workbook workbook = new XSSFWorkbook(fis)) {

                Sheet sheet = workbook.getSheetAt(0);

                Row headerRow = sheet.getRow(0);

                int lnTransNox = -1;
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    if ("sTransNox".equalsIgnoreCase(headerRow.getCell(i).getStringCellValue())) {
                        lnTransNox = i;
                        break;
                    }
                }
                
                int lnLastName = -1;
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    if ("sLastName".equalsIgnoreCase(headerRow.getCell(i).getStringCellValue())) {
                        lnLastName = i;
                        break;
                    }
                }
                
                int lnFrstName = -1;
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    if ("sFrstName".equalsIgnoreCase(headerRow.getCell(i).getStringCellValue())) {
                        lnFrstName = i;
                        break;
                    }
                }
                
                int lnAddressx = -1;
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    if ("sAddressx".equalsIgnoreCase(headerRow.getCell(i).getStringCellValue())) {
                        lnAddressx = i;
                        break;
                    }
                }
                
                int lnClientID = -1;
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    if ("sClientID".equalsIgnoreCase(headerRow.getCell(i).getStringCellValue())) {
                        lnClientID = i;
                        break;
                    }
                }

                if (lnTransNox == -1) {
                    System.out.println("⚠ 'UID' column not found!");
                } else {
                    instance.beginTrans();
                    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                        Row row = sheet.getRow(i);
                        if (row == null) continue;
                        
                        Cell transnox = row.getCell(lnTransNox);
                        Cell lastname = row.getCell(lnLastName);
                        Cell frstname = row.getCell(lnFrstName);
                        Cell addressx = row.getCell(lnAddressx);
                        
                        String sql = "SELECT sTownIDxx" +
                                        " FROM TownCity" +
                                        " WHERE sTownName LIKE " + SQLUtil.toSQL(addressx.getStringCellValue().trim() + "%") +
                                            " AND cRecdStat = '1'" +
                                        " LIMIT 1";     
                        ResultSet rs = instance.executeQuery(sql);
                        
                        String lsTownIDxx = "";                        
                        
                        if (rs.next()){
                            lsTownIDxx = rs.getString("sTownIDxx");
                        } else {
                            lsTownIDxx = "0314";
                        }
                        
                        String sClientID = MiscUtil.getNextCode("Client_Master", "sClientID", true, instance.getConnection(), instance.getBranchCode());
                        
                        sql = "INSERT INTO Client_Master SET" +
                                "  sClientID = " + SQLUtil.toSQL(sClientID) +
                                ", sLastName = " + SQLUtil.toSQL(lastname.getStringCellValue().trim()) + 
                                ", sFrstName = " + SQLUtil.toSQL(frstname.getStringCellValue().trim()) + 
                                ", sMiddName = ''" +
                                ", cGenderCd = '0'" +
                                ", cCvilStat = '0'" +
                                ", sCitizenx = ''" +
                                ", dBirthDte = '1900-01-01 00:00:00'" +
                                ", sBirthPlc = ''" +
                                ", sHouseNox = ''" +
                                ", sAddressx = ''" +
                                ", sTownIDxx = " + SQLUtil.toSQL(lsTownIDxx) + 
                                ", sPhoneNox = ''" +
                                ", sMobileNo = ''" +
                                ", sEmailAdd = ''" +
                                ", sTaxIDNox = ''" +
                                ", sAddlInfo = ''" +
                                ", sCompnyNm = " + SQLUtil.toSQL(lastname.getStringCellValue().trim() + ", " + frstname.getStringCellValue().trim()) +
                                ", sClientNo = ''" +
                                ", sSpouseID = ''" +
                                ", sFatherID = ''" +
                                ", sMotherID = ''" +
                                ", sSiblngID = ''" +
                                ", cLRClient = '0'" +
                                ", cMCClient = '0'" +
                                ", cSCClient = '0'" +
                                ", cSPClient = '0'" +
                                ", cCPClient = '1'" +
                                ", sBrgyIDxx = ''" +
                                ", sMaidenNm = ''" +
                                ", sSSSNoxxx = ''" +
                                ", cEducLevl = '0'" +
                                ", sRelgnIDx = ''" +
                                ", sOccptnID = ''" +
                                ", nGrssIncm = 0" +
                                ", cClientTp = '0'" +
                                ", sSuffixNm = ''" +
                                ", sOccptnOT = ''" +
                                ", cRecdStat = '1'" +
                                ", sModified = " + SQLUtil.toSQL(instance.getUserID()) +
                                ", dModified = " + SQLUtil.toSQL(instance.getServerDate());
                        
                        System.out.println(sql);
                        if (instance.executeQuery(sql, "Client_Master", instance.getBranchCode(), "") <= 0){
                            instance.rollbackTrans();
                            System.err.println("Unable to add client record...");
                            System.exit(1);
                        }
                        
                        sql = "UPDATE CP_SO_Master SET" +
                                    "  sClientID  = " + SQLUtil.toSQL(sClientID) +
                                " WHERE sTransNox = " + SQLUtil.toSQL(transnox.getStringCellValue().trim());
                        
                        System.out.println(sql);
                        if (instance.executeQuery(sql, "CP_SO_Master", instance.getBranchCode(), "") <= 0){
                            instance.rollbackTrans();
                            System.err.println("Unable to update CP Sales record...");
                            System.exit(1);
                        }
                        
                        Cell uid = row.getCell(lnClientID);
                        uid.setCellValue(sClientID);
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
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}