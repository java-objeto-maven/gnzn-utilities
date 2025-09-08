package org.guanzon.gnzn.utilities.lib.hcm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.base.StringHelper;
import org.guanzon.appdriver.base.WebClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Employees implements ILoveMyJobValidator{
    private final String SOURCE_CODE = "GCircle";
    
    private GRider _instance;
    private boolean _wparent;
    private String _message;
    
    private DayOfWeek _day;
    
    @Override
    public void setGRider(GRider foValue) {
        _instance = foValue;
    }
    
    @Override
    public void setWithParent(boolean fbValue) {
        _wparent = fbValue;
    }

    @Override
    public boolean Run(String fsBranchCd, String fsDateFrom, String fsDateThru) {
        if (_instance == null){
            _message = "Application driver is not set.";
            return false;
        }
        
        try {
            //get the users login for the day
            String lsSQL = getSQ_Master(fsDateFrom);
            ResultSet loRS = _instance.executeQuery(lsSQL);

            if (MiscUtil.RecordCount(loRS) == 0) {
                _message = "No app logins for the day.";
                return false;
            }

            while(loRS.next()){
                //check if the raffle entry was created for this user
                lsSQL = getSQ_Entry(loRS.getString("sBranchCd"), fsDateFrom, loRS.getString("sUserIDxx"), loRS.getString("sEmployID"));
                ResultSet loRX = _instance.executeQuery(lsSQL);
                
                if (!loRX.next()){
                    if (_day == DayOfWeek.MONDAY){
                        //is the employee has no IOC
                        if (!hasIOC(loRS.getString("sEmployID"))){
                            //create raffle entry
                            _instance.beginTrans();
                            lsSQL = "INSERT INTO RaffleEntries SET" +
                                    "  sRaffleID = " + SQLUtil.toSQL(MiscUtil.getNextCode("RaffleEntries", "sRaffleID", true, _instance.getConnection(), fsBranchCd)) +
                                    ", dTransact = " + SQLUtil.toSQL(fsDateFrom) +
                                    ", sBranchCd = " + SQLUtil.toSQL(loRS.getString("sBranchCd")) +
                                    ", sRaffleNo = " + SQLUtil.toSQL(StringHelper.prepad(String.valueOf(getNextRaffle(fsBranchCd, fsDateFrom)), 6, '0'))  +
                                    ", sAcctNmbr = " + SQLUtil.toSQL(loRS.getString("sUserIDxx")) +
                                    ", sClientID = " + SQLUtil.toSQL(loRS.getString("sEmployID")) +
                                    ", sMobileNo = " + SQLUtil.toSQL(loRS.getString("sMobileNo")) +
                                    ", sReferNox = NULL" + 
                                    ", sSourceCd = " + SQLUtil.toSQL(SOURCE_CODE) +
                                    ", cRaffledx = '0'" +
                                    ", cMsgSentx = '0'" + 
                                    ", sModified = " + SQLUtil.toSQL(_instance.getUserID()) +
                                    ", dModified = " + SQLUtil.toSQL(_instance.getServerDate());

                            System.err.println(lsSQL);
                            if (_instance.executeUpdate(lsSQL) <= 0){
                                _instance.rollbackTrans();
                                _message = "Unable to create raffle entry.";
                                return false;
                            }
                            _instance.commitTrans();
                        }
                    } else {
                        _instance.beginTrans();
                        lsSQL = "INSERT INTO RaffleEntries SET" +
                                "  sRaffleID = " + SQLUtil.toSQL(MiscUtil.getNextCode("RaffleEntries", "sRaffleID", true, _instance.getConnection(), fsBranchCd)) +
                                ", dTransact = " + SQLUtil.toSQL(fsDateFrom) +
                                ", sBranchCd = " + SQLUtil.toSQL(loRS.getString("sBranchCd")) +
                                ", sRaffleNo = " + SQLUtil.toSQL(StringHelper.prepad(String.valueOf(getNextRaffle(fsBranchCd, fsDateFrom)), 6, '0'))  +
                                ", sAcctNmbr = " + SQLUtil.toSQL(loRS.getString("sUserIDxx")) +
                                ", sClientID = " + SQLUtil.toSQL(loRS.getString("sEmployID")) +
                                ", sMobileNo = " + SQLUtil.toSQL(loRS.getString("sMobileNo")) +
                                ", sReferNox = NULL" + 
                                ", sSourceCd = " + SQLUtil.toSQL(SOURCE_CODE) +
                                ", cRaffledx = '0'" +
                                ", cMsgSentx = '0'" + 
                                ", sModified = " + SQLUtil.toSQL(_instance.getUserID()) +
                                ", dModified = " + SQLUtil.toSQL(_instance.getServerDate());

                        System.err.println(lsSQL);
                        if (_instance.executeUpdate(lsSQL) <= 0){
                            _instance.rollbackTrans();
                            _message = "Unable to create raffle entry.";
                            return false;
                        }
                        _instance.commitTrans();
                    }
                    
                }
            }
            
            lsSQL = "SELECT sAcctNmbr" +
                    " FROM RaffleEntries" +
                    " WHERE dTransact = " + SQLUtil.toSQL(fsDateFrom) +
                        " AND sSourceCd = " + SQLUtil.toSQL(SOURCE_CODE) +
                        " AND cMsgSentx = '0'";
            loRS = _instance.executeQuery(lsSQL);
            
            JSONArray rcpts = new JSONArray();
            JSONObject rcpt;
            while(loRS.next()){
                rcpt = new JSONObject();
                rcpt.put("app", "gRider");
                rcpt.put("user",loRS.getString("sAcctNmbr"));
                rcpts.add(rcpt);
            }
            
            if (!rcpts.isEmpty()){
                if (_day == DayOfWeek.MONDAY){
                    if (SendRegularSystemNotification(rcpts, 
                                                "I Love My Job Mondays",
                                                "Congratulations! You are entitled for 1 ticket on todays raffle draw.")){

                        lsSQL = "UPDATE RaffleEntries SET" +
                                    "  cMsgSentx = '1'" +
                                " WHERE dTransact = " + SQLUtil.toSQL(fsDateFrom) +
                                    " AND sSourceCd = " + SQLUtil.toSQL(SOURCE_CODE) +
                                    " AND cMsgSentx = '0'";
                        _instance.executeUpdate(lsSQL);
                    }
                } if (_day == DayOfWeek.THURSDAY) {
                    if (SendRegularSystemNotification(rcpts, 
                                                "I Love My Job Thursdays",
                                                "Congratulations! You are entitled for 1 ticket on todays raffle draw.")){

                        lsSQL = "UPDATE RaffleEntries SET" +
                                    "  cMsgSentx = '1'" +
                                " WHERE dTransact = " + SQLUtil.toSQL(fsDateFrom) +
                                    " AND sSourceCd = " + SQLUtil.toSQL(SOURCE_CODE) +
                                    " AND cMsgSentx = '0'";
                        _instance.executeUpdate(lsSQL);
                    }
                }
            }
        } catch (SQLException e) {
            _message = e.getMessage();
            return false;
        }
        
        return true;
    }

    @Override
    public String getMessage() {
        return _message;
    }
    
    private boolean hasIOC(String fsEmployID) throws SQLException{
        // Define the desired date format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        // Get the current date
        LocalDate currentDate = LocalDate.now();
        
        // Get the YearMonth instance of the previous month
        YearMonth previousMonth = YearMonth.from(currentDate.minusMonths(1));
        
        // Get the first and last day of the previous month
        LocalDate ldFrom = previousMonth.atDay(1);
        LocalDate ldThru = previousMonth.atEndOfMonth();
        
        // Format the dates
        String lsFrom = ldFrom.format(formatter);
        String lsThru = ldThru.format(formatter);
        
        String lsSQL = "SELECT" +
                            "  e.nYearxxxx" +
                            ", a.sTransNox" +
                            ", a.dTransact" +
                            ", IFNULL(CONCAT(b.sLastName, ', ', b.sFrstName, IF(IFNULL(b.sSuffixNm, '') = '', ' ', CONCAT(' ', b.sSuffixNm, ' ')), b.sMiddName), c.sDeptName) sFullName" +
                            ", d.sVioltnNm" +
                        " FROM IOC_Master a" +
                            " LEFT JOIN Client_Master b ON a.sEmployTo = b.sClientID" +
                            " LEFT JOIN Department c ON a.sEmployFr = c.sDeptIDxx" +
                            " LEFT JOIN CRR_Violation d ON a.sRegltnID = d.sVioltnID" +
                            " LEFT JOIN HR_Calendar_Period_Annual e ON a.dTransact BETWEEN e.sPeriodFr AND e.sPeriodTo" +
                            ", Employee_Master001 f" +
                        " WHERE a.sEmployTo = f.sEmployID" +
                            " AND a.sEmployTo = " + SQLUtil.toSQL(fsEmployID) +
                            " AND a.dTransact BETWEEN " + SQLUtil.toSQL(lsFrom) + " AND " + SQLUtil.toSQL(lsThru) +
                            " AND f.sEmpLevID = '0'" +
                            " AND a.cTranStat <> '3'" +
                        " ORDER BY dTransact DESC";

        ResultSet loRS = _instance.executeQuery(lsSQL);
        
        return loRS.next();
    }
    
    private boolean SendRegularSystemNotification(JSONArray rcpts,
                                                        String title,
                                                        String message){
        try{
            String sURL = "https://restgk.guanzongroup.com.ph/notification/send_request_system.php";
            Calendar calendar = Calendar.getInstance();
            //Create the header section needed by the API
            Map<String, String> headers =
                    new HashMap<String, String>();
            headers.put("Accept", "application/json");
            headers.put("Content-Type", "application/json");
            headers.put("g-api-id", "gRider");
            headers.put("g-api-imei", "356060072281722");
            headers.put("g-api-key", SQLUtil.dateFormat(calendar.getTime(), "yyyyMMddHHmmss"));
            headers.put("g-api-hash", org.apache.commons.codec.digest.DigestUtils.md5Hex((String)headers.get("g-api-imei") + (String)headers.get("g-api-key")));
            headers.put("g-api-user", "GAP0190001");
            headers.put("g-api-mobile", "09171870011");
            headers.put("g-api-token", "cPYKpB-pPYM:APA91bE82C4lKZduL9B2WA1Ygd0znWEUl9rM7pflSlpYLQJq4Nl9l5W4tWinyy5RCLNTSs3bX3JjOVhYnmCpe7zM98cENXt5tIHwW_2P8Q3BXI7gYtEMTJN5JxirOjNTzxWHkWDEafza");

            JSONObject param = new JSONObject();
            param.put("type", "00000");
            param.put("parent", null);
            param.put("title", title);
            param.put("message", message);
            param.put("rcpt", rcpts);
            param.put("infox", null);

            String response = WebClient.sendHTTP(sURL, param.toJSONString(), (HashMap<String, String>) headers);
            if(response == null){
                System.out.println("HTTP Error detected: " + System.getProperty("store.error.info"));
                System.exit(1);
            }

            System.out.println(response);
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    
    private int getNextRaffle(String fsBranchCd, String fsTransact) throws SQLException{
        String lsSQL = "SELECT sRaffleNo" +
                        " FROM RaffleEntries" +
                        " WHERE sRaffleID LIKE " + SQLUtil.toSQL(fsBranchCd + String.valueOf(MiscUtil.getDateYear(_instance.getServerDate())).substring(2) + "%") +
                            " AND dTransact = " + SQLUtil.toSQL(fsTransact) +
                        " ORDER BY sRaffleID DESC" +
                        " LIMIT 1";
        ResultSet loRS = _instance.executeQuery(lsSQL);
        
        if (loRS.next())
            return Integer.parseInt(loRS.getString("sRaffleNo")) + 1;
        else 
            return 1;
    }
    
    private String getSQ_Entry(String fsBranchCd, String fsTransact, String fsUserIDxx, String fsEmployID){
        return "SELECT *" +
                " FROM RaffleEntries" +
                " WHERE dTransact = " + SQLUtil.toSQL(fsTransact) +
                    " AND sBranchCd = " + SQLUtil.toSQL(fsBranchCd) +
                    " AND sClientID = " + SQLUtil.toSQL(fsEmployID) + 
                    " AND sAcctNmbr = " + SQLUtil.toSQL(fsUserIDxx) +
                    " AND sSourceCd = " + SQLUtil.toSQL(SOURCE_CODE);
    }
    
    private String getSQ_Master(String dTranDate){
        LocalDate date = LocalDate.parse(dTranDate, DateTimeFormatter.ISO_LOCAL_DATE);
        _day = date.getDayOfWeek();
        
        switch (_day){
            case MONDAY:
                return "SELECT" +
                            "  a.sUserIDxx" +
                            ", c.sCompnyNm" +
                            ", d.sDeptName" +
                            ", e.sPositnNm" +
                            ", a.sEmailAdd" +
                            ", f.dLogInxxx" +
                            ", c.sMobileNo" +
                            ", b.sEmployID" +
                            ", f.sLogNoxxx" +
                            ", b.sBranchCd" +
                        " FROM App_User_Master a" +
                            ", Employee_Master001 b" + 
                                " LEFT JOIN Client_Master c" + 
                                    " ON b.sEmployID = c.sClientID" + 
                                " LEFT JOIN Department d" + 
                                    " ON b.sDeptIDxx = d.sDeptIDxx" + 
                                " LEFT JOIN `Position` e" + 
                                    " ON b.sPositnID = e.sPositnID" +
                                ", (SELECT * FROM xxxSysUserLog" + 
                                    " WHERE sProdctID = 'gRider'" + 
                                    " AND dLogInxxx BETWEEN " + 
                                        SQLUtil.toSQL(SQLUtil.dateFormat(CommonUtils.dateAdd(SQLUtil.toDate(dTranDate, SQLUtil.FORMAT_SHORT_DATE), -7), SQLUtil.FORMAT_SHORT_DATE) + " 00:00:01") + 
                                            " AND " + SQLUtil.toSQL(dTranDate + " 23:59:00") + ") f" + 
                        " WHERE a.sEmployNo = b.sEmployID" + 
                            " AND a.sUserIDxx = f.sUserIDxx" + 
                            " AND a.sProdctID = 'gRider'" + 
                            " AND a.cActivatd = '1'" + 
                            " AND a.sEmployNo is NOT NULL" + 
                            " AND b.dFiredxxx IS NULL" + 
                            " AND b.cRecdStat = '1'" + 
                        " GROUP BY a.sUserIDxx" + 
                        " ORDER BY f.dLogInxxx";
            case THURSDAY:
                return "SELECT" +
                            "  a.sUserIDxx" +
                            ", c.sCompnyNm" +
                            ", d.sDeptName" +
                            ", e.sPositnNm" +
                            ", a.sEmailAdd" +
                            ", f.dLogInxxx" +
                            ", c.sMobileNo" +
                            ", b.sEmployID" +
                            ", f.sLogNoxxx" +
                            ", b.sBranchCd" +
                        " FROM App_User_Master a" +
                            ", Employee_Master001 b" + 
                                " LEFT JOIN Client_Master c" + 
                                    " ON b.sEmployID = c.sClientID" + 
                                " LEFT JOIN Department d" + 
                                    " ON b.sDeptIDxx = d.sDeptIDxx" + 
                                " LEFT JOIN `Position` e" + 
                                    " ON b.sPositnID = e.sPositnID" +
                                ", (SELECT * FROM xxxSysUserLog" + 
                                    " WHERE sProdctID = 'gRider'" + 
                                    " AND dLogInxxx BETWEEN " + 
                                        SQLUtil.toSQL(SQLUtil.dateFormat(CommonUtils.dateAdd(SQLUtil.toDate(dTranDate, SQLUtil.FORMAT_SHORT_DATE), -3), SQLUtil.FORMAT_SHORT_DATE) + " 00:00:01") + 
                                            " AND " + SQLUtil.toSQL(dTranDate + " 23:59:00") + ") f" + 
                        " WHERE a.sEmployNo = b.sEmployID" + 
                            " AND a.sUserIDxx = f.sUserIDxx" + 
                            " AND a.sProdctID = 'gRider'" + 
                            " AND a.cActivatd = '1'" + 
                            " AND b.sBranchCd IN " + System.getProperty("ilmj.main.office") +
                            " AND b.sDeptIDxx <> '015'" +
                            " AND a.sEmployNo is NOT NULL" + 
                            " AND b.dFiredxxx IS NULL" + 
                            " AND b.cRecdStat = '1'" + 
                        " GROUP BY a.sUserIDxx" + 
                        " ORDER BY f.dLogInxxx";
            case FRIDAY:
                return "SELECT" +
                            "  a.sBranchCd" +
                            ", a.sEmployID sUserIDxx" +
                            ", a.sEmployID" +
                            ", IFNULL(b.sMobileNo, '') sMobileNo" +
                            ", b.sCompnyNm" +
                        " FROM Employee_Master001 a" +
                            ", Client_Master b" +
                        " WHERE a.sEmployID = b.sClientID" +
                            " AND a.sBranchCd = 'M0W1'" +
                            " AND a.cRecdStat = '1'" +
                            " AND b.cGenderCd = '1'";
            default:
                return "SELECT 0 = 1;";
        }
    }
}