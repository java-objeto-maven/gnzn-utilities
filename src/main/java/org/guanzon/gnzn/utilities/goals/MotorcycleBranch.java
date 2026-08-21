package org.guanzon.gnzn.utilities.goals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.SQLUtil;

public class MotorcycleBranch {
    // Used if the year can't be parsed out of the file name.
    private static final String DEFAULT_YEAR = "2026";
    private static final String DEFAULT_FILE_NAME = "FINAL MC SALES GOAL 2026 PER AREA.xlsx";
 
    // Pulls a 4-digit year out of a file name like "..._2026_PER_AREA.xlsx".
    private static final Pattern YEAR_IN_FILENAME = Pattern.compile("(19|20)\\d{2}");
 
    // Each entry is the accepted spellings for that month's header, checked case-insensitively.
    // "JUNE" is included alongside "JUN" because this workbook spells it out in full.
    private static final String[][] MONTH_ALIASES = {
        {"jan"}, {"feb"}, {"mar"}, {"apr"}, {"may"}, {"jun", "june"},
        {"jul"}, {"aug"}, {"sep", "sept"}, {"oct"}, {"nov"}, {"dec"}
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
 
            String fileName = (args.length > 0) ? args[0] : DEFAULT_FILE_NAME;
            File file = new File(path + "/temp/" + fileName);
            if (!file.exists()) {
                System.out.println("File not found: " + file.getAbsolutePath());
                System.exit(1);
                return;
            }
            System.out.println("File opened: " + file.getAbsolutePath());
 
            String year = (args.length > 1) ? args[1] : detectYear(fileName);
            System.out.println("Using year: " + year);
 
            boolean anyRowModified = false;
 
            try (FileInputStream fis = new FileInputStream(file);
                 Workbook workbook = new XSSFWorkbook(fis)) {
 
                int sheetCount = workbook.getNumberOfSheets();
                for (int s = 0; s < sheetCount; s++) {
                    Sheet sheet = workbook.getSheetAt(s);
                    String sheetName = sheet.getSheetName().trim();
 
                    System.out.println("Checking sheet \"" + sheetName + "\"...");
                    boolean sheetModified = processSheet(sheet, year, instance);
                    anyRowModified = anyRowModified || sheetModified;
                }
 
                // Write the workbook ONCE, after ALL sheets are processed.
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
 
    private static String detectYear(String fileName) {
        Matcher m = YEAR_IN_FILENAME.matcher(fileName);
        return m.find() ? m.group() : DEFAULT_YEAR;
    }
 
    /**
     * Processes a single sheet ONLY if it contains a "Branch" column — this is what tells a
     * per-branch data sheet (e.g. "R1-A", "BIGBIKE") apart from a per-area rollup sheet like
     * "COMPANY GOAL" (which has an "Area" column and date-typed month headers instead, and
     * gets skipped automatically since it has no "Branch" header).
     *
     * Column positions and month headers are resolved case-insensitively by name, so sheets
     * that spell "JUNE" in full, use ALL CAPS headers, or have extra columns (1Q..4Q, TTL)
     * all still work without changes.
     *
     * @return true if at least one row in this sheet was updated.
     */
    private static boolean processSheet(Sheet sheet, String year, GRider instance) throws Exception {
        String sheetName = sheet.getSheetName().trim();
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            System.out.println("  Sheet \"" + sheetName + "\" has no header row, skipping.");
            return false;
        }
 
        Map<String, Integer> headerIndex = new HashMap<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                headerIndex.put(cell.getStringCellValue().trim().toLowerCase(), i);
            }
        }
 
        Integer branchIdx = headerIndex.get("branch");
        if (branchIdx == null) {
            System.out.println("  Sheet \"" + sheetName + "\" has no Branch column, skipping (likely a rollup/summary sheet).");
            return false;
        }
 
        Integer capturedIdx = headerIndex.get("captured");
        if (capturedIdx == null) {
            System.out.println("  Sheet \"" + sheetName + "\" has no Captured column, skipping.");
            return false;
        }
 
        int[] monthIdx = new int[MONTH_ALIASES.length];
        for (int m = 0; m < MONTH_ALIASES.length; m++) {
            monthIdx[m] = -1;
            for (String alias : MONTH_ALIASES[m]) {
                Integer idx = headerIndex.get(alias);
                if (idx != null) {
                    monthIdx[m] = idx;
                    break;
                }
            }
        }
 
        boolean modified = false;
        int lastRow = sheet.getLastRowNum();
 
        for (int i = 1; i <= lastRow; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
 
            Cell branchCell = row.getCell(branchIdx);
            if (branchCell == null || branchCell.getCellType() != CellType.STRING) continue;
 
            String branchName = branchCell.getStringCellValue().trim();
            if (branchName.isEmpty()) continue;
 
            Cell capturedCell = row.getCell(capturedIdx);
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
                                        + ", sPeriodxx = " + SQLUtil.toSQL(year + MONTH_PERIODS[m])
                                        + ", nMCGoalxx = " + goal
                                        + ", dModified = " + serverDateSql
                                    + " ON DUPLICATE KEY UPDATE "
                                        + " nMCGoalxx = " + goal
                                        + ", dModified = " + serverDateSql;
 
                if (instance.executeQuery(upsertSql, "MC_Branch_Performance", instance.getBranchCode(), "") <= 0) {
                    System.err.println("Unable to execute command: " + upsertSql);
                    instance.rollbackTrans();
                    rowOk = false;
                    break;
                }
            }
 
            if (!rowOk) {
                // Preserve original fail-fast behavior: stop the whole run on a bad statement.
                System.exit(1);
                return modified;
            }
 
            instance.commitTrans();
            capturedCell.setCellValue("YES");
            modified = true;
        }
 
        return modified;
    }
}
