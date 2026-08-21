package org.guanzon.gnzn.utilities.goals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.SQLUtil;
 
/**
 * Captures AREA-level monthly sales goals (e.g. "R1-A", "R2", "BIGBIKE", "INSTITUTIONAL")
 * from a single-sheet workbook where month headers are date cells rather than text.
 *
 * NOTE: The workbook labels the code column "Branch", but the values in it (R1-A, R2,
 * INSTITUTIONAL, etc.) are AREA codes, not individual branch names — this is area-level
 * data, one row per area, not per branch. Area codes are resolved against Branch_Area,
 * and goals are written to MC_Area_Performance — column names are still a best guess,
 * see the TODOs.
 */

public class MotorcycleArea {
    // TODO: confirm the exact column names below against the Branch_Area schema before
    // running against production — table name is confirmed, column names are still a guess.
    private static final String LOOKUP_TABLE = "Branch_Area";   // table containing area codes/names
    private static final String LOOKUP_NAME_COLUMN = "sAreaDesc"; // column to LIKE-match the sheet's code/name against
    private static final String LOOKUP_CODE_COLUMN = "sAreaCode"; // column to pull the resolved area code from
 
    private static final String TARGET_TABLE = "MC_Area_Performance"; // table to write goals into
    private static final String TARGET_CODE_COLUMN = "sAreaCode";
    private static final String TARGET_PERIOD_COLUMN = "sPeriodxx";
    private static final String TARGET_GOAL_COLUMN = "nMCGoalxx";
    private static final String TARGET_MODIFIED_COLUMN = "dModified";
 
    private static final String DEFAULT_FILE_NAME = "FINAL MC SALES GOAL 2026 AREA ONLY.xlsx";
    private static final String DEFAULT_YEAR = "2026";
    private static final Pattern YEAR_IN_FILENAME = Pattern.compile("(19|20)\\d{2}");
 
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
 
                // This format is a single sheet, but loop defensively in case a future version adds more.
                int sheetCount = workbook.getNumberOfSheets();
                for (int s = 0; s < sheetCount; s++) {
                    Sheet sheet = workbook.getSheetAt(s);
                    System.out.println("Checking sheet \"" + sheet.getSheetName() + "\"...");
                    boolean sheetModified = processSheet(sheet, year, instance);
                    anyRowModified = anyRowModified || sheetModified;
                }
 
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
     * Resolves the code column (first string header, excluding "Captured") and the 12 month
     * columns by inspecting the header row's actual cell type/value rather than its text —
     * month headers here are date cells (e.g. Jan 1 2012) rather than "JAN"/"Jan" strings.
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
 
        Integer codeIdx = null;
        Integer capturedIdx = null;
        // monthIdx[0] = January column, monthIdx[11] = December column
        int[] monthIdx = new int[12];
        java.util.Arrays.fill(monthIdx, -1);
 
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell == null) continue;
 
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(cell.getDateCellValue());
                int monthNumber = cal.get(Calendar.MONTH); // 0 = Jan ... 11 = Dec
                monthIdx[monthNumber] = i;
            } else if (cell.getCellType() == CellType.STRING) {
                String header = cell.getStringCellValue().trim().toLowerCase();
                if (header.equals("captured")) {
                    capturedIdx = i;
                } else if (codeIdx == null && !header.equals("total")
                        && !header.matches("q[1-4]")) {
                    // First non-quarter/total string header is treated as the area code column
                    // (labeled "Branch" in this workbook, but holds area codes like "R1-A").
                    codeIdx = i;
                }
            }
        }
 
        if (codeIdx == null) {
            System.out.println("  Sheet \"" + sheetName + "\" has no recognizable code column, skipping.");
            return false;
        }
        if (capturedIdx == null) {
            System.out.println("  Sheet \"" + sheetName + "\" has no Captured column, skipping.");
            return false;
        }
 
        boolean modified = false;
        int lastRow = sheet.getLastRowNum();
 
        for (int i = 1; i <= lastRow; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
 
            Cell codeCell = row.getCell(codeIdx);
            if (codeCell == null || codeCell.getCellType() != CellType.STRING) continue;
 
            String areaCode = codeCell.getStringCellValue().trim();
            if (areaCode.isEmpty()) continue;
 
            Cell capturedCell = row.getCell(capturedIdx);
            boolean alreadyMarkedNo = capturedCell != null
                    && capturedCell.getCellType() == CellType.STRING
                    && "no".equalsIgnoreCase(capturedCell.getStringCellValue());
 
            if (!alreadyMarkedNo) continue;
 
            String lookupSql = "SELECT " + LOOKUP_CODE_COLUMN + " FROM " + LOOKUP_TABLE
                    + " WHERE " + LOOKUP_NAME_COLUMN + " LIKE " + SQLUtil.toSQL("%" + areaCode + "%");
 
            String resolvedCode = null;
            try (ResultSet rs = instance.executeQuery(lookupSql)) {
                if (rs.next()) {
                    resolvedCode = rs.getString(LOOKUP_CODE_COLUMN);
                }
            }
 
            if (resolvedCode == null) continue;
 
            String resolvedCodeSql = SQLUtil.toSQL(resolvedCode);
            String serverDateSql = SQLUtil.toSQL(instance.getServerDate());
 
            instance.beginTrans();
            boolean rowOk = true;
 
            for (int m = 0; m < monthIdx.length; m++) {
                if (monthIdx[m] == -1) continue;
 
                Cell monthCell = row.getCell(monthIdx[m]);
                double goal = (monthCell != null && monthCell.getCellType() == CellType.NUMERIC)
                        ? monthCell.getNumericCellValue()
                        : 0d;
 
                String period = year + String.format("%02d", m + 1);
 
                String upsertSql = "INSERT INTO " + TARGET_TABLE + " SET"
                                        + "  " + TARGET_CODE_COLUMN + " = " + resolvedCodeSql
                                        + ", " + TARGET_PERIOD_COLUMN + " = " + SQLUtil.toSQL(period)
                                        + ", " + TARGET_GOAL_COLUMN + " = " + goal
                                        + ", " + TARGET_MODIFIED_COLUMN + " = " + serverDateSql
                                    + " ON DUPLICATE KEY UPDATE "
                                        + " " + TARGET_GOAL_COLUMN + " = " + goal
                                        + ", " + TARGET_MODIFIED_COLUMN + " = " + serverDateSql;
 
                if (instance.executeQuery(upsertSql, TARGET_TABLE, instance.getBranchCode(), "") <= 0) {
                    System.err.println("Unable to execute command: " + upsertSql);
                    instance.rollbackTrans();
                    rowOk = false;
                    break;
                }
            }
 
            if (!rowOk) {
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
