package org.guanzon.gnzn.utilities.mis;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.guanzon.appdriver.base.GRider;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.sql.ResultSet;
import org.guanzon.appdriver.base.SQLUtil;

public class EmployeeIDFIll {
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
            
            File file = new File("D:\\GGC_Maven_Systems\\temp\\Internal List.xlsx");
            if (!file.exists()) {
                System.out.println("File not found: " + file.getAbsolutePath());
                System.exit(1);
            }
            System.out.println("File opened.");
            
            try (FileInputStream fis = new FileInputStream(file);
                Workbook workbook = new XSSFWorkbook(fis)) {

                Sheet sheet = workbook.getSheetAt(0);
                Row headerRow = sheet.getRow(0);
                
                int employeeId = -1;
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    if ("Employee ID".equalsIgnoreCase(headerRow.getCell(i).getStringCellValue())) {
                        employeeId = i;
                        break;
                    }
                }
                
                int branchCode = -1;
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    if ("Branch".equalsIgnoreCase(headerRow.getCell(i).getStringCellValue())) {
                        branchCode = i;
                        break;
                    }
                }
                
                int deptId = -1;
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    if ("Department ID".equalsIgnoreCase(headerRow.getCell(i).getStringCellValue())) {
                        deptId = i;
                        break;
                    }
                }
                
                int positionId = -1;
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    if ("Position ID".equalsIgnoreCase(headerRow.getCell(i).getStringCellValue())) {
                        positionId = i;
                        break;
                    }
                }
                
                int tenure = -1;
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    if ("Tenure".equalsIgnoreCase(headerRow.getCell(i).getStringCellValue())) {
                        tenure = i;
                        break;
                    }
                }
                
                int employeeName = -1;
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    if ("Name".equalsIgnoreCase(headerRow.getCell(i).getStringCellValue())) {
                        employeeName = i;
                        break;
                    }
                }
                
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;
                    
                    String value = "";

                    // Update existing "Score" column (+5 points)
                    Cell empname = row.getCell(employeeName);
                    value = empname.getStringCellValue().trim();
                    
                    String sql = "SELECT" +
                                        "  a.sEmployID" +
                                        ", a.sBranchCd" +
                                        ", a.sDeptIDxx" +
                                        ", a.sPositnID" +
                                        ", ROUND(DATEDIFF('2025-09-06', IFNULL(a.dRegularx, a.dStartEmp)) / 365, 2) nServicex" +
                                    " FROM Employee_Master001 a" +
                                        ", Client_Master b" +
                                    " WHERE a.sEmployID = b.sClientID" +
                                        " AND b.sCompnyNm LIKE " + SQLUtil.toSQL(value + "%");
                    
                    ResultSet rs = instance.executeQuery(sql);
                    
                    if (rs.next()){
                        Cell rowcel = row.createCell(employeeId);
                        rowcel.setCellValue(rs.getString("sEmployID"));
                        
                        rowcel = row.createCell(branchCode);
                        rowcel.setCellValue(rs.getString("sBranchCd"));
                        
                        rowcel = row.createCell(deptId);
                        rowcel.setCellValue(rs.getString("sDeptIDxx"));
                        
                        rowcel = row.createCell(positionId);
                        rowcel.setCellValue(rs.getString("sPositnID"));
                        
                        rowcel = row.createCell(tenure);
                        rowcel.setCellValue(rs.getDouble("nServicex"));
                    } else {
                        System.out.println("NOT FOUND - " + value);
                    }
                }
                
                
               // ✅ Save as a new file instead of overwriting
               String outputPath = file.getParent() + File.separator +
                                   file.getName().replace(".xlsx", "_updated.xlsx");
               try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                   workbook.write(fos);
               }

               System.out.println("Excel file saved as: " + outputPath);
           } catch (Exception e) {
               e.printStackTrace();
           }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void processExcel(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            // ✅ Add a new column "Status"
            Row headerRow = sheet.getRow(0);
            int newColIndex = headerRow.getLastCellNum();
            headerRow.createCell(newColIndex).setCellValue("Status");

            // ✅ Find "Score" column index
            int scoreColIndex = -1;
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                if ("Score".equalsIgnoreCase(headerRow.getCell(i).getStringCellValue())) {
                    scoreColIndex = i;
                    break;
                }
            }

            if (scoreColIndex == -1) {
                System.out.println("⚠ 'Score' column not found!");
            } else {
                // ✅ Loop through rows, update "Score", and set "Status"
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    // Update existing "Score" column (+5 points)
                    Cell scoreCell = row.getCell(scoreColIndex);
                    if (scoreCell != null && scoreCell.getCellType() == CellType.NUMERIC) {
                        double score = scoreCell.getNumericCellValue();
                        scoreCell.setCellValue(score + 5); // update value
                    }

                    // Add new "Status" column
                    Cell statusCell = row.createCell(newColIndex);
                    if (scoreCell != null && scoreCell.getCellType() == CellType.NUMERIC) {
                        double updatedScore = scoreCell.getNumericCellValue();
                        statusCell.setCellValue(updatedScore >= 75 ? "Pass" : "Fail");
                    } else {
                        statusCell.setCellValue("N/A");
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
