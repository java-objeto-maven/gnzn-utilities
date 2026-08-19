package org.guanzon.gnzn.utilities.goals;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.guanzon.appdriver.base.GRider;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.base.StringHelper;

public class Spareparts {
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
            
            File file = new File("D:\\GGC_Maven_Systems\\temp\\8B for QR Code & ID Number.xlsx");
            if (!file.exists()) {
                System.out.println("File not found: " + file.getAbsolutePath());
                System.exit(1);
            }
            System.out.println("File opened.");
            
            try (FileInputStream fis = new FileInputStream(file);
                Workbook workbook = new XSSFWorkbook(fis)) {

                Sheet sheet = workbook.getSheetAt(0);

                Row headerRow = sheet.getRow(0);
                
                int areaId = -1;
                int branchId = -1;
                int janId = -1;
                int febId = -1;
                int marId = -1;
                int aprId = -1;
                int mayId = -1;
                int junId = -1;
                int julId = -1;
                int augId = -1;
                int sepId = -1;
                int octId = -1;
                int novId = -1;
                int decId = -1;
                int capturedId = -1;
                
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    switch (headerRow.getCell(i).getStringCellValue()){
                        case "Area":
                            areaId = i;
                            break;
                        case "Branch":
                            branchId = i;
                            break;
                        case "Jan":
                            janId = i;
                            break;
                        case "Feb":
                            febId = i;
                            break;
                        case "Mar":
                            marId = i;
                            break;
                        case "Apr":
                            aprId = i;
                            break;
                        case "May":
                            mayId = i;
                            break;
                        case "Jun":
                            junId = i;
                            break;
                        case "Jul":
                            julId = i;
                            break;
                        case "Aug":
                            augId = i;
                            break;
                        case "Sep":
                            sepId = i;
                            break;
                        case "Oct":
                            octId = i;
                            break;
                        case "Nov":
                            novId = i;
                            break;
                        case "Dec":
                            decId = i;
                            break;
                        case "Captured":
                            capturedId = i;
                            break;
                    }
                }

                if (branchId == -1) {
                    System.out.println("Branch column not found!");
                } else {
                    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                        Row row = sheet.getRow(i);
                        if (row == null) continue;
                        
                        Cell branch = row.getCell(branchId);

                        String sql = "SELECT sBranchCd FROM Branch WHERE sBranchNm LIKE " + SQLUtil.toSQL("%" + branch.getStringCellValue().trim() + "%");

                        ResultSet rs = instance.executeQuery(sql);
                       
                        if (rs.next()){
                            Cell captured = row.getCell(capturedId);
                            
                            if (captured.getStringCellValue().equalsIgnoreCase("no")){
                                sql = "INSERT INTO MC_Branch_Performance SET" +
                                    "  sBranchCd = " + SQLUtil.toSQL(rs.getString("sBranchCd")) +
                                    ", sPeriodxx = " + SQLUtil.toSQL("");
                            
                            
                                captured.setCellValue("YES");
                            }
                            
                            
                            
                            Cell sysName = row.getCell(systemName);
                            sysName.setCellValue(rs.getString("sCompnyNm"));
                            
                            Cell idno = row.getCell(idNumber);
                            
                            if (rs.getString("sIDNumber").isEmpty()){
                                sql = generateEmployeeID(instance, rs.getString("dHiredxxx"));
                                
                                idno.setCellValue(sql);
                                
                                sql = "UPDATE Employee_Master001 SET sIDNumber = " + SQLUtil.toSQL(sql) +
                                        " WHERE sEmployID = " + SQLUtil.toSQL(rs.getString("sEmployID"));
                                
                                instance.beginTrans();
                                if (instance.executeQuery(sql, "Employee_Master001", instance.getBranchCode(), "") <= 0){
                                    instance.rollbackTrans();
                                    System.err.println("Unable to update ID Number...");
                                    System.exit(1);
                                }
                                instance.commitTrans();
                            } else {
                                idno.setCellValue(rs.getString("sIDNumber"));
                            }
                        }
                   }
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
