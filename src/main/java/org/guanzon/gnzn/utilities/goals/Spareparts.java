package org.guanzon.gnzn.utilities.goals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.SQLUtil;

public class Spareparts {
    private final static String YEAR = "2025";
    
    // Column header -> DB period code, in one place instead of 14 loose ints + a 15-way switch
    private static final String[] MONTH_HEADERS = {
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };
    private static final String[] MONTH_PERIODS = {
        "01", "02", "03", "04", "05", "06",
        "07", "08", "09", "10", "11", "12"
    };

    public static void main(String[] args) {
        String path = System.getProperty("os.name").toLowerCase().contains("win")
                ? "D:/GGC_Maven_Systems"
                : "/srv/GGC_Maven_Systems";
        System.setProperty("sys.default.path.config", path);

        try {
            Properties poProps = new Properties();
            try (FileInputStream propsIn = new FileInputStream(path + "/config/cas.properties")) {
                poProps.load(propsIn);
            }

            if (!"1".equals(poProps.getProperty("developer.mode"))) {
                System.err.println("Unable to log user.");
                System.exit(1);
                return;
            }

            GRider instance = new GRider("gRider");
            if (!instance.logUser("gRider", "M001000001")) {
                System.exit(1);
                return;
            }

            File file = new File(path + "/temp/SP Goal " + YEAR + ".xlsx");
            if (!file.exists()) {
                System.out.println("File not found: " + file.getAbsolutePath());
                System.exit(1);
                return;
            }
            System.out.println("File opened.");

            boolean anyRowModified = false;

            try (FileInputStream fis = new FileInputStream(file);
                 Workbook workbook = new XSSFWorkbook(fis)) {

                Sheet sheet = workbook.getSheetAt(0);
                Row headerRow = sheet.getRow(0);

                // Build header index once instead of a 15-branch switch per column
                Map<String, Integer> headerIndex = new HashMap<>();
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    Cell cell = headerRow.getCell(i);
                    if (cell != null && cell.getCellType() == CellType.STRING) {
                        headerIndex.put(cell.getStringCellValue().trim(), i);
                    }
                }

                Integer branchIdx = headerIndex.get("Branch");
                Integer capturedIdx = headerIndex.get("Captured");

                int[] monthIdx = new int[MONTH_HEADERS.length];
                for (int m = 0; m < MONTH_HEADERS.length; m++) {
                    Integer idx = headerIndex.get(MONTH_HEADERS[m]);
                    monthIdx[m] = (idx != null) ? idx : -1;
                }

                if (branchIdx == null) {
                    System.out.println("Branch column not found!");
                } else {
                    int lastRow = sheet.getLastRowNum();

                    for (int i = 1; i <= lastRow; i++) {
                        Row row = sheet.getRow(i);
                        if (row == null) continue;

                        Cell branchCell = row.getCell(branchIdx);
                        if (branchCell == null || branchCell.getCellType() != CellType.STRING) continue;

                        String branchName = branchCell.getStringCellValue().trim();
                        if (branchName.isEmpty()) continue;

                        Cell capturedCell = (capturedIdx != null) ? row.getCell(capturedIdx) : null;
                        boolean alreadyMarkedNo = capturedCell != null
                                && capturedCell.getCellType() == CellType.STRING
                                && "no".equalsIgnoreCase(capturedCell.getStringCellValue());

                        // Skip rows that don't need work before even touching the DB
                        if (!alreadyMarkedNo) continue;

                        String lookupSql = "SELECT sBranchCd FROM Branch WHERE sBranchNm LIKE "
                                + SQLUtil.toSQL("%" + branchName + "%");

                        String sBranchCd = null;
                        try (ResultSet rs = instance.executeQuery(lookupSql)) {
                            if (rs.next()) {
                                sBranchCd = rs.getString("sBranchCd");
                            }
                        }

                        if (sBranchCd == null) continue;

                        // Computed once per row instead of once per month (12x fewer calls/allocations)
                        String sBranchCdSql = SQLUtil.toSQL(sBranchCd);
                        String serverDateSql = SQLUtil.toSQL(instance.getServerDate());

                        instance.beginTrans();
                        boolean rowOk = true;

                        for (int m = 0; m < monthIdx.length; m++) {
                            if (monthIdx[m] == -1) continue;

                            Cell monthCell = row.getCell(monthIdx[m]);
                            double goal = (monthCell != null && monthCell.getCellType() == CellType.NUMERIC)
                                    ? monthCell.getNumericCellValue()
                                    : 0d;

                            String upsertSql = "INSERT INTO MC_Branch_Performance SET"
                                                    + "  sBranchCd = " + sBranchCdSql
                                                    + ", sPeriodxx = " + SQLUtil.toSQL(YEAR + MONTH_PERIODS[m])
                                                    + ", nSPGoalxx = " + goal
                                                    + ", dModified = " + serverDateSql
                                                + " ON DUPLICATE KEY UPDATE "
                                                    + " nSPGoalxx = " + goal
                                                    + ", dModified = " + serverDateSql;

                            if (instance.executeQuery(upsertSql, "MC_Branch_Performance", instance.getBranchCode(), "") <= 0) {
                                System.err.println("Unable to execute command: " + upsertSql);
                                instance.rollbackTrans();
                                rowOk = false;
                                break;
                            }
                        }

                        if (!rowOk) {
                            System.exit(1);
                            return;
                        }

                        instance.commitTrans();
                        capturedCell.setCellValue("YES");
                        anyRowModified = true;
                    }
                }

                // Write the workbook ONCE, after all rows are processed, instead of once per matched row.
                // This alone removes an O(n^2) disk-I/O pattern on large sheets.
                if (anyRowModified) {
                    String outputPath = file.getParent() + File.separator
                            + file.getName().replace(".xlsx", "_updated.xlsx");
                    try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                        workbook.write(fos);
                    }
                    System.out.println("Updated file written to: " + outputPath);
                } else {
                    System.out.println("No rows required updating.");
                }
            }

            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}